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

    // 1. 데이터베이스 (10일차 추가)
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "droid_insight_db"
        ).build()
    }

    // 2. DAO (10일차 추가)
    @Provides
    fun provideUsageDao(database: AppDatabase): UsageDao {
        return database.usageDao()
    }

    // 3. 배터리 리포지토리
    @Provides
    @Singleton
    fun provideBatteryRepository(
        @ApplicationContext context: Context
    ): BatteryRepository {
        return BatteryRepositoryImpl(context)
    }

    // 4. 앱 사용 통계 리포지토리 (여기가 에러 났던 곳!)
    @Provides
    @Singleton
    fun provideUsageRepository(
        @ApplicationContext context: Context,
        usageDao: UsageDao // [수정] DAO를 받아서
    ): UsageRepository {
        return UsageRepositoryImpl(context, usageDao) // [수정] 생성자에 넣어줌
    }

    // 5. 시스템 정보 리포지토리
    @Provides
    @Singleton
    fun provideSystemRepository(
        @ApplicationContext context: Context
    ): SystemRepository {
        return SystemRepository(context)
    }

    // 6. 네트워크 리포지토리
    @Provides
    @Singleton
    fun provideNetworkRepository(): NetworkRepository {
        return NetworkRepositoryImpl()
    }
}