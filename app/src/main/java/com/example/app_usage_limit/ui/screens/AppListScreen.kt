package com.example.app_usage_limit.ui.screens

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_usage_limit.R
import com.example.app_usage_limit.util.AppUsageManager
import androidx.core.graphics.createBitmap
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    appList: List<ApplicationInfo>,
    justGrantedPermission: Boolean,
    onAppSelect: (String) -> Unit,
    onBlockedAppClick: (String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf(appList) } // 앱 목록을 상태로 관리
    val usageManager = AppUsageManager

    // 앱 목록 갱신 함수
    fun refreshAppList() {
        apps = context.packageManager.getInstalledApplications(0)
    }

    // 검색된 앱 목록
    val filteredApps = apps.filter {
        context.packageManager.getApplicationLabel(it).toString().contains(searchQuery, ignoreCase = true)
    }

    // 앱 분류
    val (blockedApps, limitedApps, normalApps) = remember(filteredApps) {
        val now = System.currentTimeMillis()
        filteredApps.partition { usageManager.isAppBlocked(context, it.packageName) }.let { (blocked, rest) ->
            val (limited, normal) = rest.partition { usageManager.getLimitForApp(context, it.packageName) > 0 }
            Triple(blocked, limited, normal)
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("앱 사용 제한 목록", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("앱 검색", color = Color.Black) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                if (blockedApps.isNotEmpty()) {
                    item { SectionHeader("차단된 앱", color = Color.Black) }
                    items(blockedApps) { app ->
                        AppRow(app, context, Color.Black) {
                            usageManager.resetLimits(context, app.packageName)
                            Toast.makeText(context, "${app.loadLabel(context.packageManager)} 제한 해제됨", Toast.LENGTH_SHORT).show()
                            refreshAppList() // 목록 갱신
                        }
                    }
                }

                if (limitedApps.isNotEmpty()) {
                    item { SectionHeader("제한된 앱", color = Color.Black) }
                    items(limitedApps) { app ->
                        AppRow(app, context, Color.Black) {
                            usageManager.resetLimits(context, app.packageName)
                            Toast.makeText(context, "${app.loadLabel(context.packageManager)} 제한 해제됨", Toast.LENGTH_SHORT).show()
                            refreshAppList() // 목록 갱신
                        }
                    }
                }

                if (normalApps.isNotEmpty()) {
                    item { SectionHeader("일반 앱", color = Color.Black) }
                    items(normalApps) { app ->
                        AppRow(app, context, Color.Black) {
                            onAppSelect(app.packageName)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color = Color.Black) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun AppRow(
    app: ApplicationInfo,
    context: android.content.Context,
    textColor: Color,
    subText: String? = null,
    onClick: () -> Unit
) {
    val icon = remember(app.packageName) {
        try {
            context.packageManager.getApplicationIcon(app).apply {
                setBounds(0, 0, 48, 48)
            }
        } catch (e: Exception) {
            context.getDrawable(R.drawable.ic_launcher_foreground)
        }
    }
    val appName = context.packageManager.getApplicationLabel(app).toString()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        icon?.let {
            Image(
                painter = remember(icon) { BitmapPainter(it.toBitmap().asImageBitmap()) },
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(appName, color = textColor, fontSize = 16.sp)
            subText?.let {
                Text(it, fontSize = 12.sp, color = Color.Black)
            }
        }
    }
}

// 확장 함수: Drawable -> Bitmap
fun android.graphics.drawable.Drawable.toBitmap(): android.graphics.Bitmap {
    if (this is android.graphics.drawable.BitmapDrawable) {
        return bitmap
    }
    val bitmap =
        createBitmap(intrinsicWidth.takeIf { it > 0 } ?: 1, intrinsicHeight.takeIf { it > 0 } ?: 1)
    val canvas = android.graphics.Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}