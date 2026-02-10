package com.example.droidinsight.domain.model

/**
 * @property level 잔량 퍼센트 (0~100)
 * @property isCharging 충전 케이블 연결 여부
 * @property temperature 배터리 온도 (섭씨, °C)
 * @property voltage 현재 전압 (mV)
 * @property technology 배터리 기술 타입 (예: Li-ion)
 * @property health 배터리 건강 상태 (Good, Dead, Overheat 등)
 */
data class BatteryModel(
    val level: Int = 0,
    val isCharging: Boolean = false,
    val temperature: Float = 0f,
    val voltage: Int = 0,
    val technology: String = "",
    val health: String = "Unknown"
)