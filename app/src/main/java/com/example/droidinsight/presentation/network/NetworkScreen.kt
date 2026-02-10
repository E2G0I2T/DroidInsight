package com.example.droidinsight.presentation.network

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.* // [필수] getValue, setValue, remember 등을 위해 * 사용
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource // [필수] stringResource 에러 해결
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.droidinsight.R // [필수] R 클래스 임포트 (패키지명 확인하세요!)
import com.example.droidinsight.presentation.component.NetworkLineChart

@Composable
fun NetworkScreen(
    viewModel: NetworkViewModel = hiltViewModel()
) {
    // ViewModel 상태 구독
    val currentDownload by viewModel.currentDownloadSpeed.collectAsState()
    val currentUpload by viewModel.currentUploadSpeed.collectAsState()
    val downloadHistory by viewModel.downloadHistory.collectAsState()

    val isTesting by viewModel.isTesting.collectAsState()
    val maxSpeed by viewModel.maxSpeed.collectAsState()
    val avgSpeed by viewModel.avgSpeed.collectAsState()

    // [추가] 다이얼로그 표시 여부 상태 (by remember 에러 해결됨)
    var showWarningDialog by remember { mutableStateOf(false) }

    // 생명주기 처리 (앱이 백그라운드로 가면 측정 중지)
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP || event == androidx.lifecycle.Lifecycle.Event.ON_DESTROY) {
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
        // [수정] 문자열 리소스 사용
        Text(
            text = stringResource(R.string.network_title),
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
                    text = stringResource(R.string.network_realtime_traffic),
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

        // 3. 속도 측정(Benchmark) 섹션
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.network_benchmark), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.network_max), style = MaterialTheme.typography.labelSmall)
                        Text(viewModel.formatSpeed(maxSpeed), style = MaterialTheme.typography.titleMedium)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.network_avg), style = MaterialTheme.typography.labelSmall)
                        Text(viewModel.formatSpeed(avgSpeed), style = MaterialTheme.typography.titleMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // [수정] 다이얼로그 로직 적용된 버튼
                Button(
                    onClick = {
                        if (isTesting) {
                            viewModel.toggleTest()
                        } else {
                            showWarningDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTesting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    // [수정] 문자열 리소스 사용
                    Text(if (isTesting) stringResource(R.string.btn_stop_test) else stringResource(R.string.btn_start_test))
                }
            }
        }
    }

    // [추가] 경고 다이얼로그
    if (showWarningDialog) {
        AlertDialog(
            onDismissRequest = { showWarningDialog = false },
            title = { Text(text = "데이터 사용 경고") },
            text = { Text(text = "속도 측정을 위해 약 100MB의 데이터를 다운로드합니다.\n데이터 요금이 부과될 수 있습니다.\n계속하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWarningDialog = false
                        viewModel.toggleTest() // 진짜 시작
                    }
                ) {
                    Text("시작")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWarningDialog = false }) {
                    Text("취소")
                }
            }
        )
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