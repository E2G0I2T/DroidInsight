package com.example.droidinsight.domain.model

import android.graphics.drawable.Drawable

data class UsageModel(
    val packageName: String,
    val appName: String,
    val usageTime: Long, // 사용 시간 (밀리초)
    val lastTimeUsed: Long, // 마지막 사용 시각
    val appIcon: Drawable? = null // 앱 아이콘 (UI에 보여주기 위함)
)