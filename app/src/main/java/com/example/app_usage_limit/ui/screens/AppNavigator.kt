package com.example.app_usage_limit.ui.screens

import android.content.pm.ApplicationInfo
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.*
import com.example.app_usage_limit.BuildConfig
import android.content.Intent


@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val apiKey = BuildConfig.OPENAI_API_KEY

    var selectedPackage by rememberSaveable { mutableStateOf<String?>(null) }
    var subject by rememberSaveable { mutableStateOf("") }
    var justGrantedPermission by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val packageManager = context.packageManager
    val appList = remember { getInstalledUserApps(packageManager) }

    NavHost(navController, startDestination = "permission") {

        composable("permission") {
            AccessibilityPermissionScreen(
                onPermissionGranted = {
                    justGrantedPermission = true
                    navController.navigate("home") {
                        popUpTo("permission") { inclusive = true }
                    }
                }
            )
        }


        composable("home") {
            HomeScreen(
                onAppRestrictionClick = { navController.navigate("applist") },
                onAlarmSettingClick = {  navController.navigate("timer") }
            )
        }


        composable("applist") {
            AppListScreen(
                appList = appList,
                justGrantedPermission = justGrantedPermission,
                onAppSelect = { packageName ->
                    selectedPackage = packageName
                    navController.navigate("limit")
                },
                onBlockedAppClick = { packageName ->
                    if (!justGrantedPermission) {
                        selectedPackage = packageName
                        navController.navigate("subject")
                    }
                    justGrantedPermission = false
                }
            )
        }

        composable("limit") {
            selectedPackage?.let { pkg ->
                SetLimitScreen(
                    packageName = pkg,
                    onLimitSet = {
                        navController.popBackStack("home", false)
                    }
                )
            }
        }

        composable("subject") {
            selectedPackage?.let { pkg ->
                SubjectInputScreen(
                    packageName = pkg,
                    onQuizStart = { inputSubject ->
                        subject = inputSubject
                        navController.navigate("quiz")
                    },
                    onCancel = {
                        navController.popBackStack("home", false)
                    }
                )
            }
        }

        composable("quiz") {
            selectedPackage?.let { pkg ->
                QuizScreen(
                    packageName = pkg,
                    subject = subject,
                    apiKey = apiKey,
                    targetAppPackage = pkg,
                    navController = navController,
                    onCorrect = {
                        navController.navigate("home") {
                            popUpTo("quiz") { inclusive = true }
                        }
                    },
                    onWrong = {
                        // stay on screen
                    },
                    onUnlock = {
                        // optional unlock logic
                    }
                )
            }
        }
        // 타이머 액티비티 호출용 경로 추가
        composable("timer") {
            LaunchedEffect(Unit) {
                val intent = Intent(context, com.example.app_usage_limit.TimerMainActivity::class.java)
                context.startActivity(intent)
            }
        }
    }
}
