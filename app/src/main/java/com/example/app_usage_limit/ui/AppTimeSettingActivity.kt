package com.example.app_usage_limit.ui

import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.app_usage_limit.R
import com.example.app_usage_limit.util.AppLimitStorage
import com.example.app_usage_limit.util.LimitType
import java.util.*

class AppTimeSettingActivity : AppCompatActivity() {

    private lateinit var appIconView: ImageView
    private lateinit var appNameView: TextView
    private lateinit var timeInput: EditText
    private lateinit var endTimeInput: EditText
    private lateinit var saveButton: Button

    private var packageName: String? = null
    private var appLabel: String = ""
    private var appIcon: Drawable? = null

    private var selectedEndTimeMillis: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_time_setting)

        appIconView = findViewById(R.id.imageViewAppIcon)
        appNameView = findViewById(R.id.textViewAppName)
        timeInput = findViewById(R.id.editTextTimeLimit)
        endTimeInput = findViewById(R.id.editTextEndTime)
        saveButton = findViewById(R.id.buttonSave)

        packageName = intent.getStringExtra("PACKAGE_NAME")

        if (packageName == null) {
            Toast.makeText(this, "앱 정보가 없습니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadAppInfo(packageName!!)
        appNameView.text = appLabel
        appIconView.setImageDrawable(appIcon)

        // 제한 시각 선택기
        endTimeInput.setOnClickListener {
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val endCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    if (before(Calendar.getInstance())) {
                        add(Calendar.DATE, 1) // 과거 시간이면 다음날로
                    }
                }
                selectedEndTimeMillis = endCal.timeInMillis
                endTimeInput.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
            }, hour, minute, true).show()
        }

        saveButton.setOnClickListener {
            val timeLimit = timeInput.text.toString().toIntOrNull()
            val hasTimeLimit = timeLimit != null && timeLimit > 0
            val hasEndTime = selectedEndTimeMillis > 0

            if (!hasTimeLimit && !hasEndTime) {
                Toast.makeText(this, "제한 시간 또는 종료 시각 중 하나를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val limitType = when {
                hasTimeLimit && hasEndTime -> LimitType.TIME_LIMIT // 기본적으로 시간 기준
                hasTimeLimit -> LimitType.TIME_LIMIT
                hasEndTime -> LimitType.TIME_OF_DAY
                else -> LimitType.TIME_LIMIT
            }

            AppLimitStorage.saveLimitInfo(
                context = this,
                packageName = packageName!!,
                limitTimeMinutes = timeLimit?.toInt() ?: -1,
                endTimeMillis = selectedEndTimeMillis,
                limitType = limitType
            )

            Toast.makeText(this, "제한 정보가 저장되었습니다", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadAppInfo(pkg: String) {
        try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(pkg, 0)
            appLabel = pm.getApplicationLabel(appInfo).toString()
            appIcon = pm.getApplicationIcon(pkg)
        } catch (e: PackageManager.NameNotFoundException) {
            appLabel = pkg
            appIcon = getDrawable(android.R.drawable.sym_def_app_icon)
        }
    }
}
