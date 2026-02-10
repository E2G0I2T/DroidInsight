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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.droidinsight.R
import com.example.droidinsight.domain.model.BatteryModel
import com.example.droidinsight.domain.model.SystemInfo

/**
 * 대시보드 화면의 진입점
 * ViewModel에서 데이터를 수집하여 Stateless 컴포넌트인 DashboardContent에 전달
 */
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val batteryInfo by viewModel.batteryState.collectAsState()
    val systemInfo by viewModel.systemInfo.collectAsState()

    DashboardContent(
        batteryInfo = batteryInfo,
        systemInfo = systemInfo,
        formatSize = viewModel::formatSize
    )
}

/**
 * 실제 UI를 그리는 컴포넌트
 * ViewModel 의존성이 없으므로 Preview 및 테스트가 용이
 */
@Composable
private fun DashboardContent(
    batteryInfo: BatteryModel,
    systemInfo: SystemInfo,
    formatSize: (Long) -> String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. 기기 기본 정보 섹션
        InfoCard(title = stringResource(R.string.dashboard_device_specs)) {
            SpecRow(stringResource(R.string.spec_model), systemInfo.modelName)
            SpecRow(stringResource(R.string.spec_manufacturer), systemInfo.manufacturer)
            SpecRow(stringResource(R.string.spec_os), systemInfo.androidVersion)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 배터리 정보 섹션
        InfoCard(title = stringResource(R.string.dashboard_battery_status)) {
            val chargingText = if (batteryInfo.isCharging) stringResource(R.string.battery_charging)
            else stringResource(R.string.battery_discharging)
            val chargingColor = if (batteryInfo.isCharging) Color.Green
            else MaterialTheme.colorScheme.onSurface

            SpecRow(stringResource(R.string.spec_level), "${batteryInfo.level}%")
            SpecRow(stringResource(R.string.spec_status), chargingText, valueColor = chargingColor)
            SpecRow(stringResource(R.string.spec_temp), "${batteryInfo.temperature} °C")
            SpecRow(stringResource(R.string.spec_voltage), "${batteryInfo.voltage} mV")
            SpecRow(stringResource(R.string.spec_health), batteryInfo.health)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 시스템 리소스 섹션
        InfoCard(title = stringResource(R.string.dashboard_system_resources)) {
            // RAM Usage
            val usedRam = systemInfo.totalRam - systemInfo.availableRam
            ResourceBar(
                label = stringResource(R.string.resource_ram),
                usagePercent = systemInfo.ramUsagePercent,
                usageText = "${formatSize(usedRam)} / ${formatSize(systemInfo.totalRam)}"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Storage Usage
            val usedStorage = systemInfo.totalStorage - systemInfo.availableStorage
            ResourceBar(
                label = stringResource(R.string.resource_storage),
                usagePercent = systemInfo.storageUsagePercent,
                usageText = "${formatSize(usedStorage)} / ${formatSize(systemInfo.totalStorage)}"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun InfoCard(
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

@Composable
private fun SpecRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun ResourceBar(
    label: String,
    usagePercent: Float,
    usageText: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = usagePercent,
        label = "ResourceProgress"
    )

    // 85% 이상 사용 시 경고색(빨강), 아니면 기본색(파랑)
    val barColor = if (usagePercent > 0.85f) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.primary

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text(text = usageText, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(barColor)
            )
        }
    }
}