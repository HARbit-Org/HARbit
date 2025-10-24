package com.example.harbit.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
//import android.content.ContentValues
//import android.content.Context
//import android.os.Environment
//import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.harbit.R
import com.example.harbit.domain.model.SensorReading
import com.example.harbit.domain.model.enum.SensorType
import com.example.harbit.domain.repository.SensorRepository
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
//import kotlinx.serialization.encodeToString
//import kotlinx.serialization.json.Json
//import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
import javax.inject.Inject
import kotlin.collections.mutableListOf

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
                    val payload = messageEvent.data
                    val readings = parseSensorData(payload)

                    Log.d(TAG, "Received ${readings.size} sensor readings from watch")

                    // Store parsed readings via repository
                    sensorRepository.insertBatch(
                        deviceId = messageEvent.sourceNodeId,
                        readings = readings
                    )

                    receivedBatchCount++
                    updateNotification()
                    checkAndUpload()

                } catch (e: Exception) {
                    Log.e(TAG, "Error processing batch from watch", e)
                }
            }
        }
    }

//    private fun downloadJson(readings: List<SensorReading>, context: Context) {
//        try {
//            if (readings.isEmpty()) {
//                Log.w(TAG, "No readings to export")
//                return
//            }
//
//            val json = Json { prettyPrint = true }
//            val jsonString = json.encodeToString(readings)
//
//            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
//            val filename = "sensor_data_${dateFormat.format(Date())}.json"
//
//            val resolver = context.contentResolver
//            val contentValues = ContentValues().apply {
//                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
//                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
//                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
//            }
//
//            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
//            uri?.let {
//                resolver.openOutputStream(it)?.use { outputStream ->
//                    outputStream.write(jsonString.toByteArray())
//                }
//                Log.d(TAG, "JSON file saved to Downloads: $filename (${readings.size} samples)")
//            } ?: run {
//                Log.e(TAG, "Failed to create file in Downloads")
//            }
//        } catch (e: IOException) {
//            Log.e(TAG, "Error saving JSON file", e)
//        } catch (e: Exception) {
//            Log.e(TAG, "Unexpected error during JSON export", e)
//        }
//    }

    private fun parseSensorData(payload: ByteArray): List<SensorReading> {
        val readings = mutableListOf<SensorReading>()
        val buf = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN)

        while (buf.remaining() >= 21) {  // 8 + 1 + 12
            val timestamp = buf.long
            val sensorTypeByte = buf.get()
            val x = buf.float
            val y = buf.float
            val z = buf.float

            val sensorType = SensorType.Companion.fromInt(sensorTypeByte.toInt())

            if (sensorType != null) {
                readings.add(
                    SensorReading(
                        timestamp = timestamp,
                        sensorType = sensorType,
                        x = x,
                        y = y,
                        z = z
                    )
                )
            }
        }

        // Service is already a Context, use 'this@SensorDataService'
//        downloadJson(readings, this@SensorDataService)

        return readings
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
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

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
            .setContentTitle("HARbit")
            .setContentText("Conectado")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        lifecycleScope.launch {
            val unsentCount = sensorRepository.getUnsentCount()

            val notification = NotificationCompat.Builder(this@SensorDataService, CHANNEL_ID)
                .setContentTitle("HARbit")
                .setContentText("Conectado")
                .setSmallIcon(R.drawable.ic_notification)
                .setOngoing(true)
                .build()

            val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIF_ID, notification)
        }
    }
}