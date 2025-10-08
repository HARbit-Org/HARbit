package com.example.harbit.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sensor_readings",
    indices = [
        Index(value = ["batchId"]),
        Index(value = ["timestamp"]),
        Index(value = ["sensorType"])
    ]
)
data class SensorReadingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val batchId: String, // Group readings by batch
    val timestamp: Long,
    val sensorType: Int, // 1 = accel, 2 = gyro
    val x: Float,
    val y: Float,
    val z: Float,

    val uploaded: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
