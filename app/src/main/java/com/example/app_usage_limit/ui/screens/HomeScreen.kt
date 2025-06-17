package com.example.app_usage_limit.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import com.example.app_usage_limit.CharMain
import com.example.app_usage_limit.R
import androidx.compose.ui.text.style.TextAlign

@Composable
fun HomeScreen(
    onAppRestrictionClick: () -> Unit,
    onAlarmSettingClick: () -> Unit,
    context: Context = LocalContext.current
) {
    LaunchedEffect(Unit) {
        CharMain.initialize(context)
    }

    val level = CharMain.level.value
    val exp = CharMain.exp.value
    val dailyHour = CharMain.dailyHour.value.coerceAtLeast(1)
    val base = dailyHour * 0.5 * 3600 * 1000
    val threshold = base * (1 + 0.05 * (level / 10.0))
    val progress = if (threshold > 0) (exp / threshold).toFloat().coerceIn(0f, 1f) else 0f

    val characterRes = when {
        level < 5 -> R.drawable.char1
        level < 10 -> R.drawable.char2
        level < 15 -> R.drawable.char3
        else -> R.drawable.char4
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White.copy(alpha = 0.5f))
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AUL (App Usage Limit)",
                fontSize = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp),
                color = Color.Black
            )

            Spacer(modifier = Modifier.weight(0.5f))

            Image(
                painter = painterResource(id = characterRes),
                contentDescription = "캐릭터 이미지",
                modifier = Modifier.size(256.dp)
            )

            Text(
                text = "레벨: $level",
                color = Color.Black,
                fontSize = 18.sp,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .background(
                        color = Color.LightGray,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .padding(horizontal = 32.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray)
            )

            Text(
                text = "EXP: ${(exp / 1000).toInt()} / ${(threshold / 1000).toInt()}",
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    onClick = onAppRestrictionClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(126.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_b1),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "어플\n제한",
                            fontSize = 20.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Surface(
                    onClick = onAlarmSettingClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(126.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_b2),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "하루 사용 시간",
                            fontSize = 20.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Surface(
                    onClick = { CharMain.showAlarmDialog(context) },
                    modifier = Modifier
                        .weight(1f)
                        .height(126.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.icon_b3),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "폰 사용 시간\n확인",
                            fontSize = 20.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.4f))
        }
    }
}