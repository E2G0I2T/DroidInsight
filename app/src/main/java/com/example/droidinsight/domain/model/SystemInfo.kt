package com.example.droidinsight.domain.model

/**
 * 기기의 하드웨어 및 소프트웨어 정보를 담는 데이터 클래스
 * RAM 및 저장소의 원시 데이터(Byte)와 함께, UI 표시용 퍼센트(%) 계산 로직을 포함
 */
data class SystemInfo(
    val modelName: String = "Loading...",
    val androidVersion: String = "",
    val manufacturer: String = "",

    // RAM 정보 (단위: Byte)
    val totalRam: Long = 0,
    val availableRam: Long = 0,

    // 저장소 정보 (단위: Byte)
    val totalStorage: Long = 0,
    val availableStorage: Long = 0
) {
    /**
     * RAM 사용률 (0.0 ~ 1.0)
     * ProgressIndicator 등에서 바로 사용하기 위한 계산된 속성
     */
    val ramUsagePercent: Float
        get() = if (totalRam > 0) {
            (totalRam - availableRam).toFloat() / totalRam
        } else {
            0f
        }

    /**
     * 내부 저장소 사용률 (0.0 ~ 1.0)
     */
    val storageUsagePercent: Float
        get() = if (totalStorage > 0) {
            (totalStorage - availableStorage).toFloat() / totalStorage
        } else {
            0f
        }
}