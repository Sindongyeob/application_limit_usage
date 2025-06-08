package com.example.app_usage_limit.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.app_usage_limit.util.AppBlockManager
import com.example.app_usage_limit.util.AppLimitStorage
import com.example.app_usage_limit.util.AppUsageManager
import com.example.app_usage_limit.util.LimitType

class AppBlockAccessibilityService : AccessibilityService() {

    private var lastPackageName: String? = null
    private var lastTimestamp: Long = 0L

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
                Log.d("AccessibilityService", "[$lastPackageName] 사용 시간 누적(로컬 기록용): ${usedTime}ms")
                AppUsageManager.addUsageTime(this, lastPackageName!!, usedTime)  // 로그 기록용 또는 보조용
            }
        }

        lastPackageName = packageName
        lastTimestamp = now

        Log.d("AccessibilityService", "포그라운드 앱: $packageName")

        val limitInfo = AppLimitStorage.getLimitInfo(this, packageName)
        if (limitInfo != null && limitInfo.limitType == LimitType.TIME_LIMIT) {
            val usedTimeToday = AppUsageManager.getStoredUsageTime(this, packageName)
            val limitMillis = limitInfo.limitTimeMinutes * 60 * 1000

            Log.d("AccessibilityService", "[$packageName] 오늘 사용 시간: $usedTimeToday / 제한: $limitMillis")

            if (usedTimeToday >= limitMillis) {
                AppBlockManager.handleAppLaunchIfBlocked(this, packageName)
                return
            }
        }

        if (AppBlockManager.isAppBlocked(this, packageName)) {
            if (lastPackageName != packageName) {  // 또는 액티비티가 최상단인지 확인
                AppBlockManager.handleAppLaunchIfBlocked(this, packageName)
            }
        } else {
            AppBlockManager.trackUsageIfNecessary(this, packageName)
        }
    }

    override fun onInterrupt() {
        Log.d("AccessibilityService", "서비스 인터럽트됨")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        lastPackageName = null
        lastTimestamp = 0L
        return super.onUnbind(intent)
    }
}