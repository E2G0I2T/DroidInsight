package com.example.droidinsight.domain.model

import android.graphics.drawable.Drawable

/**
 * 앱 사용 통계 정보를 UI에 표시하기 위한 데이터 모델
 * @property packageName 앱 패키지명 (고유 식별자, 예: com.kakao.talk)
 * @property appName 사용자에게 표시되는 앱 이름
 * @property usageTime 오늘 하루 총 사용 시간
 * @property lastTimeUsed 마지막 사용 시각
 * @property appIcon 앱 아이콘 이미지
 * @property progress 전체 사용량 대비 비율
 */
data class UsageModel(
    val packageName: String,
    val appName: String,
    val usageTime: Long,
    val lastTimeUsed: Long,
    val appIcon: Drawable? = null,
    val progress: Float = 0f
)