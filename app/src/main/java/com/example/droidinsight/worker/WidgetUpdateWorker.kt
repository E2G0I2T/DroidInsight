package com.example.droidinsight.worker

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.droidinsight.presentation.widget.BatteryWidget

/**
 * 백그라운드에서 주기적으로 위젯 UI를 갱신하기 위한 Worker
 * WorkManager에 의해 15분(시스템 최소 주기)마다 실행되어, 앱이 종료된 상태에서도 위젯 정보를 최신화
 */
class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting periodic widget update...")

            // 1. 현재 홈 화면에 추가된 모든 BatteryWidget 인스턴스를 찾음
            // 2. 각 인스턴스의 provideGlance()를 재실행하여 데이터를 갱신
            BatteryWidget().updateAll(context)

            Log.d(TAG, "Widget update completed successfully.")
            Result.success()
        } catch (e: Exception) {
            // 백그라운드 작업 중 에러 발생 시 로그를 남기고 실패 처리
            Log.e(TAG, "Widget update failed", e)
            if (runAttemptCount < 3) {
                // 3번까지 재시도
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "WidgetUpdateWorker"
    }
}