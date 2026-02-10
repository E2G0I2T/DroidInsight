package com.example.droidinsight.data.repository

import android.net.TrafficStats
import com.example.droidinsight.domain.model.NetworkModel
import com.example.droidinsight.domain.repository.NetworkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class NetworkRepositoryImpl @Inject constructor() : NetworkRepository {

    companion object {
        private const val UPDATE_INTERVAL_MS = 1000L // 1초 간격
    }

     // 실시간 네트워크 트래픽 사용량을 1초 단위로 측정하여 Flow로 방출
     // TrafficStats API를 사용하여 이전 시점과 현재 시점의 데이터 전송량 차이(Delta)를 계산
    override fun observeNetworkUsage(): Flow<NetworkModel> = flow {
        // 1. 기준점 설정 (초기 총 데이터량)
        var lastRxBytes = TrafficStats.getTotalRxBytes() // Download
        var lastTxBytes = TrafficStats.getTotalTxBytes() // Upload

        // 기기 미지원 시 처리 (TrafficStats.UNSUPPORTED == -1)
        if (lastRxBytes == TrafficStats.UNSUPPORTED.toLong() || lastTxBytes == TrafficStats.UNSUPPORTED.toLong()) {
            emit(NetworkModel(0, 0))
            return@flow
        }

        // 초기값 방출 (UI가 1초간 비어있는 것 방지)
        emit(NetworkModel(0, 0))

        while (true) {
            // 2. 측정 주기 대기
            delay(UPDATE_INTERVAL_MS)

            // 3. 현재 시점 데이터량 가져오기
            val currentRxBytes = TrafficStats.getTotalRxBytes()
            val currentTxBytes = TrafficStats.getTotalTxBytes()

            // 4. 차이 계산 (속도 = 현재 누적량 - 1초 전 누적량)
            // coerceAtLeast(0)은 기기 재부팅이나 카운터 오버플로우로 인한 음수 값을 방지
            val downloadSpeed = (currentRxBytes - lastRxBytes).coerceAtLeast(0)
            val uploadSpeed = (currentTxBytes - lastTxBytes).coerceAtLeast(0)

            emit(NetworkModel(downloadSpeed, uploadSpeed))

            // 5. 기준점 갱신
            lastRxBytes = currentRxBytes
            lastTxBytes = currentTxBytes
        }
    }.flowOn(Dispatchers.IO)
}