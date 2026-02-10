package com.example.droidinsight.domain.repository

import com.example.droidinsight.domain.model.BatteryModel
import kotlinx.coroutines.flow.Flow

/**
 * 배터리 데이터에 접근하기 위한 리포지토리 인터페이스
 * Domain Layer에 위치하며, 구체적인 구현체(Data Layer)는 Hilt를 통해 주입
 */
interface BatteryRepository {

    /**
     * 실시간 배터리 상태 변화를 관찰
     * 시스템의 배터리 브로드캐스트를 수신하여, 값이 변경될 때마다 새로운 데이터를 방출
     *
     * @return 최신 배터리 정보(BatteryModel)를 지속적으로 전달하는 Flow 스트림
     */
    fun getBatteryData(): Flow<BatteryModel>
}