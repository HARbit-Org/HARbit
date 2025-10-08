package com.example.harbit.data.repository

import android.util.Log
import com.example.harbit.data.local.SensorBatchDao
import com.example.harbit.data.local.SensorBatchEntity
import com.example.harbit.data.remote.BackendApiService
import com.example.harbit.data.remote.dto.BatchData
import com.example.harbit.data.remote.dto.SensorBatchUploadRequest
import com.example.harbit.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepositoryImpl @Inject constructor(
    private val sensorBatchDao: SensorBatchDao,
    private val apiService: BackendApiService
) : SensorRepository {
    
    companion object {
        private const val TAG = "SensorRepository"
    }
    
    override suspend fun insertBatch(batch: SensorBatchEntity): Long {
        return sensorBatchDao.insertBatch(batch)
    }
    
    override suspend fun getUnsentBatches(): List<SensorBatchEntity> {
        return sensorBatchDao.getUnsentBatches()
    }
    
    override suspend fun markAsUploaded(batchIds: List<Long>) {
        sensorBatchDao.markAsUploaded(batchIds)
    }
    
    override suspend fun getUnsentCount(): Int {
        return sensorBatchDao.getUnsentCount()
    }
    
    override suspend fun getUnsentDataSize(): Long? {
        return sensorBatchDao.getUnsentDataSize()
    }
    
    override suspend fun deleteOldUploaded(olderThan: Long) {
        sensorBatchDao.deleteOldUploaded(olderThan)
    }
    
    override suspend fun uploadBatchesToBackend(batches: List<SensorBatchEntity>): Boolean {
        return try {
            // TODO: Get actual user ID from auth system
            val userId = "temp_user_id"
            
            val request = SensorBatchUploadRequest(
                userId = userId,
                batches = batches.map { batch ->
                    BatchData(
                        timestamp = batch.timestamp,
                        deviceId = batch.deviceId,
                        sampleCount = batch.sampleCount,
                        data = android.util.Base64.encodeToString(
                            batch.batchData,
                            android.util.Base64.NO_WRAP
                        )
                    )
                }
            )
            
            val response = apiService.uploadSensorData(request)
            
            if (response.isSuccessful) {
                Log.d(TAG, "Successfully uploaded ${batches.size} batches")
                true
            } else {
                Log.e(TAG, "Upload failed: ${response.code()} ${response.message()}")
                false
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Upload error", e)
            false
        }
    }
    
    override fun getTodaySteps(): Flow<Int> = flow {
        // TODO: Implement when you have steps data processing
        emit(0)
    }
    
    override fun getCurrentHeartRate(): Flow<Int> = flow {
        // TODO: Implement when you have heart rate data processing
        emit(0)
    }
}
