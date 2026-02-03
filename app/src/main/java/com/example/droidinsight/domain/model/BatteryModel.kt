package com.example.droidinsight.domain.model

data class BatteryModel(
    val level: Int = 0,          // 잔량 (%)
    val isCharging: Boolean = false, // 충전 중 여부
    val temperature: Float = 0f, // 온도 (섭씨)
    val voltage: Int = 0,        // 전압 (mV)
    val technology: String = "", // 기술 (Li-ion 등)
    val health: String = "Unknown" // 건강 상태
)