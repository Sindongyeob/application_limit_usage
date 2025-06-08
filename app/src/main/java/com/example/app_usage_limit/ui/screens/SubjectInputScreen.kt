package com.example.app_usage_limit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SubjectInputScreen(
    packageName: String,
    onQuizStart: (String) -> Unit,
    onCancel: () -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("퀴즈 주제를 입력하세요", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = subject,
            onValueChange = {
                subject = it
                showError = false
            },
            label = { Text("예: 역사, 과학, 스포츠") },
            singleLine = true,
            isError = showError
        )

        if (showError) {
            Text("주제를 입력해주세요.", color = MaterialTheme.colorScheme.error)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                if (subject.isNotBlank()) {
                    onQuizStart(subject)
                } else {
                    showError = true
                }
            }) {
                Text("퀴즈 시작")
            }

            OutlinedButton(onClick = onCancel) {
                Text("취소")
            }
        }
    }
}
