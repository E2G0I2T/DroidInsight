package com.example.droidinsight.data.local.entity

import androidx.room.Entity

// tableName: 실제 DB에 저장될 테이블 이름
// primaryKeys: 날짜와 패키지명이 같으면 덮어쓰기 위해 복합키 사용
@Entity(tableName = "usage_stats", primaryKeys = ["date", "packageName"])
data class UsageEntity(
    val date: Long,          // 기준 날짜 (오늘 0시 0분 0초)
    val packageName: String, // 앱 패키지명 (예: com.youtube.android)
    val appName: String,     // 앱 이름
    val usageTime: Long,     // 사용 시간 (밀리초)
    val lastTimeUsed: Long   // 마지막 사용 시각
)