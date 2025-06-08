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
        endTimeMillis: Long = -1,
        limitCount: Int = -1,
        limitType: LimitType = LimitType.TIME_LIMIT
    ) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = JSONObject().apply {
            put("packageName", packageName)
            put("limitTimeMinutes", limitTimeMinutes)
            put("limitCount", limitCount)
            put("startTimeMillis", System.currentTimeMillis())
            put("endTimeMillis", endTimeMillis)
            put("limitType", limitType.name)
            put("isBlocked", true)
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
            isBlocked = json.optBoolean("isBlocked", true)
        )
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
                isBlocked = json.optBoolean("isBlocked", true)
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
            val isBlocked = json.optBoolean("isBlocked", true)
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

    fun setTimeLimit(context: Context, packageName: String, minutes: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(KEY_TIME_LIMIT + packageName, minutes) }
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
        }
    }
}
