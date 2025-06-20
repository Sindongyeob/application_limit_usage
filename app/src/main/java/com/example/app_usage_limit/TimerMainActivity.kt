package com.example.app_usage_limit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class TimerMainActivity : AppCompatActivity() {

    private lateinit var inputMinutes: EditText
    private lateinit var inputSeconds: EditText
    private lateinit var startTimerButton: Button

    private var isTimerRunning = false  // 기존 UI 상태용 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputMinutes = findViewById(R.id.inputMinutes)
        inputSeconds = findViewById(R.id.inputSeconds)
        startTimerButton = findViewById(R.id.startTimerButton)

        // 1. SharedPreferences에서 백그라운드 타이머 실행 여부 불러오기
        val prefs = getSharedPreferences("timer_prefs", MODE_PRIVATE)
        val isTimerRunningInBackground = prefs.getBoolean("isTimerRunning", false)

        // 2. 만약 백그라운드에서 타이머 실행 중이면 UI 상태도 맞춤
        if (isTimerRunningInBackground) {
            startTimerButton.text = "타이머 중지"
            isTimerRunning = true
        } else {
            startTimerButton.text = "타이머 시작"
            isTimerRunning = false
        }

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }

        startTimerButton.setOnClickListener {
            if(!isTimerRunning){
                val minutesStr = inputMinutes.text.toString()
                val secondsStr = inputSeconds.text.toString()

                val minutes = minutesStr.toLongOrNull() ?: 0L
                val seconds = secondsStr.toLongOrNull() ?: 0L

                if (minutes == 0L && seconds == 0L) {
                    Toast.makeText(this, "분 또는 초 단위로 시간을 입력하세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (seconds !in 0..59) {
                    Toast.makeText(this, "초는 0~59 사이의 값이어야 합니다", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val duration = (minutes * 60 + seconds) * 1000L  // 밀리초 변환

                val serviceIntent = Intent(this, TimerService::class.java).apply {
                    action = "START_TIMER"
                    putExtra("durationMillis", duration)
                }
                ContextCompat.startForegroundService(this, serviceIntent)

                Toast.makeText(this, "타이머 시작: ${minutes}분 ${seconds}초", Toast.LENGTH_SHORT).show()
                startTimerButton.text = "타이머 중지"
                isTimerRunning = true

                // 3. SharedPreferences에 실행 상태 저장
                prefs.edit().putBoolean("isTimerRunning", true).apply()
            } else {
                Toast.makeText(this, "타이머 중지", Toast.LENGTH_SHORT).show()
                val stopIntent = Intent(this, TimerService::class.java).apply {
                    action = "STOP_TIMER"
                }
                startService(stopIntent)
                startTimerButton.text = "타이머 시작"
                isTimerRunning = false

                // 4. SharedPreferences에 실행 상태 저장
                prefs.edit().putBoolean("isTimerRunning", false).apply()
            }
        }
    }
}
