package com.example.droidinsight.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.droidinsight.R

/**
 * 하단 내비게이션 바(Bottom Navigation Bar)의 아이템 정보를 정의하는 Sealed Class
 * 각 화면의 경로(Route), 탭 이름(리소스 ID), 아이콘을 관리
 *
 * @property route 내비게이션 이동 시 사용할 고유 경로 문자열
 * @property titleRes 탭에 표시될 이름의 String Resource ID (다국어 지원)
 * @property icon 탭에 표시될 벡터 아이콘
 */
sealed class BottomNavItem(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    // 1. 대시보드
    object Dashboard : BottomNavItem(
        route = "dashboard",
        titleRes = R.string.nav_dashboard,
        icon = Icons.Default.Home
    )

    // 2. 앱 사용 통계
    object Usage : BottomNavItem(
        route = "usage",
        titleRes = R.string.nav_usage,
        icon = Icons.Default.List
    )

    // 3. 네트워크 모니터
    object Network : BottomNavItem(
        route = "network",
        titleRes = R.string.nav_network,
        icon = Icons.Default.Share
    )
}