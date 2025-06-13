package com.example.app_usage_limit.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.app_usage_limit.CharMain
import com.example.app_usage_limit.R

@Composable
fun HomeScreen(
    onAppRestrictionClick: () -> Unit,
    onAlarmSettingClick: () -> Unit,
    context: Context = LocalContext.current
) {
    // CharMain 초기화
    LaunchedEffect(Unit) {
        CharMain.initialize(context)
    }

    // CharMain에서 상태 가져오기
    val level = CharMain.level.value
    val exp = CharMain.exp.value
    val dailyHour = CharMain.dailyHour.value.coerceAtLeast(1) // 0 방지

    // 경험치 계산
    val base = dailyHour * 0.5 * 3600 * 1000
    val threshold = base * (1 + 0.05 * (level / 10.0))
    val progress = if (threshold > 0) (exp / threshold).toFloat().coerceIn(0f, 1f) else 0f

    // 레벨에 따른 캐릭터 이미지 선택
    val characterRes = when {
        level < 5 -> R.drawable.char1
        level < 10 -> R.drawable.char2
        level < 15 -> R.drawable.char3
        else -> R.drawable.char4
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        // 캐릭터 이미지
        Image(
            painter = painterResource(id = characterRes),
            contentDescription = "캐릭터 이미지",
            modifier = Modifier.size(120.dp)
        )

        Text(text = "Level $level")

        // 경험치 프로그레스 바
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp)
        )

        Text(text = "EXP: ${(exp / 1000).toInt()} / ${(threshold / 1000).toInt()}")

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onAppRestrictionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Text("어플리케이션 제한 설정")
            }

            Button(
                onClick = onAlarmSettingClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Text("환기 알람 설정")
            }

            Button(
                onClick = { CharMain.showAlarmDialog(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Text("폰 사용 시간")
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}