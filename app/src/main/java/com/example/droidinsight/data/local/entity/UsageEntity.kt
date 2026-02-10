package com.example.droidinsight.data.local.entity

import androidx.room.Entity


 // 복합키(date + packageName)를 사용하여 "하루에 앱 하나당 1개의 레코드"만 저장됨
@Entity(
    tableName = "usage_stats",
    primaryKeys = ["date", "packageName"]
)
data class UsageEntity(
    val date: Long,          // 기준 날짜 (00:00:00 타임스탬프)
    val packageName: String, // 식별자
    val appName: String,     // 표시 이름
    val usageTime: Long,     // 총 사용 시간
    val lastTimeUsed: Long   // 마지막 사용 시각
)