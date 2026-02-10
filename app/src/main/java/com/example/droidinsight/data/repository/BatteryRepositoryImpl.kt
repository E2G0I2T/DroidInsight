package com.example.droidinsight.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.droidinsight.domain.model.BatteryModel
import com.example.droidinsight.domain.repository.BatteryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class BatteryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BatteryRepository {

     // 배터리 상태 변화를 감지하여 Flow로 방출
    override fun getBatteryData(): Flow<BatteryModel> = callbackFlow {
        // 1. 리시버 정의: 상태가 변할 때마다 데이터를 파싱 하여 전송
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val batteryModel = parseBatteryIntent(intent)
                trySend(batteryModel) // 채널로 데이터 방출
            }
        }

        // 2. 리시버 등록
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        // 3. 리소스 정리
        // Flow 수집이 멈추거나 뷰가 파괴되면 실행되어 메모리 누수를 방지
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

     // Intent에서 배터리 정보를 추출하여 도메인 모델로 변환하는 헬퍼 함수
    private fun parseBatteryIntent(intent: Intent): BatteryModel {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale) else 0

        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f // 섭씨 변환
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

        val healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
        val healthString = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            else -> "Unknown"
        }

        return BatteryModel(
            level = batteryPct,
            isCharging = isCharging,
            temperature = temp,
            voltage = voltage,
            technology = technology,
            health = healthString
        )
    }
}