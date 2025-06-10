package com.example.app_usage_limit.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.app_usage_limit.R


@Composable
fun HomeScreen(
    onAppRestrictionClick: () -> Unit,
    onAlarmSettingClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        // 캐릭터 이미지 (리소스 ID는 실제 프로젝트에 맞게 변경 필요)
//        Image(
//            painter = painterResource(id = R.drawable.ic_character),
//            contentDescription = "캐릭터 이미지",
//            modifier = Modifier.size(120.dp)
//        )

        // 프로그레스 바 (89%로 설정)
        LinearProgressIndicator(
            progress = 0.89f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // 버튼 영역
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onAppRestrictionClick,
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
            ) {
                Text("어플리케이션 제한 설정")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onAlarmSettingClick,
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
            ) {
                Text("환기 알람 설정")
            }
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}
