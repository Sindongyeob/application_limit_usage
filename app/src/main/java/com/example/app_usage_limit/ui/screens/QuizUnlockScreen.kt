package com.example.app_usage_limit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun QuizUnlockScreen(onQuizPassed: () -> Unit) {
    var userAnswer by remember { mutableStateOf("") }
    val correctAnswer = "42"  // 예시 정답, 실제는 QuizManager에서 받아와야함
    var message by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column {
            Text(text = "퀴즈를 풀어주세요!")
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "질문: 6 x 7 = ?")
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = userAnswer,
                onValueChange = { userAnswer = it },
                label = { Text("정답 입력") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if (userAnswer.trim() == correctAnswer) {
                    message = "정답입니다!"
                    onQuizPassed()
                } else {
                    message = "틀렸습니다. 다시 시도하세요."
                }
            }) {
                Text("제출")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(message)
        }
    }
}
