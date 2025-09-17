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
            Text("Gyroscope Data", style = MaterialTheme.typography.headlineMedium)
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
    private val _gyroText = MutableStateFlow("Waiting for gyroscope data...")
    val gyroText = _gyroText.asStateFlow()

    private val MSG_PATH = "/gyro"
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
                while (buf.remaining() >= 8 + 3 * 4) {
                    val timestamp = buf.long
                    val x = buf.float
                    val y = buf.float
                    val z = buf.float
                    sb.append("timestamp: $timestamp\nx: $x\ny: $y\nz: $z\n\n")
                }
                _gyroText.value = sb.toString()
                android.util.Log.d("GyroViewModel", "Received gyro data from ${event.sourceNodeId}")
            } catch (e: Exception) {
                _gyroText.value = "Error parsing data: ${e.message}"
                android.util.Log.e("GyroViewModel", "Error parsing gyro data", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        messageClient.removeListener(this)
    }
}