package com.example.harbit.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.harbit.ui.components.BottomNavItem
import com.example.harbit.ui.components.HARbitBottomNavigation
import com.example.harbit.ui.navigation.AppNavigation
import com.example.harbit.ui.screens.dashboard.DashboardScreen
import com.example.harbit.ui.screens.profile.ProfileScreen
import com.example.harbit.ui.screens.progress.ProgressScreen

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    
    // List of routes that should show bottom navigation
    val bottomNavRoutes = listOf("profile", "activity", "progress")
    val showBottomNav = currentRoute in bottomNavRoutes
    
    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                HARbitBottomNavigation(
                    currentRoute = currentRoute ?: "activity",
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to the start destination and save state
                            popUpTo("activity") {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentRoute) {
                "profile" -> ProfileScreen()
                "activity" -> DashboardScreen(
                    onActivityDetailClick = {
                        navController.navigate("activity_detail")
                    },
                    onHeartRateClick = {
                        navController.navigate("heart_rate_detail")
                    },
                    onStepsClick = {
                        navController.navigate("steps_detail")
                    }
                )
                "progress" -> ProgressScreen()
                else -> {
                    // Handle other routes through navigation
                    AppNavigation(
                        navController = navController,
                        startDestination = if (currentRoute == null) "welcome" else currentRoute
                    )
                }
            }
        }
    }
}