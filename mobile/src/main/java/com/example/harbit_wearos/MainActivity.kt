package com.example.harbit_wearos

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                GyroScreen()
            }
        }
    }
}

@Composable
fun GyroScreen() {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: GyroViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(context))
    val gyroText by viewModel.gyroText.collectAsState()
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Sensor Data", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Scrollable area for the gyro data
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
            ) {
                val scrollState = androidx.compose.foundation.rememberScrollState()
                Text(
                    text = gyroText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.verticalScroll(scrollState)
                )
            }
            
            // Add a button to refresh the connection
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { 
                    // Cast to our view model type to access the function
                    (viewModel as? GyroViewModel)?.checkWearConnection() 
                },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text("Refresh Connection")
            }
        }
    }
}

class GyroViewModel(application: Application) : AndroidViewModel(application), MessageClient.OnMessageReceivedListener {
    private val _gyroText = MutableStateFlow("Waiting for sensor data...")
    val gyroText = _gyroText.asStateFlow()

    private val MSG_PATH = "/sensor_data"
    private val messageClient = Wearable.getMessageClient(application)
    private val nodeClient = Wearable.getNodeClient(application)

    init {
        // Register listener immediately
        messageClient.addListener(this)
        
        // Check for and establish connection with wear devices
        checkWearConnection()
    }
    
    // Made public to allow UI interaction
    fun checkWearConnection() {
        _gyroText.value = "Checking connection..."
        
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            if (nodes.isEmpty()) {
                _gyroText.value = "No connected wearable devices found.\nPlease ensure your watch is connected."
            } else {
                val nodeNames = nodes.joinToString(", ") { it.displayName }
                _gyroText.value = "Connected to: $nodeNames\nWaiting for gyroscope data..."
                
                // Send a test message to verify connection
                nodes.forEach { node ->
                    messageClient.sendMessage(node.id, "/mobile-connected", byteArrayOf())
                        .addOnSuccessListener {
                            android.util.Log.d("GyroViewModel", "Successfully sent connection message to ${node.displayName}")
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("GyroViewModel", "Failed to send message to ${node.displayName}: ${e.message}")
                            _gyroText.value = "Connection error: ${e.message}\nPlease restart the app."
                        }
                }
            }
        }.addOnFailureListener { e ->
            _gyroText.value = "Failed to check wearable connection: ${e.message}"
            android.util.Log.e("GyroViewModel", "Failed to get connected nodes: ${e.message}")
        }
    }

    override fun onMessageReceived(event: MessageEvent) {
        if (event.path == MSG_PATH) {
            val payload = event.data
            val buf = java.nio.ByteBuffer.wrap(payload)
            val sb = StringBuilder()
            
            try {
                // Each sample is [long timestamp][byte sensorType][float x][float y][float z]
                // sensorType: 1 = accelerometer, 2 = gyroscope
                val accelData = mutableMapOf<Long, Triple<Float, Float, Float>>()
                val gyroData = mutableMapOf<Long, Triple<Float, Float, Float>>()
                
                while (buf.remaining() >= 8 + 1 + 3 * 4) { // timestamp + sensorType + 3 floats
                    val timestamp = buf.long
                    val sensorType = buf.get()
                    val x = buf.float
                    val y = buf.float
                    val z = buf.float
                    
                    // Store based on sensor type
                    if (sensorType.toInt() == 1) { // Accelerometer
                        accelData[timestamp] = Triple(x, y, z)
                    } else if (sensorType.toInt() == 2) { // Gyroscope
                        gyroData[timestamp] = Triple(x, y, z)
                    }
                }
                
                // Combine and display data, sorted by timestamp
                val allTimestamps = (accelData.keys + gyroData.keys).sorted()
                
                sb.append("Received ${accelData.size} accelerometer samples and ${gyroData.size} gyroscope samples\n\n")
                
                for (timestamp in allTimestamps) {
                    sb.append("timestamp: $timestamp\n")
                    
                    accelData[timestamp]?.let { (x, y, z) ->
                        sb.append("ACCEL: x=$x, y=$y, z=$z\n")
                    }
                    
                    gyroData[timestamp]?.let { (x, y, z) ->
                        sb.append("GYRO: x=$x, y=$y, z=$z\n")
                    }
                    
                    sb.append("\n")
                }
                
                _gyroText.value = sb.toString()
                android.util.Log.d("SensorViewModel", "Received ${allTimestamps.size} sensor readings from ${event.sourceNodeId}")
            } catch (e: Exception) {
                _gyroText.value = "Error parsing data: ${e.message}"
                android.util.Log.e("SensorViewModel", "Error parsing sensor data", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        messageClient.removeListener(this)
    }
}