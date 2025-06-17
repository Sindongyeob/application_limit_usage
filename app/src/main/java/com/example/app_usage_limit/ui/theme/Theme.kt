package com.example.app_usage_limit.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color.White, // 배경색 흰색
    surface = Color.White, // 표면색 흰색
    onBackground = Color.Black, // 배경 위 글자색 검정
    onSurface = Color.Black // 표면 위 글자색 검정
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color.White, // 배경색 흰색
    surface = Color.White, // 표면색 흰색
    onBackground = Color.Black, // 배경 위 글자색 검정
    onSurface = Color.Black // 표면 위 글자색 검정
)

@Composable
fun App_usage_limitTheme(
    darkTheme: Boolean = false, // 다크 모드 비활성화
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicLightColorScheme(context) // 항상 라이트 모드 사용
        }
        else -> LightColorScheme // 라이트 테마 고정
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}