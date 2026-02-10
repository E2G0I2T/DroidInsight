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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.example.droidinsight.R
import com.example.droidinsight.domain.model.UsageModel
import com.example.droidinsight.presentation.component.UsageBarChart

//앱 사용 통계 화면의 진입점
@Composable
fun UsageScreen(
    viewModel: UsageViewModel = hiltViewModel()
) {
    // 1. 상태 수집
    val usageList by viewModel.uiState.collectAsState()
    val hasPermission by viewModel.hasPermission.collectAsState()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 2. 생명주기 감지
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissionAndLoadData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 초기 실행 시 권한 체크
    LaunchedEffect(Unit) {
        viewModel.checkPermissionAndLoadData()
    }

    // 3. UI 그리기 위임
    UsageContent(
        usageList = usageList,
        hasPermission = hasPermission,
        onPermissionClick = {
            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        },
        formatTime = viewModel::formatTime
    )
}

/**
 * 실제 UI를 그리는 컴포넌트
 */
@Composable
private fun UsageContent(
    usageList: List<UsageModel>,
    hasPermission: Boolean,
    onPermissionClick: () -> Unit,
    formatTime: (Long) -> String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.usage_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (hasPermission) {
            if (usageList.isEmpty()) {
                // 데이터 없음 상태
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(R.string.usage_empty))
                }
            } else {
                // 데이터 목록 표시
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = stringResource(R.string.usage_top5),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        UsageBarChart(usageList = usageList)

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    items(usageList) { app ->
                        UsageItem(
                            app = app,
                            formattedTime = formatTime(app.usageTime)
                        )
                    }
                }
            }
        } else {
            // 권한 없음 상태
            PermissionRequestScreen(onPermissionClick)
        }
    }
}

@Composable
private fun PermissionRequestScreen(onPermissionClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.usage_permission_msg),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onPermissionClick) {
            Text(stringResource(R.string.usage_permission_btn))
        }
    }
}

@Composable
private fun UsageItem(
    app: UsageModel,
    formattedTime: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (app.appIcon != null) {
                    AsyncImage(
                        model = app.appIcon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 앱 이름 & 사용 시간
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 퍼센트 텍스트
                Text(
                    text = "${(app.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 진행률 바
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