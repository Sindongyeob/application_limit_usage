package com.example.app_usage_limit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UsageLimitScreen(
    currentLimitMinutes: Int?,
    onLimitChange: (Int) -> Unit,
    onSaveLimit: () -> Unit
) {
    var input by remember { mutableStateOf(currentLimitMinutes?.toString() ?: "") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "앱 사용 제한 시간 설정 (분)", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = input,
            onValueChange = {
                input = it.filter { ch -> ch.isDigit() }
                error = input.isEmpty()
            },
            label = { Text("제한 시간 (분)") },
            isError = error,
            modifier = Modifier.fillMaxWidth()
        )

        if (error) {
            Text(text = "숫자를 입력해주세요", color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                val minutes = input.toIntOrNull()
                if (minutes != null && minutes > 0) {
                    onLimitChange(minutes)
                    onSaveLimit()
                } else {
                    error = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("저장")
        }
    }
}
