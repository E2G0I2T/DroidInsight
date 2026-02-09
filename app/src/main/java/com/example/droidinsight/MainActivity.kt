package com.example.droidinsight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.glance.appwidget.updateAll // [추가] 위젯 강제 업데이트 함수
import androidx.lifecycle.lifecycleScope // [추가] 비동기 실행을 위한 스코프
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.droidinsight.presentation.MainScreen
import com.example.droidinsight.presentation.widget.BatteryWidget // [추가] 위젯 클래스 임포트
import com.example.droidinsight.ui.theme.DroidInsightTheme
import com.example.droidinsight.worker.WidgetUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch // [추가] 코루틴
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupWidgetWorker()

        // [수정] 안전하게 위젯 업데이트 요청하기
        lifecycleScope.launch {
            try {
                // 1. 앱 켜지고 1초만 기다려주기 (시스템 준비 시간)
                kotlinx.coroutines.delay(1000)

                // 2. 위젯 업데이트 시도
                BatteryWidget().updateAll(applicationContext)

            } catch (e: Exception) {
                // 3. 실패해도 조용히 넘어감 (앱 튕김 방지)
                e.printStackTrace()
            }
        }

        setContent {
            DroidInsightTheme {
                MainScreen()
            }
        }
    }

    // ... (setupWidgetWorker 함수는 그대로 둠) ...
    private fun setupWidgetWorker() {
        val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "widget_update_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}