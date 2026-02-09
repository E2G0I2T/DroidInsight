package com.example.droidinsight.data.repository

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.example.droidinsight.domain.model.SystemInfo
import java.io.File
import javax.inject.Inject

class SystemRepository @Inject constructor(
    private val context: Context
) {
    fun getSystemInfo(): SystemInfo {
        // 1. 기기 기본 정보
        val model = Build.MODEL
        val version = "Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
        val manufacturer = Build.MANUFACTURER.uppercase()

        // 2. RAM 정보 (ActivityManager)
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memoryInfo)

        // 3. 내부 저장소 정보 (StatFs)
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalStorage = totalBlocks * blockSize
        val availableStorage = availableBlocks * blockSize

        return SystemInfo(
            modelName = model,
            androidVersion = version,
            manufacturer = manufacturer,
            totalRam = memoryInfo.totalMem,
            availableRam = memoryInfo.availMem,
            totalStorage = totalStorage,
            availableStorage = availableStorage
        )
    }
}