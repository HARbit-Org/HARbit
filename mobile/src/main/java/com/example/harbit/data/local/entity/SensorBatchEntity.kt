package com.example.harbit.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_batches")
data class SensorBatchEntity(
    @PrimaryKey
    val id: String,

    val deviceId: String,                                 // Smartwatch device ID
    val timestamp: Long,                                  // When batch was received
    val sampleCount: Int,                                 // Calculated from readings

    val uploaded: Boolean = false,                        // Has this been sent to backend?
    val createdAt: Long = System.currentTimeMillis()      // When was it uploaded?
)