package com.example.app_usage_limit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

    var showSuccessDialog by remember { mutableStateOf(false) }

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

    // 화면 정중앙에 배치하기 위해 Box 사용
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center // 중앙 정렬
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(), // Column은 최대 너비를 차지하도록
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Column 내부 요소도 수평 중앙 정렬
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
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("정답입니다!") },
            text = { Text("앱 사용이 해제되었습니다. 해당 앱을 실행하거나 홈으로 돌아갈 수 있습니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false

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