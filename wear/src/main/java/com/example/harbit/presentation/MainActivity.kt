package com.example.harbit.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Text
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.Button
import com.example.harbit.sensors.SensorService
import com.example.harbit.presentation.theme.HARbitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var running by remember { mutableStateOf(false) }
            HARbitTheme(
                darkTheme = true,
                dynamicColor = false
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = com.example.harbit.R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .width(80.dp)
                        )
//                        Text(if (running) "Streaming sensors..." else "Ready")
                        Spacer(Modifier.height(8.dp))
//                        if (running) {
//                            Text("Accelerometer + Gyroscope", style = MaterialTheme.typography.bodyLarge)
//                            Text("@ 20Hz synchronized", style = MaterialTheme.typography.bodyLarge)
//                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (!running) {
                                    ContextCompat.startForegroundService(
                                        this@MainActivity, Intent(this@MainActivity, SensorService::class.java)
                                    )
                                } else {
                                    stopService(Intent(this@MainActivity, SensorService::class.java))
                                }
                                running = !running
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .width(100.dp)
                                .height(30.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (running) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondaryContainer
                            ),
                        ) {
                            Text(
                                if (running) "Detener" else "Empezar",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (running) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}