package com.example.droidinsight.domain.model

data class NetworkModel(
    val downloadSpeed: Long = 0, // 초당 바이트 수 (Bps)
    val uploadSpeed: Long = 0    // 초당 바이트 수 (Bps)
)