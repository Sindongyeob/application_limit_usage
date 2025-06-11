package com.example.app_usage_limit

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var progressBar: ProgressBar
    private lateinit var levelText: TextView
    private lateinit var characterImage: ImageView
    private lateinit var btnLimitMode: Button

    private var level = 1
    private var exp = 0L
    private var dailyHour = 4

    private var isLimitMode = false
    private var limitStartTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_basic)

        // UI 요소 초기화
        progressBar = findViewById(R.id.expBar)
        levelText = findViewById(R.id.levelText)
        characterImage = findViewById(R.id.characterImage)
        btnLimitMode = findViewById(R.id.button1)

        // SharedPreferences 로드
        prefs = getSharedPreferences("growth", MODE_PRIVATE)
        level = prefs.getInt("level", 1)
        exp = prefs.getLong("exp", 0L)
        dailyHour = prefs.getInt("dailyHour", 4)

        updateUI()

        // 하루 목표 시간 표시 버튼
        findViewById<Button>(R.id.button2).setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("하루 목표 사용 시간: $dailyHour 시간")
                .setPositiveButton("확인", null)
                .show()
        }

        // 제한 모드 버튼
        btnLimitMode.setOnClickListener {
            if (!isLimitMode) startLimitMode() else stopLimitMode()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isLimitMode) {
            stopLimitMode()  // 백그라운드로 갈 때 자동 종료
        }
    }

    private fun startLimitMode() {
        isLimitMode = true
        limitStartTime = System.currentTimeMillis()
        btnLimitMode.text = "제한 모드 종료"
        Toast.makeText(this, "제한 모드를 시작합니다.", Toast.LENGTH_SHORT).show()
    }

    private fun stopLimitMode() {
        if (!isLimitMode) return

        val duration = System.currentTimeMillis() - limitStartTime
        accumulateXP(duration)
        isLimitMode = false
        btnLimitMode.text = "제한 모드 시작"
        Toast.makeText(this, "제한 모드를 종료했습니다.", Toast.LENGTH_SHORT).show()
    }

    private fun accumulateXP(ms: Long) {
        // 기본 목표 시간(밀리초)
        val base = dailyHour * 0.5 * 3600 * 1000
        // 다음 레벨까지 필요한 XP
        val threshold = base * (1 + 0.05 * (level / 10.0))

        exp += ms
        if (exp >= threshold) {
            level++
            exp -= threshold.toLong()
        }

        prefs.edit()
            .putInt("level", level)
            .putLong("exp", exp)
            .apply()

        updateUI()
    }

    private fun updateUI() {
        val base = dailyHour * 0.5 * 3600 * 1000
        val threshold = base * (1 + 0.05 * (level / 10.0))
        val percent = (exp * 100 / threshold).toInt().coerceAtMost(100)

        progressBar.progress = percent
        levelText.text = "레벨: $level"

        val imgIndex = ((level - 1) / 10).coerceAtMost(3)
        val resId = resources.getIdentifier("char${imgIndex + 1}", "drawable", packageName)
        characterImage.setImageResource(resId)
    }
