package com.example.app_usage_limit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.app_usage_limit.ui.screens.AppNavigator
import com.example.app_usage_limit.ui.theme.AppUsageLimitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppUsageLimitTheme {
                CharMain.loadStats(this@MainActivity) // 저장된 레벨과 경험치 불러오기
                AppNavigator()
            }
        }
    }
}
