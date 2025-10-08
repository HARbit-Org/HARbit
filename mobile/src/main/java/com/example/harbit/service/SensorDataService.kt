package com.example.harbit.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.harbit.data.local.SensorBatchEntity
import com.example.harbit.domain.repository.SensorRepository
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SensorDataService : LifecycleService(), MessageClient.OnMessageReceivedListener {

    companion object {
        private const val TAG = "SensorDataService"
        private const val CHANNEL_ID = "sensor_data_sync"
        private const val NOTIF_ID = 100
        private const val MSG_PATH = "/sensor_data"
        
        // Upload configuration
        private const val UPLOAD_INTERVAL_MINUTES = 15  // Upload every 15 minutes
        private const val MIN_BATCHES_TO_UPLOAD = 5     // Or when we have 5+ batches
        
        // Batch metadata
        private const val BYTES_PER_SAMPLE = 21  // 8 (timestamp) + 1 (sensor type) + 12 (3 floats)
    }

    @Inject
    lateinit var sensorRepository: SensorRepository

    private val messageClient by lazy { Wearable.getMessageClient(this) }
    
    private var lastUploadTime = 0L
    private var receivedBatchCount = 0

    override fun onCreate() {
        super.onCreate()
        
        // Start as foreground service
        startForeground(NOTIF_ID, createNotification())
        
        // Register message listener to receive data from smartwatch
        messageClient.addListener(this)
        
        // Start periodic upload task
        startPeriodicUpload()
        
        Log.d(TAG, "SensorDataService started - listening for smartwatch data")
    }

    override fun onDestroy() {
        super.onDestroy()
        messageClient.removeListener(this)
        Log.d(TAG, "SensorDataService stopped")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == MSG_PATH) {
            lifecycleScope.launch {
                try {
                    val batchData = messageEvent.data
                    val sampleCount = batchData.size / BYTES_PER_SAMPLE
                    
                    Log.d(TAG, "Received batch from watch: ${batchData.size} bytes, ~$sampleCount samples")
                    
                    // Store in local database via repository
                    val batch = SensorBatchEntity(
                        timestamp = System.currentTimeMillis(),
                        deviceId = messageEvent.sourceNodeId,
                        batchData = batchData,
                        sampleCount = sampleCount,
                        uploaded = false
                    )
                    
                    sensorRepository.insertBatch(batch)
                    receivedBatchCount++
                    
                    // Update notification
                    updateNotification()
                    
                    // Check if we should upload now
                    checkAndUpload()
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing batch from watch", e)
                }
            }
        }
    }

    private fun startPeriodicUpload() {
        lifecycleScope.launch {
            while (true) {
                delay(UPLOAD_INTERVAL_MINUTES * 60 * 1000L)
                checkAndUpload()
            }
        }
    }

    private suspend fun checkAndUpload() {
        val unsentCount = sensorRepository.getUnsentCount()
        val timeSinceLastUpload = System.currentTimeMillis() - lastUploadTime
        val minutesSinceUpload = timeSinceLastUpload / (60 * 1000)
        
        // Upload if: 15+ minutes passed OR we have 5+ batches
        val shouldUpload = minutesSinceUpload >= UPLOAD_INTERVAL_MINUTES || 
                          unsentCount >= MIN_BATCHES_TO_UPLOAD
        
        if (shouldUpload && unsentCount > 0) {
            uploadBatchesToBackend()
        } else {
            Log.d(TAG, "Not uploading yet: $unsentCount batches, $minutesSinceUpload minutes since last upload")
        }
    }

    private suspend fun uploadBatchesToBackend() {
        try {
            val batches = sensorRepository.getUnsentBatches()
            
            if (batches.isEmpty()) {
                Log.d(TAG, "No batches to upload")
                return
            }
            
            Log.d(TAG, "Uploading ${batches.size} batches to backend...")
            
            val success = sensorRepository.uploadBatchesToBackend(batches)
            
            if (success) {
                // Mark batches as uploaded
                sensorRepository.markAsUploaded(batches.map { it.id })
                lastUploadTime = System.currentTimeMillis()
                
                Log.d(TAG, "Successfully uploaded ${batches.size} batches")
                updateNotification()
                
                // Clean up old uploaded data (keep last 7 days)
                val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
                sensorRepository.deleteOldUploaded(sevenDaysAgo)
            } else {
                Log.e(TAG, "Failed to upload batches - will retry later")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading batches", e)
        }
    }

    private fun createNotification(): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sensor Data Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Syncing sensor data from smartwatch to backend"
            }
            nm.createNotificationChannel(channel)
        }
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("HARbit Sync Active")
            .setContentText("Collecting sensor data from smartwatch")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        lifecycleScope.launch {
            val unsentCount = sensorRepository.getUnsentCount()
            val unsentSize = sensorRepository.getUnsentDataSize() ?: 0L
            val sizeMB = unsentSize / (1024.0 * 1024.0)
            
            val notification = NotificationCompat.Builder(this@SensorDataService, CHANNEL_ID)
                .setContentTitle("HARbit Sync Active")
                .setContentText("$unsentCount batches pending (${String.format("%.1f", sizeMB)} MB)")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .setOngoing(true)
                .build()
            
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIF_ID, notification)
        }
    }
}
