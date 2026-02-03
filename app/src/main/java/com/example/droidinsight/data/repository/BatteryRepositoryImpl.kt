package com.example.droidinsight.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.droidinsight.domain.model.BatteryModel
import com.example.droidinsight.domain.repository.BatteryRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class BatteryRepositoryImpl @Inject constructor(
    private val context: Context
) : BatteryRepository {

    override fun getBatteryData(): Flow<BatteryModel> = callbackFlow {
        // 1. 배터리 상태가 바뀔 때마다 호출될 리시버 정의
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale) else 0

                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f // 섭씨 변환
                val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

                // Health 상태 문자열 변환
                val healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
                val healthString = when(healthInt) {
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                    BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                    else -> "Unknown"
                }

                // 데이터를 Flow로 내보냄
                trySend(
                    BatteryModel(
                        level = batteryPct,
                        isCharging = isCharging,
                        temperature = temp,
                        voltage = voltage,
                        technology = technology,
                        health = healthString
                    )
                )
            }
        }

        // 2. 리시버 등록 (화면 켜질 때)
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        // 3. Flow가 닫힐 때(화면 꺼질 때) 리시버 해제 (메모리 누수 방지)
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }
}