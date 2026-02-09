package com.example.droidinsight.presentation.usage

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // [필수] Color 에러 해결
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage // [필수] AsyncImage 에러 해결
import com.example.droidinsight.domain.model.UsageModel
import com.example.droidinsight.presentation.component.UsageBarChart

@Composable
fun UsageScreen(
    viewModel: UsageViewModel = hiltViewModel()
) {
    // ViewModel 상태 구독
    val usageList by viewModel.uiState.collectAsState()
    val hasPermission by viewModel.hasPermission.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 화면이 다시 보일 때(Resume) 권한 체크
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissionAndLoadData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 초기 실행 시 권한 체크 1회 실행
    LaunchedEffect(Unit) {
        viewModel.checkPermissionAndLoadData()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("오늘의 앱 사용 기록", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (hasPermission) {
            if (usageList.isEmpty()) {
                Text("집계된 사용 기록이 없습니다.")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 0번째 아이템으로 차트 넣기
                    item {
                        Text(
                            text = "Top 5 사용량 비교",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // 우리가 만든 커스텀 차트
                        UsageBarChart(usageList = usageList)

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider() // 구분선
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 리스트 아이템들
                    items(usageList) { app ->
                        UsageItem(app = app, viewModel = viewModel)
                    }
                }
            }
        } else {
            // 권한 없을 때 화면
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "앱 사용 정보를 보려면\n권한이 필요합니다.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {
                    context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                }) {
                    Text("권한 허용하러 가기")
                }
            }
        }
    }
}

@Composable
fun UsageItem(app: UsageModel, viewModel: UsageViewModel) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // [수정된 부분] 아이콘 표시 로직
                if (app.appIcon != null) {
                    // 1. 실제 앱 아이콘이 있으면 Coil로 표시
                    AsyncImage(
                        model = app.appIcon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    // 2. 없으면 기본 안드로이드 아이콘 표시
                    Icon(
                        imageVector = Icons.Default.Info, // 기본 아이콘(i 모양)으로 변경
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 앱 이름 & 시간
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName, // 진짜 이름
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1
                    )
                    Text(
                        text = viewModel.formatTime(app.usageTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 퍼센트 텍스트
                Text(
                    text = "${(app.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 사용량 그래프 (Progress Bar)
            LinearProgressIndicator(
                progress = { app.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            )
        }
    }
}