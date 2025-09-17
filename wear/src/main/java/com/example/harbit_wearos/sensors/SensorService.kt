package com.example.harbit_wearos.sensors

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.google.android.gms.wearable.Wearable
import java.nio.ByteBuffer

class SensorService : LifecycleService(), SensorEventListener {

    companion object {
        private const val CHANNEL_ID = "gyro_stream"
        private const val NOTIF_ID = 1
        private const val MSG_PATH = "/gyro"

        // batching ~0.4s at 100 Hz (40 samples)
        private const val BATCH_SAMPLES = 40
        private const val BYTES_PER_SAMPLE = 8 + 3 * 4 // long timestamp + 3 floats
        private const val SENSOR_PERIOD_US = 10_000    // 100 Hz
        private const val MAX_LATENCY_US = 2_000_000   // batch up to ~2s at the framework level
    }

    private lateinit var sensorManager: SensorManager
    private var gyro: Sensor? = null

    private val msgClient by lazy { Wearable.getMessageClient(this) }
    private val nodeClient by lazy { Wearable.getNodeClient(this) }

    private var batchBuf: ByteBuffer = ByteBuffer.allocate(BATCH_SAMPLES * BYTES_PER_SAMPLE)
    private var batchCount = 0

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, makeNotification())
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Add message listener for connection validation from mobile
        Wearable.getMessageClient(this).addListener { messageEvent ->
            if (messageEvent.path == "/mobile-connected") {
                android.util.Log.d("GyroService", "Mobile connection confirmed from ${messageEvent.sourceNodeId}")
                // Send a small test payload to confirm bidirectional communication
                val testPayload = ByteBuffer.allocate(BYTES_PER_SAMPLE).apply {
                    putLong(System.nanoTime())
                    putFloat(0f)
                    putFloat(0f)
                    putFloat(0f)
                }.array()
                
                msgClient.sendMessage(messageEvent.sourceNodeId, MSG_PATH, testPayload)
                    .addOnSuccessListener {
                        android.util.Log.d("GyroService", "Sent test data to confirm connection")
                    }
            }
        }

        // Verify if we have connected devices before starting the sensor
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            if (nodes.isEmpty()) {
                android.util.Log.w("GyroService", "No connected nodes found!")
            } else {
                android.util.Log.d("GyroService", "Connected to ${nodes.size} node(s): ${nodes.joinToString { it.displayName }}")
            }
            
            // Start the sensor anyway, as the phone might connect later
            gyro?.let {
                sensorManager.registerListener(
                    this, it, SENSOR_PERIOD_US, MAX_LATENCY_US
                )
            } ?: stopSelf() // no gyro available
        }.addOnFailureListener { e ->
            android.util.Log.e("GyroService", "Failed to get connected nodes: ${e.message}")
            
            // Start the sensor anyway
            gyro?.let {
                sensorManager.registerListener(
                    this, it, SENSOR_PERIOD_US, MAX_LATENCY_US
                )
            } ?: stopSelf() // no gyro available
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        // No need to explicitly remove message listeners in a service that's being destroyed
        android.util.Log.d("GyroService", "Service is being destroyed")
    }

    private fun makeNotification(): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Gyro streaming", NotificationManager.IMPORTANCE_LOW)
            )
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Streaming gyroscope")
            .setContentText("Sending data to phone…")
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build()
    }

    override fun onSensorChanged(e: SensorEvent) {
        // Pack: [long timestamp][float x][float y][float z]
        android.util.Log.d("GyroService", "timestamp: ${e.timestamp}, x: ${e.values[0]}, y: ${e.values[1]}, z: ${e.values[2]}")
        if (batchBuf.remaining() < BYTES_PER_SAMPLE) {
            sendAndReset()
        }
        batchBuf.putLong(e.timestamp)
        batchBuf.putFloat(e.values[0])
        batchBuf.putFloat(e.values[1])
        batchBuf.putFloat(e.values[2])
        batchCount++

        if (batchCount >= BATCH_SAMPLES) {
            sendAndReset()
        }
    }

    private fun sendAndReset() {
        if (batchCount == 0) return
        val payload = ByteArray(batchBuf.position())
        batchBuf.flip()
        batchBuf.get(payload)
        batchBuf.clear()
        batchCount = 0

        // Send to all connected nodes (phone) with added error handling
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            if (nodes.isEmpty()) {
                android.util.Log.w("GyroService", "No connected nodes found to send data to!")
                return@addOnSuccessListener
            }
            
            nodes.forEach { node ->
                msgClient.sendMessage(node.id, MSG_PATH, payload)
                    .addOnSuccessListener {
                        android.util.Log.d("GyroService", "Successfully sent data to ${node.displayName}")
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("GyroService", "Failed to send data to ${node.displayName}: ${e.message}")
                    }
            }
        }.addOnFailureListener { e ->
            android.util.Log.e("GyroService", "Failed to get connected nodes: ${e.message}")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}