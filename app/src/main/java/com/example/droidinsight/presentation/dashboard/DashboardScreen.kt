package com.example.droidinsight.presentation.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.droidinsight.domain.model.SystemInfo

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val batteryInfo by viewModel.batteryState.collectAsState()
    val systemInfo by viewModel.systemInfo.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // 스크롤 가능
    ) {
        Text(
            text = "Droid Insight",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. 기기 기본 정보
        InfoCard(title = "Device Specs") {
            SpecRow("Model", systemInfo.modelName)
            SpecRow("Manufacturer", systemInfo.manufacturer)
            SpecRow("OS Version", systemInfo.androidVersion)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 배터리 정보
        InfoCard(title = "Battery Status") {
            val chargingText = if (batteryInfo.isCharging) "Charging ⚡" else "Discharging"
            val chargingColor = if (batteryInfo.isCharging) Color.Green else MaterialTheme.colorScheme.onSurface

            SpecRow("Level", "${batteryInfo.level}%")
            SpecRow("Status", chargingText, valueColor = chargingColor)
            SpecRow("Temp", "${batteryInfo.temperature} °C")
            SpecRow("Voltage", "${batteryInfo.voltage} mV")
            SpecRow("Health", batteryInfo.health)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 시스템 리소스 (RAM & Storage)
        InfoCard(title = "System Resources") {
            // RAM Usage
            ResourceBar(
                label = "RAM",
                usagePercent = systemInfo.ramUsagePercent,
                usageText = "${viewModel.formatSize(systemInfo.totalRam - systemInfo.availableRam)} / ${viewModel.formatSize(systemInfo.totalRam)}"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Storage Usage
            ResourceBar(
                label = "Internal Storage",
                usagePercent = systemInfo.storageUsagePercent,
                usageText = "${viewModel.formatSize(systemInfo.totalStorage - systemInfo.availableStorage)} / ${viewModel.formatSize(systemInfo.totalStorage)}"
            )
        }

        // 하단 여백 확보
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// [공통 컴포넌트] 카드 UI
@Composable
fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

// [공통 컴포넌트] 텍스트 한 줄
@Composable
fun SpecRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

// [공통 컴포넌트] 리소스 게이지 바
@Composable
fun ResourceBar(label: String, usagePercent: Float, usageText: String) {
    val animatedProgress by animateFloatAsState(targetValue = usagePercent, label = "progress")

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text(text = usageText, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(8.dp))

        // 프로그레스 바 배경
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        ) {
            // 실제 사용량 (채워지는 부분)
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        if (usagePercent > 0.85f) MaterialTheme.colorScheme.error // 85% 넘으면 빨간색
                        else MaterialTheme.colorScheme.primary
                    )
            )
        }
    }
}