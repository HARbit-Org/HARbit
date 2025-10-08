package com.example.harbit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.compose.runtime.SideEffect
import androidx.navigation.compose.rememberNavController
import com.example.harbit.service.SensorDataService
import com.example.harbit.ui.navigation.AppNavigation
import com.example.harbit.ui.theme.HARbitTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    
    // Permission launcher for POST_NOTIFICATIONS
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, start service
            startSensorDataService()
        } else {
            // Permission denied - service can still start but notification won't show
            startSensorDataService()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission for Android 13+
        requestNotificationPermissionAndStartService()
        
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
    
    private fun requestNotificationPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requires runtime permission for notifications
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    startSensorDataService()
                }
                else -> {
                    // Request permission
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Below Android 13, no runtime permission needed
            startSensorDataService()
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