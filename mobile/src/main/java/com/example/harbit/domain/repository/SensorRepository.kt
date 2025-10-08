package com.example.harbit.domain.repository

import com.example.harbit.data.local.SensorBatchEntity
import kotlinx.coroutines.flow.Flow

interface SensorRepository {
    
    /**
     * Insert a new sensor batch into local storage
     */
    suspend fun insertBatch(batch: SensorBatchEntity): Long
    
    /**
     * Get all batches that haven't been uploaded yet
     */
    suspend fun getUnsentBatches(): List<SensorBatchEntity>
    
    /**
     * Mark batches as uploaded
     */
    suspend fun markAsUploaded(batchIds: List<Long>)
    
    /**
     * Get count of unsent batches
     */
    suspend fun getUnsentCount(): Int
    
    /**
     * Get total size of unsent data in bytes
     */
    suspend fun getUnsentDataSize(): Long?
    
    /**
     * Delete old uploaded batches
     */
    suspend fun deleteOldUploaded(olderThan: Long)
    
    /**
     * Upload batches to backend
     * @return true if upload was successful
     */
    suspend fun uploadBatchesToBackend(batches: List<SensorBatchEntity>): Boolean
    
    /**
     * Get today's step count
     */
    fun getTodaySteps(): Flow<Int>
    
    /**
     * Get current heart rate
     */
    fun getCurrentHeartRate(): Flow<Int>
}
