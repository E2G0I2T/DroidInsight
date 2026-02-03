package com.example.droidinsight.domain.repository

import com.example.droidinsight.domain.model.BatteryModel
import kotlinx.coroutines.flow.Flow

interface BatteryRepository {
    // 실시간으로 배터리 상태가 변하면 계속 데이터를 쏴주는 파이프라인(Flow)
    fun getBatteryData(): Flow<BatteryModel>
}