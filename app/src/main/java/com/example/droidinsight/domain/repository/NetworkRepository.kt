package com.example.droidinsight.domain.repository

import com.example.droidinsight.domain.model.NetworkModel
import kotlinx.coroutines.flow.Flow

interface NetworkRepository {
    // 1초마다 네트워크 속도를 방출하는 Flow
    fun observeNetworkUsage(): Flow<NetworkModel>
}