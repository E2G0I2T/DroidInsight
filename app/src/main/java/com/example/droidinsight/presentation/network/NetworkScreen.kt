package com.example.droidinsight.presentation.network

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.droidinsight.presentation.component.NetworkLineChart
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun NetworkScreen(
    viewModel: NetworkViewModel = hiltViewModel()
) {
    // ViewModel 상태 구독
    val currentDownload by viewModel.currentDownloadSpeed.collectAsState()
    val currentUpload by viewModel.currentUploadSpeed.collectAsState()
    val downloadHistory by viewModel.downloadHistory.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    // [추가] 속도 측정 관련 상태 구독
    val isTesting by viewModel.isTesting.collectAsState()
    val maxSpeed by viewModel.maxSpeed.collectAsState()
    val avgSpeed by viewModel.avgSpeed.collectAsState()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            // 화면이 숨겨지거나(STOP), 파괴될 때(DESTROY) -> 테스트 중단
            if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_DESTROY) {
                // 뷰모델에 stopTest()가 public이어야 함 (현재는 private일 수 있음 -> public으로 변경 필요)
                viewModel.stopTest()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Network Monitor",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. 실시간 그래프
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Real-time Download Traffic",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                NetworkLineChart(
                    dataPoints = downloadHistory,
                    lineColor = Color(0xFF00E676)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. 현재 속도 카드들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CompactSpeedCard(
                title = "Download",
                speed = viewModel.formatSpeed(currentDownload),
                icon = Icons.Default.KeyboardArrowDown,
                color = Color(0xFF00E676),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            CompactSpeedCard(
                title = "Upload",
                speed = viewModel.formatSpeed(currentUpload),
                icon = Icons.Default.KeyboardArrowUp,
                color = Color(0xFF2979FF),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. [추가] 속도 측정(Benchmark) 섹션
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Network Benchmark", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("MAX", style = MaterialTheme.typography.labelSmall)
                        Text(viewModel.formatSpeed(maxSpeed), style = MaterialTheme.typography.titleMedium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("AVG", style = MaterialTheme.typography.labelSmall)
                        Text(viewModel.formatSpeed(avgSpeed), style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.toggleTest() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTesting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (isTesting) "Stop Test" else "Start Speed Test")
                }
            }
        }
    }
}

@Composable
fun CompactSpeedCard(
    title: String,
    speed: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelSmall)
            Text(
                text = speed,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}