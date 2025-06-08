package com.example.app_usage_limit.ui.screens

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.ExperimentalMaterial3Api

data class AppInfo(
    val packageName: String,
    val appName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledAppsScreen(navController: NavController, context: Context) {
    val packageManager = context.packageManager

    // 앱 목록 가져오기
    val installedApps = remember {
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { appInfo ->
                // 사용자 앱만 필터링 (시스템 앱 제외)
                (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
            }
            .map {
                AppInfo(
                    packageName = it.packageName,
                    appName = packageManager.getApplicationLabel(it).toString()
                )
            }
            .sortedBy { it.appName }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("앱 선택") })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(installedApps) { app ->
                AppItem(app = app) {
                    // 클릭 시 제한 시간 설정 화면으로 이동, 패키지명과 앱 이름 전달
                    navController.navigate("set_limit/${app.packageName}/${app.appName}")
                }
            }
        }
    }
}

@Composable
fun AppItem(app: AppInfo, onClick: () -> Unit) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onClick() }
        .padding(16.dp)
    ) {
        Text(text = app.appName, style = MaterialTheme.typography.bodyLarge)
        Text(text = app.packageName, style = MaterialTheme.typography.bodySmall)
    }
}
