package com.example.droidinsight.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.droidinsight.presentation.dashboard.DashboardScreen
import com.example.droidinsight.presentation.navigation.BottomNavItem
import com.example.droidinsight.presentation.network.NetworkScreen // [중요] 임포트 확인
import com.example.droidinsight.presentation.usage.UsageScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // [핵심 수정] 여기에 BottomNavItem.Network가 포함되어야 탭이 보입니다!
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Network, // 여기가 Sensor에서 Network로 바뀌었는지 확인
        BottomNavItem.Usage
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. 대시보드
            composable(BottomNavItem.Dashboard.route) {
                DashboardScreen()
            }

            // 2. 네트워크 (여기도 연결 확인!)
            composable(BottomNavItem.Network.route) {
                NetworkScreen()
            }

            // 3. 앱 통계
            composable(BottomNavItem.Usage.route) {
                UsageScreen()
            }
        }
    }
}