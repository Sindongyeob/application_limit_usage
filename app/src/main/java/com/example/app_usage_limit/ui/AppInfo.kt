package com.example.app_usage_limit.ui

import android.graphics.drawable.Drawable

// 앱 정보 데이터 클래스
data class AppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable
)