package com.example.harbit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.harbit.ui.navigation.AppNavigation
import com.example.harbit.ui.theme.HARbitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HARbitTheme (
                darkTheme = false,
                dynamicColor = false
            ) {
                val navController = rememberNavController()
                AppNavigation(
                    navController = navController,
                    startDestination = "welcome"
                )
            }
        }
    }
}