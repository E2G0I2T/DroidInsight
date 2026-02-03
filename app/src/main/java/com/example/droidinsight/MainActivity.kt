package com.example.droidinsight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.droidinsight.presentation.usage.UsageScreen // 여기 임포트 확인!
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 프로젝트 생성 시 기본 테마 이름 (DroidInsightTheme가 없으면 MaterialTheme로 감싸도 됨)
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // [중요] 여기를 DashboardScreen()에서 UsageScreen()으로 바꿔야 합니다!
                    UsageScreen()
                }
            }
        }
    }
}