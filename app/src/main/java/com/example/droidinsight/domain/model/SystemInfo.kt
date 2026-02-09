package com.example.droidinsight.domain.model

data class SystemInfo(
    val modelName: String = "Loading...",
    val androidVersion: String = "",
    val manufacturer: String = "",

    // RAM 정보 (Byte 단위)
    val totalRam: Long = 1,
    val availableRam: Long = 1,

    // 저장소 정보 (Byte 단위)
    val totalStorage: Long = 1,
    val availableStorage: Long = 1
) {
    // RAM 사용률 (0.0 ~ 1.0)
    val ramUsagePercent: Float
        get() = if (totalRam > 0) (totalRam - availableRam).toFloat() / totalRam else 0f

    // 저장소 사용률 (0.0 ~ 1.0)
    val storageUsagePercent: Float
        get() = if (totalStorage > 0) (totalStorage - availableStorage).toFloat() / totalStorage else 0f
}