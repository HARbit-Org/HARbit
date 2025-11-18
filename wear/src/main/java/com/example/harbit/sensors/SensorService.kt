package com.example.harbit.sensors

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import java.nio.ByteBuffer

class SensorService : LifecycleService(), SensorEventListener, MessageClient.OnMessageReceivedListener {

    companion object {
        private const val CHANNEL_ID = "sensor_stream"
        private const val NOTIF_ID = 1
        private const val MSG_PATH = "/sensor_data"
        private const val PING_PATH = "/ping"
        private const val PONG_PATH = "/pong"
        
        // 20 Hz sampling rate - optimized for consistent performance
        private const val SAMPLE_RATE_HZ = 20
        private const val SENSOR_PERIOD_US = (1000000 / SAMPLE_RATE_HZ) // 50,000 us = 20 Hz
        private const val MAX_LATENCY_US = 100_000  // ✅ 100ms latency (no HIGH_SAMPLING_RATE_SENSORS needed)

        // Format: [long timestamp][byte sensorType][float x][float y][float z]
        // sensorType: 1 = accelerometer, 2 = gyroscope
        private const val BYTES_PER_SAMPLE = 8 + 1 + 3 * 4 // timestamp + sensorType + 3 floats
        
        // batching ~1s at 20 Hz (20 samples)
        private const val BATCH_BYTES_BUDGET = 50 * 1024
        private const val BATCH_SAMPLES = BATCH_BYTES_BUDGET / BYTES_PER_SAMPLE
//        private const val BATCH_SAMPLES = 20
    }

    private lateinit var sensorManager: SensorManager
    private var accel: Sensor? = null
//    private var gyro: Sensor? = null

    private val msgClient by lazy { Wearable.getMessageClient(this) }
    private val nodeClient by lazy { Wearable.getNodeClient(this) }

    // ✅ WakeLock to prevent CPU sleep and maintain 20Hz
    private lateinit var wakeLock: PowerManager.WakeLock

//    private var batchBuf: ByteBuffer = ByteBuffer.allocate(BATCH_SAMPLES * BYTES_PER_SAMPLE * 2) // Twice the size to accommodate both sensors

    private var batchBuf: ByteBuffer = ByteBuffer.allocate(BATCH_SAMPLES * BYTES_PER_SAMPLE)
        .order(java.nio.ByteOrder.LITTLE_ENDIAN)
    private var batchCount = 0
    
    // ✅ Frequency monitoring
    private var lastSampleTime = 0L
    private var sampleCount = 0L
    private var frequencyCheckTime = System.currentTimeMillis()

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, makeNotification())
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        
        // ✅ Acquire WakeLock to keep CPU active for consistent 20Hz
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "HARbit::SensorCollection"
        ).apply {
            acquire()
            android.util.Log.d("SensorService", "WakeLock acquired - CPU will stay active")
        }
        
        // Get both accelerometer and gyroscope sensors
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        
        // Check if sensors exist
        if (accel == null) {
            android.util.Log.e("SensorService", "No accelerometer available!")
        }
        
//        if (gyro == null) {
//            android.util.Log.e("SensorService", "No gyroscope available!")
//        }

        // Add message listener for ping/pong from mobile
        msgClient.addListener(this)
        android.util.Log.d("SensorService", "Message listener registered for ping/pong")

        // Verify if we have connected devices before starting the sensors
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            if (nodes.isEmpty()) {
                android.util.Log.w("SensorService", "No connected nodes found!")
            } else {
                android.util.Log.d("SensorService", "Connected to ${nodes.size} node(s): ${nodes.joinToString { it.displayName }}")
            }
            
        // Register accelerometer immediately for consistent sampling
        accel?.let {
            val registered = sensorManager.registerListener(
                this, it, 
                SENSOR_PERIOD_US, // ✅ 50,000 microseconds = 20Hz
                100_000 // ✅ 100ms max latency (no HIGH_SAMPLING_RATE_SENSORS needed)
            )
            if (registered) {
                android.util.Log.d("SensorService", "✅ Accelerometer registered at ${SAMPLE_RATE_HZ}Hz with WakeLock")
            } else {
                android.util.Log.e("SensorService", "Failed to register accelerometer")
                stopSelf()
            }
        } ?: run {
            android.util.Log.e("SensorService", "No accelerometer available!")
            stopSelf()
        }
            
        }.addOnFailureListener { e ->
            android.util.Log.e("SensorService", "Failed to get connected nodes: ${e.message}")
            
            // Register both sensors anyway
            var sensorsRegistered = false
            
            accel?.let {
                sensorManager.registerListener(
                    this, it, SENSOR_PERIOD_US, MAX_LATENCY_US
                )
                sensorsRegistered = true
            }
            
//            gyro?.let {
//                sensorManager.registerListener(
//                    this, it, SENSOR_PERIOD_US, MAX_LATENCY_US
//                )
//                sensorsRegistered = true
//            }
            
            // Stop service if no sensors could be registered
            if (!sensorsRegistered) {
                android.util.Log.e("SensorService", "No sensors could be registered!")
                stopSelf()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d("SensorService", "Service is being destroyed")
        
        // ✅ Unregister sensor listener first
        sensorManager.unregisterListener(this)
        android.util.Log.d("SensorService", "Sensor listeners unregistered")
        
        // ✅ Remove message listener
        msgClient.removeListener(this)
        android.util.Log.d("SensorService", "Message listener removed")
        
        // ✅ Release WakeLock to save battery
        if (this::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
            android.util.Log.d("SensorService", "WakeLock released")
        }
        
        // ✅ Send any remaining data before shutdown
        if (batchCount > 0) {
            android.util.Log.d("SensorService", "Sending final batch with $batchCount samples before shutdown")
            sendAndReset()
        }
    }
    
    /**
     * Handle incoming messages from the mobile app (ping requests)
     */
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == PING_PATH) {
            android.util.Log.d("SensorService", "Ping received from mobile, sending pong...")
            // Respond with pong
            msgClient.sendMessage(messageEvent.sourceNodeId, PONG_PATH, ByteArray(0))
                .addOnSuccessListener {
                    android.util.Log.d("SensorService", "Pong sent successfully")
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("SensorService", "Failed to send pong: ${e.message}")
                }
        }
    }

    private fun makeNotification(): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Sensor streaming", NotificationManager.IMPORTANCE_LOW)
            )
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Streaming sensors")
            .setContentText("Sending accelerometer data to phone...")
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build()
    }

    override fun onSensorChanged(e: SensorEvent) {
        // Monitor actual sampling frequency
        val currentTime = System.currentTimeMillis()
        sampleCount++
        
        if (currentTime - frequencyCheckTime >= 5000) { // Check every 5 seconds
            val actualHz = sampleCount * 1000.0 / (currentTime - frequencyCheckTime)
            android.util.Log.d("SensorService", "📊 Actual sampling rate: ${String.format("%.1f", actualHz)} Hz (target: ${SAMPLE_RATE_HZ} Hz)")
            sampleCount = 0
            frequencyCheckTime = currentTime
        }
        
        // Use the sensor event's timestamp directly
        val timestamp = e.timestamp
        // val nowEpochMs = System.currentTimeMillis()
        // val nowElapsedNs = SystemClock.elapsedRealtimeNanos()
        // val epochMs = nowEpochMs - (nowElapsedNs - timestamp) / 1_000_000L
        
        // Check if we have enough space in buffer
        if (batchBuf.remaining() < BYTES_PER_SAMPLE) {
            sendAndReset()
        }
        
        // Only accelerometer (type 1)
        if (e.sensor.type != Sensor.TYPE_ACCELEROMETER) {
            return // Ignore other sensors
        }
        
        // Pack: [long timestamp][byte sensorType][float x][float y][float z]
        batchBuf.putLong(timestamp)
        batchBuf.put(1.toByte()) // Accelerometer type
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
        val samples = batchCount
        val payload = ByteArray(batchBuf.position())
        batchBuf.flip()
        batchBuf.get(payload)
        batchBuf.clear()
        batchCount = 0

        // Send to all connected nodes (phone) with added error handling
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            if (nodes.isEmpty()) {
                android.util.Log.w("SensorService", "No connected nodes found to send data to!")
                return@addOnSuccessListener
            }
            
            nodes.forEach { node ->
                msgClient.sendMessage(node.id, MSG_PATH, payload)
                    .addOnSuccessListener {
                        android.util.Log.d("SensorService", "Successfully sent batch of $samples sensor readings to ${node.displayName}")
                    }
                    .addOnFailureListener { e ->
                        android.util.Log.e("SensorService", "Failed to send data to ${node.displayName}: ${e.message}")
                    }
            }
        }.addOnFailureListener { e ->
            android.util.Log.e("SensorService", "Failed to get connected nodes: ${e.message}")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}