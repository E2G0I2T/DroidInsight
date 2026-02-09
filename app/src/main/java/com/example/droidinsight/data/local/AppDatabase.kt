package com.example.droidinsight.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.droidinsight.data.local.dao.UsageDao
import com.example.droidinsight.data.local.entity.UsageEntity

@Database(entities = [UsageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usageDao(): UsageDao
}