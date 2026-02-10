package com.example.droidinsight.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Process
import com.example.droidinsight.data.local.dao.UsageDao
import com.example.droidinsight.data.local.entity.UsageEntity
import com.example.droidinsight.domain.model.UsageModel
import com.example.droidinsight.domain.repository.UsageRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject

class UsageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val usageDao: UsageDao
) : UsageRepository {

    // 사용자가 앱 사용 정보 접근 권한(PACKAGE_USAGE_STATS)을 허용했는지 확인
    override fun hasPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override suspend fun getTodayUsageStats(): List<UsageModel> {
        val (startTime, endTime) = getTodayTimeRange()

        // 1. 시스템 API에서 Raw 데이터 가져오기
        val systemUsageList = querySystemUsage(startTime, endTime)
            .filter { it.totalTimeInForeground > 0 } // 사용 안 한 앱 제외
            .sortedByDescending { it.totalTimeInForeground }

        if (systemUsageList.isEmpty()) return emptyList()

        // 2. UI 표시용 데이터로 가공 (이름, 아이콘, 비율 계산)
        val maxUsageTime = systemUsageList.first().totalTimeInForeground.toFloat()
        val usageModels = systemUsageList.map { usageStats ->
            mapToDomainModel(usageStats, maxUsageTime)
        }

        // 3. DB에 백업 (실패해도 앱 동작에는 영향 없도록 예외 처리)
        cacheToDatabase(usageModels, startTime)

        return usageModels
    }

    private fun getTodayTimeRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        return Pair(startTime, endTime)
    }

    private fun querySystemUsage(start: Long, end: Long): List<UsageStats> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        return usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            start,
            end
        ) ?: emptyList()
    }

    private fun mapToDomainModel(usageStats: UsageStats, maxTime: Float): UsageModel {
        val pm = context.packageManager
        val packageName = usageStats.packageName

        // 앱 이름 가져오기 (실패 시 패키지명 -> 포맷팅)
        val appName = try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val label = pm.getApplicationLabel(appInfo).toString()
            formatAppName(label, packageName)
        } catch (e: Exception) {
            formatAppName(packageName, packageName)
        }

        // 아이콘 가져오기
        val icon: Drawable? = try {
            pm.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }

        return UsageModel(
            packageName = packageName,
            appName = appName,
            usageTime = usageStats.totalTimeInForeground,
            lastTimeUsed = usageStats.lastTimeUsed,
            appIcon = icon,
            progress = usageStats.totalTimeInForeground / maxTime
        )
    }

    // "com.google.android.youtube" -> "Youtube" 로 변환하는 로직
    private fun formatAppName(originalName: String, packageName: String): String {
        return if (originalName.contains("com.") || originalName.contains(".")) {
            packageName.substringAfterLast('.')
                .replaceFirstChar { it.uppercase() }
        } else {
            originalName
        }
    }

    private suspend fun cacheToDatabase(models: List<UsageModel>, date: Long) {
        try {
            val entities = models.map { model ->
                UsageEntity(
                    date = date,
                    packageName = model.packageName,
                    appName = model.appName,
                    usageTime = model.usageTime,
                    lastTimeUsed = model.lastTimeUsed
                )
            }
            usageDao.insertUsageStats(entities)
        } catch (e: Exception) {
            e.printStackTrace() // DB 에러는 로그만 남기고 무시 (UI 표시가 우선)
        }
    }
}