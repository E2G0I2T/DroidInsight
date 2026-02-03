package com.example.droidinsight.presentation.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    // ViewModel의 데이터를 실시간으로 관찰 (State 구독)
    val batteryInfo by viewModel.batteryState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Droid Insight Dashboard",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 배터리 정보 출력
        Text(text = "배터리 잔량: ${batteryInfo.level}%")
        val chargingText = if (batteryInfo.isCharging) {
            "충전 연결됨 (⚡)"
        } else {
            "충전 안 됨 (배터리 사용 중)"
        }

        Text(text = "충전 상태: $chargingText")
        Text(text = "온도: ${batteryInfo.temperature} °C")
        Text(text = "전압: ${batteryInfo.voltage} mV")
        Text(text = "건강 상태: ${batteryInfo.health}")
        Text(text = "기술: ${batteryInfo.technology}")
    }
}