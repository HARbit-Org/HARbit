package com.example.harbit_wearos

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

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

@Serializable
data class SensorReading(
    val timestamp: Long,
    val x: Float,
    val y: Float,
    val z: Float
)

@Serializable
data class SensorDataExport(
    val gyro: List<SensorReading>,
    val accel: List<SensorReading>
)

@Composable
fun GyroScreen() {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: GyroViewModel = viewModel(factory = ViewModelProvider.AndroidViewModelFactory(context))
    val gyroText by viewModel.gyroText.collectAsState()
    val sensorCount by viewModel.sensorCount.collectAsState()
    
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Sensor Data", style = MaterialTheme.typography.headlineMedium)
            Text("Collected: $sensorCount samples", style = MaterialTheme.typography.bodyMedium)
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
            
            // Buttons row
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.checkWearConnection() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Refresh")
                }
                Button(
                    onClick = { viewModel.clearData() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
                Button(
                    onClick = { viewModel.downloadJson(context) },
                    modifier = Modifier.weight(1f),
                    enabled = sensorCount > 0
                ) {
                    Text("Download")
                }
            }
        }
    }
}

class GyroViewModel(application: Application) : AndroidViewModel(application), MessageClient.OnMessageReceivedListener {
    private val _gyroText = MutableStateFlow("Waiting for sensor data...")
    val gyroText = _gyroText.asStateFlow()
    
    private val _sensorCount = MutableStateFlow(0)
    val sensorCount = _sensorCount.asStateFlow()

    private val MSG_PATH = "/sensor_data"
    private val messageClient = Wearable.getMessageClient(application)
    private val nodeClient = Wearable.getNodeClient(application)
    
    // Store sensor data for export
    private val gyroData = mutableListOf<SensorReading>()
    private val accelData = mutableListOf<SensorReading>()

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
                var sampleCount = 0
                
                while (buf.remaining() >= 8 + 1 + 3 * 4) { // timestamp + sensorType + 3 floats
                    val timestamp = buf.long
                    val sensorType = buf.get()
                    val x = buf.float
                    val y = buf.float
                    val z = buf.float
                    
                    // Store data based on sensor type
                    if (sensorType.toInt() == 1) { // Accelerometer
                        accelData.add(SensorReading(timestamp, x, y, z))
                    } else if (sensorType.toInt() == 2) { // Gyroscope
                        gyroData.add(SensorReading(timestamp, x, y, z))
                    }
                    
                    sampleCount++
                }
                
                _sensorCount.value = gyroData.size + accelData.size
                
                sb.append("Total collected: ${gyroData.size} gyro + ${accelData.size} accel samples\n")
                sb.append("Received $sampleCount new samples\n\n")
                
                // Display last few samples of each type
                if (gyroData.isNotEmpty()) {
                    sb.append("Recent GYRO samples:\n")
                    gyroData.takeLast(3).forEach { gyro ->
                        sb.append("  ${gyro.timestamp}: x=${String.format("%.3f", gyro.x)}, y=${String.format("%.3f", gyro.y)}, z=${String.format("%.3f", gyro.z)}\n")
                    }
                    sb.append("\n")
                }
                
                if (accelData.isNotEmpty()) {
                    sb.append("Recent ACCEL samples:\n")
                    accelData.takeLast(3).forEach { accel ->
                        sb.append("  ${accel.timestamp}: x=${String.format("%.3f", accel.x)}, y=${String.format("%.3f", accel.y)}, z=${String.format("%.3f", accel.z)}\n")
                    }
                }
                
                _gyroText.value = sb.toString()
                android.util.Log.d("SensorViewModel", "Received $sampleCount sensor readings, total: ${gyroData.size}")
            } catch (e: Exception) {
                _gyroText.value = "Error parsing data: ${e.message}"
                android.util.Log.e("SensorViewModel", "Error parsing sensor data", e)
            }
        }
    }
    
    fun clearData() {
        gyroData.clear()
        accelData.clear()
        _sensorCount.value = 0
        _gyroText.value = "Data cleared. Waiting for new sensor data..."
    }
    
    fun downloadJson(context: Context) {
        try {
            val sensorExport = SensorDataExport(
                gyro = gyroData.toList(),
                accel = accelData.toList()
            )
            
            val json = Json { prettyPrint = true }
            val jsonString = json.encodeToString(sensorExport)
            
            // Generate filename with timestamp
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            val filename = "sensor_data_${dateFormat.format(Date())}.json"
            
            // Save to Downloads folder
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                _gyroText.value = "JSON downloaded to Downloads/$filename\nTotal samples: ${gyroData.size}"
                android.util.Log.d("SensorViewModel", "JSON file saved: $filename")
            }
        } catch (e: IOException) {
            _gyroText.value = "Error saving file: ${e.message}"
            android.util.Log.e("SensorViewModel", "Error saving JSON", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        messageClient.removeListener(this)
    }
}