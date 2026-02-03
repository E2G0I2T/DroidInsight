package com.example.droidinsight.di

import android.content.Context
import com.example.droidinsight.data.repository.BatteryRepositoryImpl
import com.example.droidinsight.domain.repository.BatteryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBatteryRepository(
        @ApplicationContext context: Context
    ): BatteryRepository {
        return BatteryRepositoryImpl(context)
    }
}