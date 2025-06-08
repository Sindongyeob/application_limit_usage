package com.example.app_usage_limit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.app_usage_limit.quiz.QuizManager
import kotlinx.coroutines.launch

@Composable
fun QuizScreen(
    packageName: String,
    subject: String,
    apiKey: String,
    onCorrect: () -> Unit,
    onWrong: () -> Unit,
    onUnlock: () -> Unit,
    targetAppPackage: String,
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var question by remember { mutableStateOf("퀴즈를 불러오는 중...") }
    var correctAnswer by remember { mutableStateOf("") }
    var userAnswer by remember { mutableStateOf("") }
    var showResult by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ✅ 정답 다이얼로그 표시 여부
    var showSuccessDialog by remember { mutableStateOf(false) }

    // 퀴즈 로딩
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val (q, a) = QuizManager.generateQuiz(apiKey, subject)
            question = q
            correctAnswer = a
        } catch (e: Exception) {
            errorMessage = "퀴즈 로딩 실패: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("퀴즈", style = MaterialTheme.typography.headlineMedium)

        if (isLoading) {
            CircularProgressIndicator()
            Text("문제를 불러오는 중입니다...")
        } else if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        } else {
            Text(question)

            OutlinedTextField(
                value = userAnswer,
                onValueChange = { userAnswer = it },
                label = { Text("정답 입력") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    isCorrect = userAnswer.trim().equals(correctAnswer.trim(), ignoreCase = true)
                    showResult = true

                    if (isCorrect) {
                        onCorrect()
                        onUnlock()
                        showSuccessDialog = true
                    } else {
                        onWrong()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("제출")
            }

            if (showResult && !isCorrect) {
                Text("오답입니다. 정답은: $correctAnswer", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    // ✅ 정답 시 다이얼로그
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("정답입니다!") },
            text = { Text("앱 사용이 해제되었습니다. 해당 앱을 실행하거나 홈으로 돌아갈 수 있습니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false

                    // ✅ 자동 실행 기능
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(targetAppPackage)
                    if (launchIntent != null) {
                        context.startActivity(launchIntent)
                    } else {
                        navController.navigate("home") {
                            popUpTo("quiz") { inclusive = true }
                        }
                    }
                }) {
                    Text("앱 실행")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    navController.navigate("home") {
                        popUpTo("quiz") { inclusive = true }
                    }
                }) {
                    Text("홈으로")
                }
            }
        )
    }
}

