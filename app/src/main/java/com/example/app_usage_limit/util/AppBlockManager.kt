package com.example.app_usage_limit.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.app_usage_limit.block.SubjectInputActivity
import java.time.LocalTime

object AppBlockManager {

    fun isAppBlocked(context: Context, packageName: String): Boolean {
        val info = AppLimitStorage.getLimitInfo(context, packageName) ?: return false
        if (AppLimitStorage.isUnlocked(context, packageName)) return false
        if (UnblockManager.isAppUnblocked(context, packageName)) return false

        return when (info.limitType) {
            LimitType.TIME_LIMIT -> {
                val usedMillis = AppUsageManager.getUsedTimeToday(context, packageName)
                val limitMillis = info.limitTimeMinutes * 60 * 1000L
                Log.d("AppBlockCheck", "[$packageName] 시간 제한: $usedMillis / $limitMillis")
                usedMillis >= limitMillis
            }
            LimitType.TIME_OF_DAY -> {
                val currentTime = System.currentTimeMillis()
                Log.d("AppBlockCheck", "[$packageName] 시간대 제한: now=$currentTime, end=${info.endTimeMillis}")
                currentTime >= info.endTimeMillis
            }
            LimitType.USAGE_COUNT -> {
                val launchCount = AppUsageManager.getLaunchCount(context, packageName)
                Log.d("AppBlockCheck", "[$packageName] 실행 횟수 제한: $launchCount / ${info.limitCount}")
                launchCount >= info.limitCount
            }
        }
    }

    fun handleAppLaunchIfBlocked(context: Context, packageName: String): Boolean {

        Log.d("AppBlockManager", "앱 차단 화면 실행: $packageName")
        val intent = Intent(context, SubjectInputActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("PACKAGE_NAME", packageName)
        }
        context.startActivity(intent)
        return true
    }

    fun trackUsageIfNecessary(context: Context, packageName: String) {
        val info = AppLimitStorage.getLimitInfo(context, packageName) ?: return
        if (info.limitType == LimitType.USAGE_COUNT) {
            AppUsageManager.incrementLaunchCount(context, packageName)
            Log.d("AppBlockManager", "실행 횟수 증가: $packageName -> ${AppUsageManager.getLaunchCount(context, packageName)}회")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun checkAndAutoUnblockApps(context: Context) {
        val appList = AppLimitStorage.getAllLimitedApps(context)

        for (app in appList) {
            val shouldUnblock = when (app.limitType) {
                LimitType.TIME_LIMIT -> {
                    val usedMillis = AppUsageManager.getUsedTimeToday(context, app.packageName)
                    usedMillis < app.limitTimeMinutes * 60 * 1000L
                }
                LimitType.TIME_OF_DAY -> {
                    val now = LocalTime.now()
                    val end = LocalTime.ofSecondOfDay(app.endTimeMillis / 1000)
                    now.isAfter(end)
                }
                LimitType.USAGE_COUNT -> {
                    AppUsageManager.getLaunchCount(context, app.packageName) < app.limitCount
                }
            }

            if (app.isBlocked && shouldUnblock) {
                Log.d("AppBlockManager", "차단 해제 조건 만족: ${app.packageName}")
                UnblockManager.unblockApp(context, app.packageName)  // 내부에서 AppLimitStorage 처리 포함 가능
            }
        }
    }
}
