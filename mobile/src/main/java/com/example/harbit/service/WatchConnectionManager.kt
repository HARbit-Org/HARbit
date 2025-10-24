package com.example.harbit.service

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the connection status with the smartwatch.
 * Sends periodic ping messages to check if the watch is connected and responding.
 */
@Singleton
class WatchConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) : MessageClient.OnMessageReceivedListener {

    companion object {
        private const val TAG = "WatchConnectionManager"
        private const val PING_PATH = "/ping"
        private const val PONG_PATH = "/pong"
        private const val PING_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes
        private const val RESPONSE_TIMEOUT_MS = 10_000L // 10 seconds to wait for response
    }

    private val messageClient by lazy { Wearable.getMessageClient(context) }
    private val nodeClient by lazy { Wearable.getNodeClient(context) }

    private val _isWatchConnected = MutableStateFlow(false)
    val isWatchConnected: StateFlow<Boolean> = _isWatchConnected.asStateFlow()

    private var pingJob: Job? = null
    private var timeoutJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var isListenerRegistered = false

    /**
     * Start monitoring watch connection with periodic pings
     */
    fun startMonitoring() {
        if (pingJob?.isActive == true) {
            Log.d(TAG, "Monitoring already active")
            return
        }

        // Register message listener if not already registered
        if (!isListenerRegistered) {
            messageClient.addListener(this)
            isListenerRegistered = true
            Log.d(TAG, "Message listener registered")
        }

        // Send initial ping immediately
        sendPing()

        // Start periodic ping job
        pingJob = scope.launch {
            while (isActive) {
                delay(PING_INTERVAL_MS)
                sendPing()
            }
        }

        Log.d(TAG, "Watch connection monitoring started (ping every 5 minutes)")
    }

    /**
     * Stop monitoring watch connection
     */
    fun stopMonitoring() {
        pingJob?.cancel()
        pingJob = null
        timeoutJob?.cancel()
        timeoutJob = null

        if (isListenerRegistered) {
            messageClient.removeListener(this)
            isListenerRegistered = false
            Log.d(TAG, "Message listener unregistered")
        }

        _isWatchConnected.value = false
        Log.d(TAG, "Watch connection monitoring stopped")
    }

    /**
     * Send a ping message to all connected nodes (watches)
     */
    private fun sendPing() {
        Log.d(TAG, "sendPing() called - preparing to send ping")
        
        // Cancel any existing timeout job
        timeoutJob?.cancel()

        // Start a new timeout job
        timeoutJob = scope.launch {
            delay(RESPONSE_TIMEOUT_MS)
            // If we reach here, no pong was received
            Log.w(TAG, "Ping timeout - no response from watch")
            _isWatchConnected.value = false
        }

        // Get connected nodes and send ping
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            if (nodes.isEmpty()) {
                Log.w(TAG, "No connected nodes found")
                _isWatchConnected.value = false
                timeoutJob?.cancel()
                return@addOnSuccessListener
            }

            Log.d(TAG, "Sending ping to ${nodes.size} node(s)")
            nodes.forEach { node ->
                Log.d(TAG, "Attempting to send ping to node: ${node.displayName} (${node.id})")
                messageClient.sendMessage(node.id, PING_PATH, ByteArray(0))
                    .addOnSuccessListener {
                        Log.d(TAG, "✓ Ping sent successfully to ${node.displayName}")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "✗ Failed to send ping to ${node.displayName}: ${e.message}")
                        _isWatchConnected.value = false
                        timeoutJob?.cancel()
                    }
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to get connected nodes: ${e.message}")
            _isWatchConnected.value = false
            timeoutJob?.cancel()
        }
    }

    /**
     * Handle incoming messages from the watch
     */
    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "Message received: path=${messageEvent.path}, source=${messageEvent.sourceNodeId}")
        if (messageEvent.path == PONG_PATH) {
            Log.d(TAG, "✓ Pong received from watch - connection confirmed!")
            _isWatchConnected.value = true
            timeoutJob?.cancel() // Cancel timeout since we got a response
        } else {
            Log.d(TAG, "Message ignored (not a pong): ${messageEvent.path}")
        }
    }

    /**
     * Manually trigger a connection check (useful for immediate feedback)
     */
    fun checkConnection() {
        sendPing()
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        stopMonitoring()
        scope.cancel()
    }
}
