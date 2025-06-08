package com.example.app_usage_limit.ui.screens

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appList: List<ApplicationInfo>,
    justGrantedPermission: Boolean,
    onAppSelect: (String) -> Unit,
    onBlockedAppClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("앱 사용 제한 목록") }
            )
        }
    ) { innerPadding ->
        AppListContent(
            appList = appList,
            justEntered = justGrantedPermission,
            onAppSelect = onAppSelect,
            onBlockedAppClick = onBlockedAppClick,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
