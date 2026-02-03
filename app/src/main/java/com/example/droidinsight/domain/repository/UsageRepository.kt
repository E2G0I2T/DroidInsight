package com.example.droidinsight.domain.repository

import com.example.droidinsight.domain.model.UsageModel
import kotlinx.coroutines.flow.Flow

interface UsageRepository {
    // 권한이 있는지 확인
    fun hasPermission(): Boolean

    // 오늘 하루 사용 앱 리스트 가져오기 (사용 시간 순 정렬)
    suspend fun getTodayUsageStats(): List<UsageModel>
}