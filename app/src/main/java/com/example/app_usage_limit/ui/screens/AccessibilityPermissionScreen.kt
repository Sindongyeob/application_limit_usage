package com.example.app_usage_limit.ui.screens

import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.app_usage_limit.service.AppBlockAccessibilityService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AccessibilityPermissionScreen(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    var alreadyNavigated by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            val isAccessibilityEnabled = isAccessibilityServiceEnabled(context)
            val isUsageAccessGranted = isUsageAccessGranted(context)

            Log.d("PermissionScreen", "접근성: $isAccessibilityEnabled / 앱사용접근: $isUsageAccessGranted")

            if (isAccessibilityEnabled && isUsageAccessGranted && !alreadyNavigated) {
                alreadyNavigated = true
                onPermissionGranted()
                break
            }

            delay(2000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "앱 차단 기능을 위해\n다음 두 가지 권한이 필요합니다:\n\n1. 접근성 권한\n2. 앱 사용 접근 권한",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })

            coroutineScope.launch {
                delay(1000)
                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }) {
            Text("권한 설정 열기")
        }
    }
}

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedComponentName = ComponentName(context, AppBlockAccessibilityService::class.java).flattenToString()
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    val services = TextUtils.split(enabledServices, ":")
    val result = services.any { it.equals(expectedComponentName, ignoreCase = true) }

    Log.d("PermissionScreen", "expected=$expectedComponentName / 활성 목록=$services / 결과=$result")
    return result
}

fun isUsageAccessGranted(context: Context): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val currentTime = System.currentTimeMillis()
        val usageEvents = usageStatsManager.queryEvents(currentTime - 60_000, currentTime)
        return usageEvents.hasNextEvent()
    }
    return false
}
