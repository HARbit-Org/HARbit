package com.example.harbit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_batches")
data class SensorBatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,              // When batch was received
    val deviceId: String,             // Smartwatch device ID
    val batchData: ByteArray,         // Raw sensor data from watch (50KB)
    val sampleCount: Int,             // Number of sensor readings in batch
    val uploaded: Boolean = false,    // Has this been sent to backend?
    val uploadedAt: Long? = null      // When was it uploaded?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorBatchEntity

        if (id != other.id) return false
        if (!batchData.contentEquals(other.batchData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + batchData.contentHashCode()
        return result
    }
}
