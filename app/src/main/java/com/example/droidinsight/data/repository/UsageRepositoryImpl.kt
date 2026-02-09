package com.example.droidinsight.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import com.example.droidinsight.data.local.dao.UsageDao // 추가
import com.example.droidinsight.data.local.entity.UsageEntity // 추가
import com.example.droidinsight.domain.model.UsageModel
import com.example.droidinsight.domain.repository.UsageRepository
import java.util.Calendar
import javax.inject.Inject

class UsageRepositoryImpl @Inject constructor(
    private val context: Context,
    private val usageDao: UsageDao // [추가] DB 접근 도구 주입
) : UsageRepository {

    override fun hasPermission(): Boolean {
        // ... (기존 코드와 동일) ...
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

        // 시간 설정 (오늘 0시)
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        // 1. 시스템 API에서 데이터 가져오기
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        // 2. 데이터 가공 (모델 변환)
        val resultList = usageStatsList
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }
            .map { usageStats ->
                // 1. 진짜 이름 가져오기 시도
                var appName = try {
                    packageManager.getApplicationLabel(
                        packageManager.getApplicationInfo(usageStats.packageName, 0)
                    ).toString()
                } catch (e: Exception) {
                    usageStats.packageName // 실패 시 패키지명
                }

                // [추가] 만약 이름이 여전히 패키지명(com...) 같다면 강제로 예쁘게 만듦
                if (appName.contains("com.") || appName.contains(".")) {
                    appName = usageStats.packageName.substringAfterLast('.') // 마지막 단어 추출
                        .replaceFirstChar { it.uppercase() } // 대문자로 시작
                }

                // 2. [추가] 앱 아이콘 가져오기
                val appIcon = try {
                    packageManager.getApplicationIcon(usageStats.packageName)
                } catch (e: Exception) {
                    null
                }

                // 최대값 기준 비율 계산 (예시 로직)
                val maxTime = usageStatsList.maxOfOrNull { it.totalTimeInForeground } ?: 1L
                val progress = usageStats.totalTimeInForeground.toFloat() / maxTime.toFloat()

                UsageModel(
                    packageName = usageStats.packageName,
                    appName = appName, // 진짜 이름
                    usageTime = usageStats.totalTimeInForeground,
                    lastTimeUsed = usageStats.lastTimeUsed,
                    appIcon = appIcon, // [추가] 아이콘 넣기
                    progress = progress
                )
            }

        // 3. [추가] DB에 저장 (캐싱)
        // 화면에 보여줄 데이터를 리턴하기 전에, 몰래 DB에 백업해둡니다.
        try {
            val entities = resultList.map { model ->
                UsageEntity(
                    date = startTime, // 오늘 날짜 기준
                    packageName = model.packageName,
                    appName = model.appName,
                    usageTime = model.usageTime,
                    lastTimeUsed = model.lastTimeUsed
                )
            }
            usageDao.insertUsageStats(entities)
        } catch (e: Exception) {
            e.printStackTrace() // DB 저장 실패해도 앱은 죽지 않게
        }

        return resultList
    }
}