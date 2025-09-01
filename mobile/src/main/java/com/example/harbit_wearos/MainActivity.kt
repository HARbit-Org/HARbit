package com.example.harbit_wearos

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
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
            Text(gyroText, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

class GyroViewModel(application: Application) : AndroidViewModel(application), MessageClient.OnMessageReceivedListener {
    private val _gyroText = MutableStateFlow("Waiting for gyroscope data...")
    val gyroText = _gyroText.asStateFlow()

    private val MSG_PATH = "/gyro"

    init {
        Wearable.getMessageClient(application).addListener(this)
    }

    override fun onMessageReceived(event: MessageEvent) {
        if (event.path == MSG_PATH) {
            val payload = event.data
            val buf = java.nio.ByteBuffer.wrap(payload)
            val sb = StringBuilder()
            while (buf.remaining() >= 8 + 3 * 4) {
                val timestamp = buf.long
                val x = buf.float
                val y = buf.float
                val z = buf.float
                sb.append("timestamp: $timestamp\nx: $x\ny: $y\nz: $z\n\n")
            }
            _gyroText.value = sb.toString()
        }
    }

    override fun onCleared() {
        super.onCleared()
        Wearable.getMessageClient(getApplication()).removeListener(this)
    }
}