package com.example.harbit.domain.repository

import com.example.harbit.domain.model.SensorBatch
import com.example.harbit.domain.model.SensorReading
import kotlinx.coroutines.flow.Flow

interface SensorRepository {
    
    /**
     * Insert a new sensor batch into local storage
     */
    suspend fun insertBatch(deviceId: String, readings: List<SensorReading>)
    
    /**
     * Get all batches that haven't been uploaded yet
     */
    suspend fun getUnsentBatches(): List<SensorBatch>
    
    /**
     * Mark batches as uploaded
     */
    suspend fun markAsUploaded(batchIds: List<String>)
    
    /**
     * Get count of unsent batches
     */
    suspend fun getUnsentCount(): Int
    
    /**
     * Delete old uploaded batches
     */
    suspend fun deleteOldUploaded(beforeTimestamp: Long)
    
    /**
     * Upload batches to backend
     * @return true if upload was successful
     */
    suspend fun uploadBatchesToBackend(batches: List<SensorBatch>): Boolean
    
    /**
     * Get today's step count
     */
    fun getTodaySteps(): Flow<Int>
    
    /**
     * Get current heart rate
     */
    fun getCurrentHeartRate(): Flow<Int>
}
