package com.example.app_usage_limit.util

data class AppLimitInfo(
    val packageName: String,
    val limitTimeMinutes: Int = -1,
    val limitCount: Int = -1,
    val startTimeMillis: Long = -1,
    val endTimeMillis: Long = -1,
    var launchLimit: Int = 0,
    val limitType: LimitType = LimitType.TIME_LIMIT,
    val isBlocked: Boolean = true
)
