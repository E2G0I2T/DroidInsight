package com.example.droidinsight.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.droidinsight.presentation.dashboard.DashboardScreen
import com.example.droidinsight.presentation.navigation.BottomNavItem
import com.example.droidinsight.presentation.network.NetworkScreen
import com.example.droidinsight.presentation.usage.UsageScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // 하단 탭 목록 정의
    val navItems = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Network,
        BottomNavItem.Usage
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { screen ->
                    // 현재 화면이 선택된 상태인지 확인
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                    NavigationBarItem(
                        icon = { Icon(imageVector = screen.icon, contentDescription = null) },
                        label = { Text(text = stringResource(id = screen.titleRes)) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                // 1. 백스택 관리: 홈(StartDestination)까지만 남기고 스택 제거
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // 2. 중복 방지: 이미 선택된 탭을 다시 누르면 새 창을 띄우지 않음
                                launchSingleTop = true
                                // 3. 상태 복원: 스크롤 위치 등 이전 상태 복구
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // 내비게이션 호스트
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Dashboard.route) {
                DashboardScreen()
            }
            composable(BottomNavItem.Network.route) {
                NetworkScreen()
            }
            composable(BottomNavItem.Usage.route) {
                UsageScreen()
            }
        }
    }
}