package com.example.droidinsight.di

import android.content.Context
import androidx.room.Room
import com.example.droidinsight.data.local.AppDatabase
import com.example.droidinsight.data.local.dao.UsageDao
import com.example.droidinsight.data.repository.BatteryRepositoryImpl
import com.example.droidinsight.data.repository.NetworkRepositoryImpl
import com.example.droidinsight.data.repository.SystemRepository
import com.example.droidinsight.data.repository.UsageRepositoryImpl
import com.example.droidinsight.domain.repository.BatteryRepository
import com.example.droidinsight.domain.repository.NetworkRepository
import com.example.droidinsight.domain.repository.UsageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val DATABASE_NAME = "droid_insight.db"

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        ).fallbackToDestructiveMigration() // 스키마 변경 시 충돌 방지
            .build()
    }

    @Provides
    @Singleton
    fun provideUsageDao(database: AppDatabase): UsageDao = database.usageDao()

    @Provides
    @Singleton
    fun provideBatteryRepository(
        @ApplicationContext context: Context
    ): BatteryRepository = BatteryRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideNetworkRepository(): NetworkRepository = NetworkRepositoryImpl()

    @Provides
    @Singleton
    fun provideSystemRepository(
        @ApplicationContext context: Context
    ): SystemRepository = SystemRepository(context)

    @Provides
    @Singleton
    fun provideUsageRepository(
        @ApplicationContext context: Context,
        usageDao: UsageDao
    ): UsageRepository = UsageRepositoryImpl(context, usageDao)
}