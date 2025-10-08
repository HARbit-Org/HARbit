package com.example.harbit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.harbit.data.local.entity.SensorReadingEntity

@Dao
interface SensorReadingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReadings(readings: List<SensorReadingEntity>)

    @Query("SELECT * FROM sensor_readings WHERE batchId = :batchId ORDER BY timestamp ASC")
    suspend fun getReadingsForBatch(batchId: String): List<SensorReadingEntity>

    @Query("SELECT * FROM sensor_readings WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    suspend fun getReadingsInRange(startTime: Long, endTime: Long): List<SensorReadingEntity>

    @Query("SELECT * FROM sensor_readings WHERE sensorType = :sensorType AND timestamp >= :startTime ORDER BY timestamp ASC")
    suspend fun getReadingsBySensorType(sensorType: Int, startTime: Long): List<SensorReadingEntity>

    @Query("DELETE FROM sensor_readings WHERE batchId IN (SELECT id FROM sensor_batches WHERE uploaded = 1 AND createdAt < :beforeTimestamp)")
    suspend fun deleteOldReadings(beforeTimestamp: Long)

    @Query("SELECT COUNT(*) FROM sensor_readings WHERE uploaded = 0")
    suspend fun getUnsentCount(): Int

    @Query("UPDATE sensor_readings SET uploaded = 1 WHERE batchId IN (:batchIds)")
    suspend fun markAsUploaded(batchIds: List<String>)
}