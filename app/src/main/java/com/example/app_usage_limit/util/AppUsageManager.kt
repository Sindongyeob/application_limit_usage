package com.example.app_usage_limit.util

import android.app.usage.UsageStatsManager
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import java.util.*

object AppUsageManager {
    private const val PREFS_NAME = "app_usage_prefs"
    private const val KEY_USAGE_TIME_PREFIX = "usage_time_"
    private const val KEY_LAUNCH_COUNT_PREFIX = "launch_count_"

    fun setLimitForApp(context: Context, packageName: String, limitMinutes: Int) {
        AppLimitStorage.saveLimitInfo(
            context = context,
            packageName = packageName,
            limitTimeMinutes = limitMinutes,
            limitType = LimitType.TIME_LIMIT
        )
    }

    // 실행 횟수 관련
    fun getLaunchCount(context: Context, packageName: String): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_LAUNCH_COUNT_PREFIX + packageName, 0)
    }

    fun incrementLaunchCount(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val count = getLaunchCount(context, packageName)
        prefs.edit { putInt(KEY_LAUNCH_COUNT_PREFIX + packageName, count + 1) }
    }

    fun resetLaunchCount(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { remove(KEY_LAUNCH_COUNT_PREFIX + packageName) }
        Log.d("AppUsageManager", "실행 횟수 초기화: $packageName")
    }

    // 사용 시간 관련
    fun addUsageTime(context: Context, packageName: String, millis: Long) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getLong(KEY_USAGE_TIME_PREFIX + packageName, 0L)
        prefs.edit { putLong(KEY_USAGE_TIME_PREFIX + packageName, current + millis) }
    }

    fun getUsageTime(context: Context, packageName: String): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_USAGE_TIME_PREFIX + packageName, 0L)
    }

    fun resetUsageTime(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { remove(KEY_USAGE_TIME_PREFIX + packageName) }
        Log.d("AppUsageManager", "사용 시간 초기화: $packageName")
    }

    fun getStoredUsageTime(context: Context, packageName: String): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_USAGE_TIME_PREFIX + packageName, 0L)
    }

    fun getUsedTimeToday(context: Context, packageName: String): Long {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStartMillis = calendar.timeInMillis

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, todayStartMillis, now
        )

        val targetStats = stats.firstOrNull { it.packageName == packageName }
        val time = targetStats?.totalTimeInForeground ?: 0L

        Log.d("AppUsageManager", "[$packageName] 오늘 사용 시간(ms): $time (${time / 1000 / 60}분)")
        return time
    }

    // ✅ 제한 해제 시 한 번에 초기화할 수 있는 함수
    fun resetLimits(context: Context, packageName: String) {
        resetUsageTime(context, packageName)
        resetLaunchCount(context, packageName)
        Log.d("AppUsageManager", "모든 제한 정보 초기화 완료: $packageName")
    }
}
