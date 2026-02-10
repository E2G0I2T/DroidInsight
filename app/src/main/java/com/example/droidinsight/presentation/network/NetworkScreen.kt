package com.example.droidinsight.presentation.network

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.droidinsight.R
import com.example.droidinsight.presentation.component.NetworkLineChart

/**
 * 네트워크 화면의 진입점
 * ViewModel과 생명주기를 관리하고, 데이터를 수집하여 Stateless 컴포넌트에 전달
 */
@Composable
fun NetworkScreen(
    viewModel: NetworkViewModel = hiltViewModel()
) {
    // 1. 상태 수집
    val currentDownload by viewModel.currentDownloadSpeed.collectAsState()
    val currentUpload by viewModel.currentUploadSpeed.collectAsState()
    val downloadHistory by viewModel.downloadHistory.collectAsState()

    val isTesting by viewModel.isTesting.collectAsState()
    val maxSpeed by viewModel.maxSpeed.collectAsState()
    val avgSpeed by viewModel.avgSpeed.collectAsState()

    // 2. 다이얼로그 상태 관리
    var showWarningDialog by remember { mutableStateOf(false) }

    // 3. 생명주기 감지 (화면 벗어나면 테스트 중단)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_DESTROY) {
                viewModel.stopTest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 4. UI 그리기 위임
    NetworkContent(
        downloadSpeed = currentDownload,
        uploadSpeed = currentUpload,
        downloadHistory = downloadHistory,
        isTesting = isTesting,
        maxSpeed = maxSpeed,
        avgSpeed = avgSpeed,
        showDialog = showWarningDialog,
        onToggleTest = viewModel::toggleTest,
        onDialogDismiss = { showWarningDialog = false },
        onShowDialog = { showWarningDialog = true },
        formatSpeed = viewModel::formatSpeed
    )
}

/**
 * 실제 UI를 그리는 컴포넌트
 * ViewModel 의존성이 없어서 Preview 및 재사용이 가능
 */
@Composable
private fun NetworkContent(
    downloadSpeed: Long,
    uploadSpeed: Long,
    downloadHistory: List<Long>,
    isTesting: Boolean,
    maxSpeed: Long,
    avgSpeed: Long,
    showDialog: Boolean,
    onToggleTest: () -> Unit,
    onDialogDismiss: () -> Unit,
    onShowDialog: () -> Unit,
    formatSpeed: (Long) -> String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.network_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. 실시간 그래프 카드
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

        // 2. 현재 속도 표시 (다운로드/업로드)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CompactSpeedCard(
                title = stringResource(R.string.network_download),
                speed = formatSpeed(downloadSpeed),
                icon = Icons.Default.KeyboardArrowDown,
                color = Color(0xFF00E676),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            CompactSpeedCard(
                title = stringResource(R.string.network_upload),
                speed = formatSpeed(uploadSpeed),
                icon = Icons.Default.KeyboardArrowUp,
                color = Color(0xFF2979FF),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. 벤치마크 컨트롤러
        BenchmarkCard(
            maxSpeed = formatSpeed(maxSpeed),
            avgSpeed = formatSpeed(avgSpeed),
            isTesting = isTesting,
            onActionClick = {
                if (isTesting) onToggleTest() else onShowDialog()
            }
        )
    }

    // 경고 다이얼로그
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDialogDismiss,
            title = { Text(text = stringResource(R.string.dialog_warning_title)) },
            text = { Text(text = stringResource(R.string.dialog_warning_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDialogDismiss()
                        onToggleTest()
                    }
                ) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDialogDismiss) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun BenchmarkCard(
    maxSpeed: String,
    avgSpeed: String,
    isTesting: Boolean,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.network_benchmark),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SpeedStatItem(label = stringResource(R.string.network_max), value = maxSpeed)
                SpeedStatItem(label = stringResource(R.string.network_avg), value = avgSpeed)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTesting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    if (isTesting) stringResource(R.string.btn_stop_test)
                    else stringResource(R.string.btn_start_test)
                )
            }
        }
    }
}

@Composable
private fun SpeedStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun CompactSpeedCard(
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