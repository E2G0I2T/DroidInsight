package com.example.droidinsight.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.droidinsight.presentation.widget.BatteryWidget

class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // 1. BatteryWidget의 모든 인스턴스를 찾아서 강제로 업데이트(provideGlance 재실행)
        BatteryWidget().updateAll(context)

        // 2. 성공적으로 갱신했음을 시스템에 알림
        return Result.success()
    }
}