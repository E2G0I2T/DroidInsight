package com.example.droidinsight

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.droidinsight.presentation.MainScreen
import com.example.droidinsight.presentation.widget.BatteryWidget
import com.example.droidinsight.ui.theme.DroidInsightTheme
import com.example.droidinsight.worker.WidgetUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 백그라운드 워커 등록 (15분마다 위젯 갱신)
        setupPeriodicWidgetUpdates()

        // 2. 앱 실행 시 위젯 즉시 갱신
        refreshWidgetOnAppStart()

        // 3. UI 설정
        setContent {
            DroidInsightTheme {
                MainScreen()
            }
        }
    }

    /**
     * WorkManager를 사용하여 15분마다 위젯을 업데이트하는 작업을 예약
     * ExistingPeriodicWorkPolicy.KEEP: 이미 예약된 작업이 있다면 덮어쓰지 않고 유지
     */
    private fun setupPeriodicWidgetUpdates() {
        val workRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "widget_update_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * 앱이 실행될 때 위젯 데이터를 최신 상태로 동기화
     * 실패하더라도 앱 실행에는 지장을 주지 않도록 예외 처리합
     */
    private fun refreshWidgetOnAppStart() {
        lifecycleScope.launch {
            try {
                BatteryWidget().updateAll(applicationContext)
                Log.d("MainActivity", "Widget update requested on app start.")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to update widget on app start", e)
            }
        }
    }
}