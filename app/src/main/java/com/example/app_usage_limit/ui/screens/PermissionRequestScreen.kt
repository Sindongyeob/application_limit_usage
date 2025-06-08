package com.example.app_usage_limit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.app_usage_limit.util.PermissionUtils

@Composable
fun PermissionRequestScreen(
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    var usagePermissionGranted by remember { mutableStateOf(PermissionUtils.hasUsageStatsPermission(context)) }
    var accessibilityPermissionGranted by remember { mutableStateOf(PermissionUtils.hasAccessibilityPermission(context)) }

    LaunchedEffect(usagePermissionGranted, accessibilityPermissionGranted) {
        if (usagePermissionGranted && accessibilityPermissionGranted) {
            onPermissionGranted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "앱 사용 제한을 위해 다음 권한을 허용해주세요.",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                PermissionUtils.openUsageAccessSettings(context)
            },
            enabled = !usagePermissionGranted
        ) {
            Text("사용량 액세스 권한 허용")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                PermissionUtils.openAccessibilitySettings(context)
            },
            enabled = !accessibilityPermissionGranted
        ) {
            Text("접근성 권한 허용")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = {
            // 권한 상태 재검사
            usagePermissionGranted = PermissionUtils.hasUsageStatsPermission(context)
            accessibilityPermissionGranted = PermissionUtils.hasAccessibilityPermission(context)
        }) {
            Text("권한 다시 확인")
        }
    }
}