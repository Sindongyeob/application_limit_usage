
package com.example.app_usage_limit.ui.screens

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.app_usage_limit.util.AppUsageManager
import androidx.core.graphics.drawable.toBitmap
import com.example.app_usage_limit.CharMain

@Composable
fun SetLimitScreen(
    packageName: String,
    onLimitSet: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    val appInfo = remember(packageName) {
        try {
            pm.getApplicationInfo(packageName, 0)
        } catch (e: Exception) {
            null
        }
    }
    val appName = appInfo?.let { pm.getApplicationLabel(it).toString() } ?: "알 수 없는 앱"
    val appIcon = appInfo?.loadIcon(pm)?.toBitmap()?.asImageBitmap()

    var timeLimitText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("앱 사용 제한 설정", style = MaterialTheme.typography.headlineMedium)

        if (appIcon != null) {
            Image(
                bitmap = appIcon,
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
        }

        Text(appName, style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = timeLimitText,
            onValueChange = {
                timeLimitText = it
                showError = false
            },
            label = { Text("제한 시간 (분)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = showError,
            singleLine = true
        )

        if (showError) {
            Text("유효한 숫자를 입력하세요.", color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                val limit = timeLimitText.toIntOrNull()
                if (limit != null && limit > 0) {
                    AppUsageManager.setLimitForApp(context, packageName, limit)

                    // CharMain 경험치 시스템 연동
                    val hourFloat = limit / 60f
                    //val hourInt = hourFloat.toInt()
                    CharMain.setDailyHour(context, hourFloat)
                    CharMain.startLimitMode(context)
                    CharMain.startMonitoring(context)

                    onLimitSet()
                } else {
                    showError = true
                }
            }
        ) {
            Text("제한 설정")
        }
    }
}
