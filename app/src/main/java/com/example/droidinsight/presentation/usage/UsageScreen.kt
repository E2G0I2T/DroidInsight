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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

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
            // [상태 A] 권한 있음: 리스트 출력
            if (usageList.isEmpty()) {
                Text("집계된 사용 기록이 없습니다. (조금만 기다려주세요)")
            } else {
                LazyColumn {
                    items(usageList) { app ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 아이콘 (라이브러리 없이 기본 아이콘 사용)
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(app.appName, style = MaterialTheme.typography.titleMedium)
                                Text(viewModel.formatTime(app.usageTime), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        } else {
            // [상태 B] 권한 없음: 버튼 출력
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