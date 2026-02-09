package com.example.droidinsight.data.repository

import android.net.TrafficStats
import com.example.droidinsight.domain.model.NetworkModel
import com.example.droidinsight.domain.repository.NetworkRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NetworkRepositoryImpl @Inject constructor() : NetworkRepository {

    override fun observeNetworkUsage(): Flow<NetworkModel> = flow {
        // 1. 처음 시작할 때의 총 데이터 사용량 기록
        var lastRxBytes = TrafficStats.getTotalRxBytes() // 다운로드 (Receive)
        var lastTxBytes = TrafficStats.getTotalTxBytes() // 업로드 (Transmit)

        // 기기가 기능을 지원하지 않으면 -1을 반환함 (예외 처리)
        if (lastRxBytes == -1L || lastTxBytes == -1L) {
            emit(NetworkModel(0, 0))
            return@flow
        }

        while (true) {
            // 2. 1초 대기 (속도 측정 간격)
            delay(1000)

            // 3. 1초 뒤의 데이터 총량 가져오기
            val currentRxBytes = TrafficStats.getTotalRxBytes()
            val currentTxBytes = TrafficStats.getTotalTxBytes()

            // 4. 차이 계산 (현재 총량 - 1초 전 총량 = 1초간 쓴 양 = 속도)
            val downloadSpeed = (currentRxBytes - lastRxBytes).coerceAtLeast(0)
            val uploadSpeed = (currentTxBytes - lastTxBytes).coerceAtLeast(0)

            // 5. 결과 방출
            emit(NetworkModel(downloadSpeed, uploadSpeed))

            // 6. 현재 값을 과거 값으로 갱신 (다음 턴을 위해)
            lastRxBytes = currentRxBytes
            lastTxBytes = currentTxBytes
        }
    }
}