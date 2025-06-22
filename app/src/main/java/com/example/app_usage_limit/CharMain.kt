package com.example.app_usage_limit

import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.*
import com.example.app_usage_limit.util.AppLimitStorage
import com.example.app_usage_limit.util.LimitType
import kotlinx.coroutines.*
import java.util.Calendar

object CharMain {
    val level = mutableIntStateOf(1)
    val exp = mutableLongStateOf(0L)
    val isLimitMode = mutableStateOf(false)
    val dailyHour = mutableStateOf(4f)
    var wakeHour: Int = 0
    var wakeMinute: Int = 0
    var threshold: Double = 0.0
    var base: Double = 0.0

    private var limitStartTime = 0L
    private const val TARGET_PACKAGE = "com.example.targetapp"
    private var monitoringJob: Job? = null
    private val monitoringScope = CoroutineScope(Dispatchers.Main)
    private var expTimerJob: Job? = null

    fun startMonitoring(context: Context) {
        if (monitoringJob?.isActive == true) return
        monitoringJob = monitoringScope.launch {
            while (true) {
                AppLimitStorage.autoUpdateExpiredBlocks(context)
                updateLimitModeByBlockedState(context)
                delay(1000L)
            }
        }
    }

    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
    }

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences("char_stats", Context.MODE_PRIVATE)
        level.intValue = prefs.getInt("level", 1)
        exp.longValue = prefs.getLong("exp", 0L)
        dailyHour.value = prefs.getFloat("dailyHour", 4f)

        val info = AppLimitStorage.getLimitInfo(context, TARGET_PACKAGE)
        info?.let {
            val end = if (it.endTimeMillis > 0) it.endTimeMillis else System.currentTimeMillis()
            val duration = end - it.startTimeMillis
            if (duration > 0) {
                accumulateXP(context, duration)
                //Toast.makeText(context, "지난 세션 ${duration / 60000}분 적립", Toast.LENGTH_SHORT).show()
                AppLimitStorage.clearLimitInfo(context, TARGET_PACKAGE)
            }
        }
    }

    fun resetStats(context: Context) {
        level.intValue = 1
        exp.longValue = 0L
        dailyHour.value = 4f  // 또는 기본값 0f 등으로 바꿔도 됨

        val prefs = context.getSharedPreferences("char_stats", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt("level", 1)
            putLong("exp", 0L)
            putFloat("dailyHour", 4f)
            apply()
        }

        Toast.makeText(context, "캐릭터 정보가 초기화되었습니다", Toast.LENGTH_SHORT).show()
    }


    fun startLimitMode(context: Context) {
        if (isLimitMode.value) return
        isLimitMode.value = true
        limitStartTime = System.currentTimeMillis()

        AppLimitStorage.saveLimitInfo(
            context,
            packageName = TARGET_PACKAGE,
            limitTimeMinutes = (dailyHour.value * 60).toInt(),
            startTimeMillis = limitStartTime,
            endTimeMillis = 0L,
            isBlocked = true,
            limitType = LimitType.TIME_LIMIT
        )
        AppLimitStorage.setTimeLimit(context, TARGET_PACKAGE, dailyHour.value.toInt() * 60)

        startExpTimer(context)
    }

    fun stopLimitMode(context: Context) {
        if (!isLimitMode.value) return
        isLimitMode.value = false

        val duration = System.currentTimeMillis() - limitStartTime
        accumulateXP(context, duration)

        AppLimitStorage.saveLimitInfo(
            context,
            packageName = TARGET_PACKAGE,
            limitTimeMinutes = (dailyHour.value * 60).toInt(),
            startTimeMillis = limitStartTime,
            endTimeMillis = System.currentTimeMillis(),
            isBlocked = false,
            limitType = LimitType.TIME_LIMIT
        )
        AppLimitStorage.updateLimitBlockedStatus(context, TARGET_PACKAGE, false)

        stopExpTimer()
    }

    fun setDailyHour(context: Context, hours: Float) {
        dailyHour.value = hours
        context.getSharedPreferences("char_stats", Context.MODE_PRIVATE)
            .edit()
            .putFloat("dailyHour", hours)
            .apply()
        AppLimitStorage.setTimeLimit(context, TARGET_PACKAGE, hours.toInt() * 60)
    }

    fun showAlarmDialog(context: Context) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                // 기상 시간 설정
                wakeHour = hourOfDay
                wakeMinute = minute

                // 저장 (선택)
                val prefs = context.getSharedPreferences("char_stats", Context.MODE_PRIVATE)
                prefs.edit().apply {
                    putInt("wakeHour", wakeHour)
                    putInt("wakeMinute", wakeMinute)
                    apply()
                }

                Toast.makeText(
                    context,
                    "기상 시간: ${hourOfDay}시 ${minute}분 으로 설정되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            },
            currentHour,
            currentMinute,
            true
        ).show()
    }

    fun accumulateXP(context: Context, ms: Long) {
        val wakeMinutes = wakeHour * 60 + wakeMinute
        val usableMinutes = (1440 - wakeMinutes).coerceIn(60, 480) // 하루 1~8시간
        base = usableMinutes * 60 * 0.2  // 1초당 0.2 경험치
        threshold = base * (1 + 0.02 * (level.value / 10.0))  // 완만한 성장률

        val expGain = ms.toDouble() / 1000.0
        val totalExp = exp.value + expGain

        if (totalExp >= threshold) {
            level.value += 1
            exp.value = (totalExp - threshold).toLong()
            Toast.makeText(context, "레벨업! 현재 레벨: ${level.value}", Toast.LENGTH_SHORT).show()
        } else {
            exp.value = totalExp.toLong()
        }

        saveStats(context)
    }


    private fun startExpTimer(context: Context) {
        expTimerJob?.cancel()
        expTimerJob = CoroutineScope(Dispatchers.Default).launch {
            while (isLimitMode.value) {
                delay(1000L)
                accumulateXP(context, 1000L)
            }
        }
    }

    private fun stopExpTimer() {
        expTimerJob?.cancel()
        expTimerJob = null
    }

    private fun updateLimitModeByBlockedState(context: Context) {
        val info = AppLimitStorage.getLimitInfo(context, TARGET_PACKAGE)
        if (info?.isBlocked == true && !isLimitMode.value) {
            startLimitMode(context)
        } else if (info?.isBlocked == false && isLimitMode.value) {
            stopLimitMode(context)
        }
    }

    fun saveStats(context: Context) {
        val prefs = context.getSharedPreferences("char_stats", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putInt("level", level.intValue)
            putLong("exp", exp.longValue)
            putFloat("dailyHour", dailyHour.value)
            apply()
        }
    }

    fun loadStats(context: Context) {
        val prefs = context.getSharedPreferences("char_stats", Context.MODE_PRIVATE)
        level.value = prefs.getInt("level", 1)
        exp.value = prefs.getLong("exp", 0L)
        dailyHour.value = prefs.getFloat("dailyHour", 4f)
    }
}
