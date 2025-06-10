package com.example.app_usage_limit.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.app_usage_limit.util.AppBlockManager
import com.example.app_usage_limit.util.AppLimitStorage
import com.example.app_usage_limit.util.AppUsageManager
import com.example.app_usage_limit.util.LimitType

class AppBlockAccessibilityService : AccessibilityService() {

    private var lastPackageName: String? = null
    private var lastTimestamp: Long = 0L

    private var checkHandler: Handler? = null
    private var checkRunnable: Runnable? = null
    private val checkIntervalMillis = 5000L // 5초마다 체크

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AccessibilityService", "서비스 연결됨")

        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            packageNames = null
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName == this.packageName || packageName.startsWith("com.android") || packageName.startsWith("com.google.android")) return

        val now = System.currentTimeMillis()

        if (lastPackageName != null && lastPackageName != packageName) {
            val usedTime = now - lastTimestamp
            if (usedTime > 0) {
                Log.d("AccessibilityService", "[$lastPackageName] 사용 시간 누적: ${usedTime}ms")
                AppUsageManager.addUsageTime(this, lastPackageName!!, usedTime)
            }
        }

        lastPackageName = packageName
        lastTimestamp = now

        Log.d("AccessibilityService", "포그라운드 앱: $packageName")

        // 제한 즉시 차단 체크
        val limitInfo = AppLimitStorage.getLimitInfo(this, packageName)
        if (limitInfo != null && limitInfo.limitType == LimitType.TIME_LIMIT) {
            val usedTimeToday = AppUsageManager.getStoredUsageTime(this, packageName)
            val limitMillis = limitInfo.limitTimeMinutes * 60 * 1000

            Log.d("AccessibilityService", "[$packageName] 오늘 사용 시간: $usedTimeToday / 제한: $limitMillis")

            if (usedTimeToday >= limitMillis) {
                AppBlockManager.handleAppLaunchIfBlocked(this, packageName)
                stopPeriodicCheck()
                return
            }
        }

        // 실시간 감시 시작
        stopPeriodicCheck()
        startPeriodicCheck(packageName)

        // 블록 여부 확인
        if (AppBlockManager.isAppBlocked(this, packageName)) {
            AppBlockManager.handleAppLaunchIfBlocked(this, packageName)
        } else {
            AppBlockManager.trackUsageIfNecessary(this, packageName)
        }
    }

    private fun startPeriodicCheck(packageName: String) {
        checkHandler?.removeCallbacksAndMessages(null)
        checkHandler = Handler(Looper.getMainLooper())
        checkRunnable = object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val usedTime = currentTime - lastTimestamp

                if (usedTime > 0) {
                    AppUsageManager.addUsageTime(this@AppBlockAccessibilityService, packageName, usedTime)
                    Log.d("AccessibilityService", "[$packageName] 주기 누적 사용 시간 추가: $usedTime")
                    lastTimestamp = currentTime
                }

                val limitInfo = AppLimitStorage.getLimitInfo(this@AppBlockAccessibilityService, packageName)
                if (limitInfo != null && limitInfo.limitType == LimitType.TIME_LIMIT) {
                    val totalUsedTime = AppUsageManager.getStoredUsageTime(this@AppBlockAccessibilityService, packageName)
                    val limit = limitInfo.limitTimeMinutes * 60 * 1000
                    Log.d("AccessibilityService", "[$packageName] 주기 체크 사용시간: $totalUsedTime / 제한: $limit")

                    if (totalUsedTime >= limit) {
                        AppBlockManager.handleAppLaunchIfBlocked(this@AppBlockAccessibilityService, packageName)
                        stopPeriodicCheck()
                        return
                    }
                }

                checkHandler?.postDelayed(this, checkIntervalMillis)
            }
        }
        checkHandler?.postDelayed(checkRunnable!!, checkIntervalMillis)
    }

    private fun stopPeriodicCheck() {
        checkHandler?.removeCallbacksAndMessages(null)
        checkHandler = null
        checkRunnable = null
    }

    override fun onInterrupt() {
        Log.d("AccessibilityService", "서비스 인터럽트됨")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        lastPackageName = null
        lastTimestamp = 0L
        stopPeriodicCheck()
        return super.onUnbind(intent)
    }
}
