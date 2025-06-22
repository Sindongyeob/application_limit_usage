package com.example.app_usage_limit.util

import android.content.Context
import androidx.core.content.edit
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object AppLimitStorage {
    private const val PREF_NAME = "AppLimitPrefs"
    private const val KEY_PREFIX = "limit_"
    private const val KEY_LAUNCH_LIMIT = "launch_limit_"
    private const val KEY_TIME_LIMIT = "time_limit_"
    private const val UNLOCKED_SUFFIX = "_unlocked"

    // 이미 주신 함수들 유지
    fun isUnlocked(context: Context, packageName: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(packageName + UNLOCKED_SUFFIX, false)
    }

    fun setUnlocked(context: Context, packageName: String, unlocked: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(packageName + UNLOCKED_SUFFIX, unlocked) }
    }

    fun saveLimitInfo(
        context: Context,
        packageName: String,
        limitTimeMinutes: Int = -1,
        limitCount: Int = -1,
        limitType: LimitType = LimitType.TIME_LIMIT,
        startTimeMillis: Long = -1,
        endTimeMillis: Long = -1,
        isBlocked: Boolean = false
    ) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = JSONObject().apply {
            put("packageName", packageName)
            put("limitTimeMinutes", limitTimeMinutes)
            put("limitCount", limitCount)
            put("startTimeMillis", if (startTimeMillis != -1L) startTimeMillis else System.currentTimeMillis())
            put("endTimeMillis", endTimeMillis)
            put("limitType", limitType.name)
            put("isBlocked", isBlocked)
        }
        prefs.edit { putString("$KEY_PREFIX$packageName", json.toString()) }
    }

    fun getLimitInfo(context: Context, packageName: String): AppLimitInfo? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString("$KEY_PREFIX$packageName", null) ?: return null
        val json = JSONObject(jsonString)

        return AppLimitInfo(
            packageName = json.getString("packageName"),
            limitTimeMinutes = json.optInt("limitTimeMinutes", -1),
            limitCount = json.optInt("limitCount", -1),
            startTimeMillis = json.optLong("startTimeMillis", -1),
            endTimeMillis = json.optLong("endTimeMillis", -1),
            limitType = LimitType.valueOf(json.optString("limitType", LimitType.TIME_LIMIT.name)),
            isBlocked = json.optBoolean("isBlocked", false)
        )
    }

    fun clearLimitInfo(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            remove("$KEY_PREFIX$packageName")
            remove(KEY_TIME_LIMIT + packageName)
            remove(KEY_LAUNCH_LIMIT + packageName)
            remove(packageName + UNLOCKED_SUFFIX)
        }
    }

    fun getAllLimitedApps(context: Context): List<AppLimitInfo> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.all.mapNotNull { (key, value) ->
            if (!key.startsWith(KEY_PREFIX) || value !is String) return@mapNotNull null
            val json = JSONObject(value)
            AppLimitInfo(
                packageName = json.getString("packageName"),
                limitTimeMinutes = json.optInt("limitTimeMinutes", -1),
                limitCount = json.optInt("limitCount", -1),
                startTimeMillis = json.optLong("startTimeMillis", -1),
                endTimeMillis = json.optLong("endTimeMillis", -1),
                limitType = LimitType.valueOf(json.optString("limitType", LimitType.TIME_LIMIT.name)),
                isBlocked = json.optBoolean("isBlocked", false)
            )
        }
    }

    fun updateLimitBlockedStatus(context: Context, packageName: String, isBlocked: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString("$KEY_PREFIX$packageName", null) ?: return
        val json = JSONObject(jsonString).apply {
            put("isBlocked", isBlocked)
        }
        prefs.edit { putString("$KEY_PREFIX$packageName", json.toString()) }
    }

    fun unblockApp(context: Context, packageName: String) {
        updateLimitBlockedStatus(context, packageName, false)
        AppUsageManager.resetLaunchCount(context, packageName)
    }

    fun autoUpdateExpiredBlocks(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()

        val updatedEntries = prefs.all.mapNotNull { (key, value) ->
            val json = JSONObject(value as? String ?: return@mapNotNull null)
            val isBlocked = json.optBoolean("isBlocked", false)
            val limitType = LimitType.valueOf(json.optString("limitType", LimitType.TIME_LIMIT.name))

            val shouldUnblock = when (limitType) {
                LimitType.TIME_LIMIT -> {
                    val start = json.optLong("startTimeMillis", -1)
                    now - start >= TimeUnit.MINUTES.toMillis(json.optInt("limitTimeMinutes", -1).toLong())
                }
                LimitType.TIME_OF_DAY -> now >= json.optLong("endTimeMillis", -1)
                LimitType.USAGE_COUNT -> {
                    val packageName = json.getString("packageName")
                    AppUsageManager.getLaunchCount(context, packageName) >= json.optInt("limitCount", -1)
                }
            }

            if (isBlocked && shouldUnblock) {
                json.put("isBlocked", false)
                key to json.toString()
            } else null
        }

        prefs.edit {
            updatedEntries.forEach { (key, updatedJson) ->
                putString(key, updatedJson)
            }
        }
    }

    // 기존에 주신 시간 제한 저장 및 조회 함수 유지
    fun setTimeLimit(context: Context, packageName: String, minutes: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(KEY_TIME_LIMIT + packageName, minutes) }
        // 저장과 동시에 LimitInfo에 반영 (동기화)
        saveLimitInfo(context, packageName, limitTimeMinutes = minutes)
    }

    fun getTimeLimit(context: Context, packageName: String): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_TIME_LIMIT + packageName, 0)
    }

    fun setLaunchLimit(context: Context, packageName: String, count: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(KEY_LAUNCH_LIMIT + packageName, count) }
    }

    fun getLaunchLimit(context: Context, packageName: String): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_LAUNCH_LIMIT + packageName, 0)
    }

    fun removeLimitInfo(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            remove("$KEY_PREFIX$packageName")
            remove(KEY_TIME_LIMIT + packageName)
            remove(KEY_LAUNCH_LIMIT + packageName)
            remove(packageName + UNLOCKED_SUFFIX)
            remove("usage_$packageName") // 누적 사용시간도 제거
        }
    }

    // 누적 사용 시간 저장 및 조회 함수 (주신 부분 유지)
    fun accumulateUsage(context: Context, packageName: String, usageMillis: Long) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val prev = prefs.getLong("usage_$packageName", 0L)
        prefs.edit { putLong("usage_$packageName", prev + usageMillis) }
    }

    fun getAccumulatedUsage(context: Context, packageName: String): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getLong("usage_$packageName", 0L)
    }

    // 추가: 동적 제한 체크 및 차단 처리 함수
    fun checkAndBlockIfLimitExceeded(context: Context, packageName: String): Boolean {
        val limitInfo = getLimitInfo(context, packageName) ?: return false
        if (limitInfo.limitTimeMinutes <= 0) return false

        val usedMillis = getAccumulatedUsage(context, packageName)
        val limitMillis = limitInfo.limitTimeMinutes * 60L * 1000L

        val isBlocked = usedMillis >= limitMillis
        updateLimitBlockedStatus(context, packageName, isBlocked)
        return isBlocked
    }
}
