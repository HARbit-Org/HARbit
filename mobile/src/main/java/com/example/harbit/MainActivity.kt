package com.example.harbit

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.core.view.WindowCompat
import androidx.compose.runtime.SideEffect
import androidx.navigation.compose.rememberNavController
import com.example.harbit.service.SensorDataService
import com.example.harbit.ui.navigation.AppNavigation
import com.example.harbit.ui.theme.HARbitTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Start foreground service for sensor data sync
        startSensorDataService()
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            HARbitTheme (
                darkTheme = false,
                dynamicColor = false
            ) {
                SetStatusBarColor()
                val navController = rememberNavController()
                AppNavigation(
                    navController = navController,
                    startDestination = "welcome"
                )
            }
        }
    }
    
    private fun startSensorDataService() {
        val serviceIntent = Intent(this, SensorDataService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}

@Composable
fun SetStatusBarColor() {
    val systemUiController = rememberSystemUiController()
    val statusBarColor = MaterialTheme.colorScheme.primary
    val useDarkIcons = true
    
    SideEffect {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = useDarkIcons
        )
    }
}