package com.example.droidinsight.data.repository

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.example.droidinsight.domain.model.SystemInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SystemRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // 기기의 하드웨어 및 소프트웨어 정보를 종합하여 반환
    fun getSystemInfo(): SystemInfo {
        val memoryInfo = getMemoryInfo()
        val storageInfo = getStorageInfo()

        return SystemInfo(
            modelName = Build.MODEL,
            androidVersion = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})",
            manufacturer = Build.MANUFACTURER.uppercase(),
            totalRam = memoryInfo.totalMem,
            availableRam = memoryInfo.availMem,
            totalStorage = storageInfo.first,
            availableStorage = storageInfo.second
        )
    }

    // ActivityManager를 통해 현재 RAM 상태를 조회
    private fun getMemoryInfo(): ActivityManager.MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    // StatFs를 사용하여 내부 저장소(Data Directory)의 용량을 계산
    private fun getStorageInfo(): Pair<Long, Long> {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)

        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalStorage = totalBlocks * blockSize
        val availableStorage = availableBlocks * blockSize

        return Pair(totalStorage, availableStorage)
    }
}