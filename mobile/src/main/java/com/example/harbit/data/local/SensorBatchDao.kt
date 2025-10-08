package com.example.harbit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SensorBatchDao {
    
    @Insert
    suspend fun insertBatch(batch: SensorBatchEntity): Long
    
    @Query("SELECT * FROM sensor_batches WHERE uploaded = 0 ORDER BY timestamp ASC")
    suspend fun getUnsentBatches(): List<SensorBatchEntity>
    
    @Query("UPDATE sensor_batches SET uploaded = 1, uploadedAt = :uploadedAt WHERE id IN (:batchIds)")
    suspend fun markAsUploaded(batchIds: List<Long>, uploadedAt: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM sensor_batches WHERE uploaded = 0")
    suspend fun getUnsentCount(): Int
    
    @Query("SELECT SUM(LENGTH(batchData)) FROM sensor_batches WHERE uploaded = 0")
    suspend fun getUnsentDataSize(): Long?
    
    @Query("DELETE FROM sensor_batches WHERE uploaded = 1 AND uploadedAt < :olderThan")
    suspend fun deleteOldUploaded(olderThan: Long)
    
    @Query("SELECT * FROM sensor_batches ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestBatch(): SensorBatchEntity?
}
