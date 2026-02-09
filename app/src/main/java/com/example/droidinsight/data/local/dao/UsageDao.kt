package com.example.droidinsight.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.droidinsight.data.local.entity.UsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {
    // 데이터 삽입 (이미 있으면 덮어쓰기 REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageStats(stats: List<UsageEntity>)

    // 특정 날짜의 데이터 조회
    @Query("SELECT * FROM usage_stats WHERE date = :date ORDER BY usageTime DESC")
    fun getUsageStatsByDate(date: Long): Flow<List<UsageEntity>>

    // 저장된 모든 데이터 삭제 (초기화용)
    @Query("DELETE FROM usage_stats")
    suspend fun clearAll()
}