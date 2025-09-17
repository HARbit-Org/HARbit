/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.harbit_wearos.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.Text
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.Button
import com.example.harbit_wearos.sensors.SensorService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var running by remember { mutableStateOf(false) }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (running) "Streaming gyroscope…" else "Ready")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {
                        if (!running) {
                            ContextCompat.startForegroundService(
                                this@MainActivity, Intent(this@MainActivity, SensorService::class.java)
                            )
                        } else {
                            stopService(Intent(this@MainActivity, SensorService::class.java))
                        }
                        running = !running
                    }) {
                        Text(if (running) "Stop" else "Start")
                    }
                }
            }
        }
    }
}