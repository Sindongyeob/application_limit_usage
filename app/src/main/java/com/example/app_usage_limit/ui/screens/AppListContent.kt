package com.example.app_usage_limit.ui.screens

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.app_usage_limit.util.AppBlockManager
import com.example.app_usage_limit.util.AppLimitStorage
import com.example.app_usage_limit.util.LimitType.*

@Composable
fun AppListContent(
    appList: List<ApplicationInfo>,
    justEntered: Boolean,
    onBlockedAppClick: (String) -> Unit,
    onAppSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(appList) { app ->
            val packageName = app.packageName
            val appName = context.packageManager.getApplicationLabel(app).toString()
            val appIcon = context.packageManager.getApplicationIcon(app)

            val limitInfo = AppLimitStorage.getLimitInfo(context, packageName)
            val isBlocked = AppBlockManager.isAppBlocked(context, packageName)

            val statusText = when (limitInfo?.limitType) {
                TIME_LIMIT -> "시간 제한 중 (${limitInfo.limitTimeMinutes}분)"
                USAGE_COUNT -> "실행 횟수 제한 (${limitInfo.limitCount}회)"
                TIME_OF_DAY -> {
                    val start = formatTime(limitInfo.startTimeMillis)
                    val end = formatTime(limitInfo.endTimeMillis)
                    "시간대 제한 ($start ~ $end)"
                }
                null -> "제한 없음"
            }

            val textColor = if (isBlocked) Color.Red else Color.Unspecified

            ListItem(
                headlineContent = { Text(appName, color = textColor) },
                supportingContent = { Text(statusText, color = textColor) },
                leadingContent = {
                    Icon(
                        painter = rememberAsyncImagePainter(appIcon),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (limitInfo != null && isBlocked) {
                            if (!justEntered) {
                                onBlockedAppClick(packageName)
                            }
                        } else {
                            onAppSelect(packageName)
                        }
                    }
            )
            Divider()
        }
    }
}


fun formatTime(timeMillis: Long): String {
    if (timeMillis < 0) return "알 수 없음"
    val calendar = java.util.Calendar.getInstance().apply { timeInMillis = timeMillis }
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    val minute = calendar.get(java.util.Calendar.MINUTE)
    return String.format("%02d:%02d", hour, minute)
}
