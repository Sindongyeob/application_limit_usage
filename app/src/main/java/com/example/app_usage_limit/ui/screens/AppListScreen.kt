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
    val usageManager = AppUsageManager

    val filteredApps = appList.filter {
        context.packageManager.getApplicationLabel(it).toString().contains(searchQuery, ignoreCase = true)
    }

    val (blockedApps, limitedApps, normalApps) = remember(filteredApps) {
        val now = System.currentTimeMillis()
        filteredApps.partition { usageManager.isAppBlocked(context, it.packageName) }.let { (blocked, rest) ->
            val (limited, normal) = rest.partition { usageManager.getLimitForApp(context, it.packageName) > 0 }
            Triple(blocked, limited, normal)
        }
    }

    Scaffold(
        containerColor = Color.White, // 배경색 흰색
        topBar = {
            TopAppBar(
                title = { Text("앱 사용 제한 목록", color = Color.Black) }, // 글자색 검정
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White // TopAppBar 배경색 흰색
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color.White) // Column 배경색 흰색
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("앱 검색", color = Color.Black) }, // 라벨 색상 검정
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
                    .background(Color.White) // LazyColumn 배경색 흰색
            ) {
                if (blockedApps.isNotEmpty()) {
                    item { SectionHeader("차단된 앱", color = Color.Black) } // 글자색 검정
                    items(blockedApps) { app ->
                        AppRow(app, context, Color.Black) { // textColor 검정
                            onBlockedAppClick(app.packageName)
                        }
                    }
                }

                if (limitedApps.isNotEmpty()) {
                    item { SectionHeader("제한된 앱", color = Color.Black) } // 글자색 검정
                    items(limitedApps) { app ->
                        val remaining = usageManager.getRemainingTime(context, app.packageName)
                        AppRow(app, context, Color.Black) { // textColor 검정
                            onAppSelect(app.packageName)
                        }
                    }
                }

                if (normalApps.isNotEmpty()) {
                    item { SectionHeader("일반 앱", color = Color.Black) } // 글자색 검정
                    items(normalApps) { app ->
                        AppRow(app, context, Color.Black) { // textColor 검정
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
                Text(it, fontSize = 12.sp, color = Color.Black) // subText 색상 검정
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