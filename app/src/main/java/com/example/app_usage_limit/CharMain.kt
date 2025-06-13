package com.example.app_usage_limit

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import com.example.app_usage_limit.util.AppLimitInfo
import com.example.app_usage_limit.util.AppLimitStorage
import com.example.app_usage_limit.util.LimitType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object CharMain {
    val level = mutableIntStateOf(1)
    val exp = mutableLongStateOf(0L)
    val isLimitMode = mutableStateOf(false)
    val dailyHour = mutableIntStateOf(4)

    private var limitStartTime = 0L
    private const val TARGET_PACKAGE = "com.example.targetapp"
    private var monitoringJob: Job? = null
    private val monitoringScope = CoroutineScope(Dispatchers.Main)

    fun startMonitoring(context: Context) {
        if (monitoringJob?.isActive == true) return
        monitoringJob = monitoringScope.launch {
            while (true) {
                AppLimitStorage.autoUpdateExpiredBlocks(context) // 제한 상태 최신화
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
        val prefs = context.getSharedPreferences("growth", Context.MODE_PRIVATE)
        level.value = prefs.getInt("level", 1)
        exp.value = prefs.getLong("exp", 0L)
        dailyHour.value = prefs.getInt("dailyHour", 4)

        // 지난 제한 기록 불러와서 경험치 반영
        val info = AppLimitStorage.getLimitInfo(context, TARGET_PACKAGE)
        info?.let {
            val end = if (it.endTimeMillis > 0) it.endTimeMillis else System.currentTimeMillis()
            val duration = end - it.startTimeMillis
            if (duration > 0) {
                accumulateXP(duration, context)
                Toast.makeText(context, "지난 세션 ${duration / 60000}분 적립", Toast.LENGTH_SHORT).show()
                AppLimitStorage.clearLimitInfo(context, TARGET_PACKAGE)
            }
        }
    }

    fun startLimitMode(context: Context) {
        if (isLimitMode.value) return
        isLimitMode.value = true
        limitStartTime = System.currentTimeMillis()

        AppLimitStorage.saveLimitInfo(
            context,
            packageName = TARGET_PACKAGE,
            limitTimeMinutes = dailyHour.value * 60,
            startTimeMillis = limitStartTime,
            endTimeMillis = 0L,
            isBlocked = true,
            limitType = LimitType.TIME_LIMIT
        )
        AppLimitStorage.setTimeLimit(context, TARGET_PACKAGE, dailyHour.value * 60)
    }

    fun stopLimitMode(context: Context) {
        if (!isLimitMode.value) return
        isLimitMode.value = false

        val duration = System.currentTimeMillis() - limitStartTime
        accumulateXP(duration, context)

        AppLimitStorage.saveLimitInfo(
            context,
            packageName = TARGET_PACKAGE,
            limitTimeMinutes = dailyHour.value * 60,
            startTimeMillis = limitStartTime,
            endTimeMillis = System.currentTimeMillis(),
            isBlocked = false,
            limitType = LimitType.TIME_LIMIT
        )
        AppLimitStorage.updateLimitBlockedStatus(context, TARGET_PACKAGE, false)
    }

    fun setDailyHour(context: Context, hours: Int) {
        dailyHour.value = hours
        context.getSharedPreferences("growth", Context.MODE_PRIVATE)
            .edit()
            .putInt("dailyHour", hours)
            .apply()
        // 제한 정보도 업데이트
        AppLimitStorage.setTimeLimit(context, TARGET_PACKAGE, hours * 60)
    }

    fun showAlarmDialog(context: Context) {
        Toast.makeText(context, "시간 설정 UI는 별도 구현 필요", Toast.LENGTH_SHORT).show()
    }

    private fun accumulateXP(ms: Long, context: Context) {
        val prefs = context.getSharedPreferences("growth", Context.MODE_PRIVATE)
        val base = dailyHour.value * 0.5 * 3600 * 1000
        val threshold = base * (1 + 0.05 * (level.value / 10.0))
        val totalExp = exp.value + ms

        if (totalExp >= threshold) {
            level.value += 1
            exp.value = (totalExp - threshold).toLong()
            Toast.makeText(context, "레벨업! 현재 레벨: ${level.value}", Toast.LENGTH_SHORT).show()
        } else {
            exp.value = totalExp
        }

        prefs.edit()
            .putInt("level", level.value)
            .putLong("exp", exp.value)
            .apply()
    }

    private fun updateLimitModeByBlockedState(context: Context) {
        val info = AppLimitStorage.getLimitInfo(context, TARGET_PACKAGE)
        if (info?.isBlocked == true && !isLimitMode.value) {
            startLimitMode(context)
        } else if (info?.isBlocked == false && isLimitMode.value) {
            stopLimitMode(context)
        }
    }
}