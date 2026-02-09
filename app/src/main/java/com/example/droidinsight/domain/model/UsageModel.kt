package com.example.droidinsight.domain.model

import android.graphics.drawable.Drawable // [추가]

data class UsageModel(
    val packageName: String,
    val appName: String,
    val usageTime: Long,
    val lastTimeUsed: Long,
    val appIcon: Drawable? = null, // [추가] 아이콘 이미지 (없을 수도 있으므로 nullable)
    val progress: Float = 0f       // [기존 유지] 사용량 비율
)