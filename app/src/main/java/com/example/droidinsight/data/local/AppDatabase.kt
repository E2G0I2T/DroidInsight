package com.example.droidinsight.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.droidinsight.data.local.dao.UsageDao
import com.example.droidinsight.data.local.entity.UsageEntity

@Database(
    entities = [UsageEntity::class],
    version = 1,
    exportSchema = false // 스키마 백업 비활성화 (경고 방지용)
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usageDao(): UsageDao
}