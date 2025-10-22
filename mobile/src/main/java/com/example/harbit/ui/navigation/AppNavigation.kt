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
import com.example.harbit.ui.screens.splash.SplashScreen
import com.example.harbit.ui.screens.dashboard.DashboardScreen
import com.example.harbit.ui.screens.details.*
import com.example.harbit.ui.screens.onboarding.SmartwatchSetupScreen
import com.example.harbit.ui.screens.profile.ProfileScreen
import com.example.harbit.ui.screens.progress.ProgressScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = "splash"  // 🆕 Changed from "welcome" to "splash"
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
                // 🆕 Splash Screen - Entry point that checks authentication
                composable("splash") {
                    SplashScreen(
                        onNavigateToWelcome = {
                            navController.navigate("welcome") {
                                popUpTo("splash") { inclusive = true }
                            }
                        },
                        onNavigateToProfileCompletion = {
                            navController.navigate("profile_completion") {
                                popUpTo("splash") { inclusive = true }
                            }
                        },
                        onNavigateToMain = {
                            navController.navigate("activity") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    )
                }
                
                // Authentication Flow
                composable("welcome") {
                    WelcomeScreen(
                        onLoginSuccess = {
                            // 🆕 Navigate to splash to re-check profile status
                            navController.navigate("splash") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        }
                    )
                }
                
                composable("profile_completion") {
                    ProfileCompletionScreen(
                        onProfileComplete = {
                            // 🆕 After profile completion, go to smartwatch setup or activity
                            navController.navigate("smartwatch_setup") {
                                popUpTo("profile_completion") { inclusive = true }
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
                                popUpTo("smartwatch_setup") { inclusive = true }  // 🆕 Changed from "welcome"
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