package com.example.droidinsight.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.droidinsight.data.local.entity.UsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {

    // 중복 데이터는 덮어쓰기 (REPLACE)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageStats(stats: List<UsageEntity>)

    // 해당 날짜 데이터 조회 (사용 시간 많은 순 정렬)
    @Query("SELECT * FROM usage_stats WHERE date = :date ORDER BY usageTime DESC")
    fun getUsageStatsByDate(date: Long): Flow<List<UsageEntity>>

    @Query("DELETE FROM usage_stats")
    suspend fun clearAll()
}