package com.example.app_usage_limit.util

import android.content.Context
import android.util.Log
import androidx.core.content.edit

object UnblockManager {
    private const val PREF_NAME = "UnblockedAppsPrefs"
    private const val KEY_PREFIX = "unblock_"

    fun isAppUnblocked(context: Context, packageName: String): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("$KEY_PREFIX$packageName", false)
    }

    fun unblockApp(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean("$KEY_PREFIX$packageName", true) }

        // ✅ 앱 제한 해제 시 모든 기록도 초기화
        AppUsageManager.resetLimits(context, packageName)

        Log.d("UnblockManager", "$packageName 앱 제한 해제됨")
    }

    fun resetUnblockState(context: Context, packageName: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { remove("$KEY_PREFIX$packageName") }
    }

    fun clearAllUnblocked(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            prefs.all.keys
                .filter { it.startsWith(KEY_PREFIX) }
                .forEach { remove(it) }
        }
    }

    fun getAllUnblockedApps(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.all
            .filter { it.key.startsWith(KEY_PREFIX) && it.value == true }
            .map { it.key.removePrefix(KEY_PREFIX) }
    }
}
