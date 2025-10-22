package com.example.harbit.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.harbit.ui.components.HARbitBottomNavigation
import com.example.harbit.ui.screens.auth.*
import com.example.harbit.ui.screens.dashboard.DashboardScreen
import com.example.harbit.ui.screens.details.*
import com.example.harbit.ui.screens.onboarding.SmartwatchSetupScreen
import com.example.harbit.ui.screens.profile.ProfileScreen
import com.example.harbit.ui.screens.progress.ProgressScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = "welcome"
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    
    // List of routes that should show bottom navigation
    val bottomNavRoutes = listOf("activity", "profile", "progress")
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
            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                // Authentication Flow
                composable("welcome") {
                    WelcomeScreen(
                        onLoginSuccess = {
                            // Navigate after successful authentication
                            navController.navigate("profile_completion")
                        }
                    )
                }
                
                composable("profile_completion") {
                    ProfileCompletionScreen(
                        onProfileComplete = {
                            navController.navigate("smartwatch_setup") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        },
                        onPrivacyPolicyClick = {
                            navController.navigate("privacy_policy")
                        }
                    )
                }
                
                composable("privacy_policy") {
                    PrivacyPolicyScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
                
                composable("smartwatch_setup") {
                    SmartwatchSetupScreen(
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onContinueClick = {
                            navController.navigate("activity") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        }
                    )
                }
                
                // Main App Screens
                composable("activity") {
                    DashboardScreen(
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
                }
                
                composable("profile") {
                    ProfileScreen()
                }
                
                composable("progress") {
                    ProgressScreen()
                }
                
                // Detail Screens
                composable("activity_detail") {
                    ActivityDistributionDetailScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
                
                composable("heart_rate_detail") {
                    HeartRateDetailScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
                
                composable("steps_detail") {
                    StepsDetailScreen(
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}