package com.example.droidinsight.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import com.example.droidinsight.domain.model.UsageModel
import com.example.droidinsight.domain.repository.UsageRepository
import java.util.Calendar
import javax.inject.Inject

class UsageRepositoryImpl @Inject constructor(
    private val context: Context
) : UsageRepository {

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
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val packageManager = context.packageManager

        // 오늘 0시부터 현재까지의 시간 설정
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        // 데이터 쿼리 (INTERVAL_DAILY 사용)
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        // 데이터 가공: 사용 시간이 0보다 큰 앱만 필터링 -> 내림차순 정렬 -> 모델 변환
        return usageStatsList
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }
            .map { usageStats ->
                val appName = try {
                    packageManager.getApplicationLabel(
                        packageManager.getApplicationInfo(usageStats.packageName, 0)
                    ).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    usageStats.packageName // 이름 못 찾으면 패키지명 사용
                }

                val icon = try {
                    packageManager.getApplicationIcon(usageStats.packageName)
                } catch (e: Exception) {
                    null // 아이콘 없으면 null
                }

                UsageModel(
                    packageName = usageStats.packageName,
                    appName = appName,
                    usageTime = usageStats.totalTimeInForeground,
                    lastTimeUsed = usageStats.lastTimeUsed,
                    appIcon = icon
                )
            }
    }
}