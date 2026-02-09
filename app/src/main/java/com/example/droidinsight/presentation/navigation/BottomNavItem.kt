package com.example.droidinsight.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Share // 공유 아이콘 (네트워크 느낌)
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Dashboard : BottomNavItem("dashboard", "대시보드", Icons.Default.Home)
    object Usage : BottomNavItem("usage", "앱 통계", Icons.Default.List)
    object Network : BottomNavItem("network", "네트워크", Icons.Default.Share)
}