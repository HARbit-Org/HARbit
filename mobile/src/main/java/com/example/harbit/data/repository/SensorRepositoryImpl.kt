package com.example.harbit.data.repository

import android.util.Log
import com.example.harbit.data.local.dao.SensorBatchDao
import com.example.harbit.data.local.dao.SensorReadingDao
import com.example.harbit.data.local.entity.SensorBatchEntity
import com.example.harbit.data.local.entity.SensorReadingEntity
import com.example.harbit.data.local.preferences.AuthPreferencesRepository
import com.example.harbit.data.remote.dto.SensorBatchDto
import com.example.harbit.data.remote.dto.SensorReadingDto
import com.example.harbit.data.remote.dto.request.SensorBatchUploadRequest
import com.example.harbit.data.remote.service.BackendApiService
import com.example.harbit.domain.events.SensorDataEvents
import com.example.harbit.domain.model.SensorBatch
import com.example.harbit.domain.model.SensorReading
import com.example.harbit.domain.model.enum.SensorType
import com.example.harbit.domain.repository.SensorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.util.UUID
import javax.inject.Inject

class SensorRepositoryImpl @Inject constructor(
    private val batchDao: SensorBatchDao,
    private val readingDao: SensorReadingDao,
    private val apiService: BackendApiService,
    private val authPreferences: AuthPreferencesRepository,
    private val sensorDataEvents: SensorDataEvents
) : SensorRepository {

    override suspend fun insertBatch(deviceId: String, readings: List<SensorReading>) {
        val batchId = UUID.randomUUID().toString()

        // Insert batch metadata
        val batch = SensorBatchEntity(
            id = batchId,
            deviceId = deviceId,
            timestamp = System.currentTimeMillis(),
            sampleCount = readings.size,
            uploaded = false
        )
        batchDao.insertBatch(batch)

        // Insert individual readings
        val readingEntities = readings.map { reading ->
            SensorReadingEntity(
                batchId = batchId,
                timestamp = reading.timestamp,
                sensorType = reading.sensorType.value,
                x = reading.x,
                y = reading.y,
                z = reading.z,
                uploaded = false
            )
        }
        readingDao.insertReadings(readingEntities)

        Log.d("SensorRepo", "Inserted batch $batchId with ${readings.size} readings")
    }

    override suspend fun getUnsentBatches(): List<SensorBatch> {
        val batches = batchDao.getUnsentBatches()

        return batches.map { batch ->
            val readingEntities = readingDao.getReadingsForBatch(batch.id)
            val readings = readingEntities.map { entity ->
                SensorReading(
                    timestamp = entity.timestamp,
                    sensorType = SensorType.fromInt(entity.sensorType) ?: SensorType.ACCELEROMETER,
                    x = entity.x,
                    y = entity.y,
                    z = entity.z
                )
            }

            SensorBatch(
                id = batch.id,
                deviceId = batch.deviceId,
                batchTimestamp = batch.timestamp,
                readings = readings
            )
        }
    }

    override suspend fun uploadBatchesToBackend(batches: List<SensorBatch>): Boolean {
        return try {
            val batchDtos = batches.map { batch ->
                SensorBatchDto(
                    id = batch.id,
                    timestamp = batch.batchTimestamp,
                    deviceId = batch.deviceId,
                    sampleCount = batch.readings.size,
                    readings = batch.readings.map { reading ->
                        SensorReadingDto(
                            timestamp = reading.timestamp,
                            sensorType = reading.sensorType.value,
                            x = reading.x,
                            y = reading.y,
                            z = reading.z
                        )
                    }
                )
            }

            // Get authenticated user ID
            val userId = authPreferences.userId.first()
            if (userId == null) {
                Log.e("SensorRepo", "No user ID available for upload")
                return false
            }

            val request = SensorBatchUploadRequest(
                userId = userId,
                batches = batchDtos
            )

            val response = apiService.uploadSensorData(request)
            val body = response.body()
            Log.d("SensorRepo", "Upload successful: ${body?.status}")
            
            // Notify that new data has been uploaded successfully
            if (response.isSuccessful && body?.status == "success") {
                sensorDataEvents.notifyDataUploaded()
                Log.d("SensorRepo", "Notified data upload event")
            }
            
            true
        } catch (e: Exception) {
            Log.e("SensorRepo", "Upload failed", e)
            false
        }
    }

    override suspend fun markAsUploaded(batchIds: List<String>) {
        batchDao.markAsUploaded(batchIds)
        readingDao.markAsUploaded(batchIds)
    }

    override suspend fun getUnsentCount(): Int {
        return batchDao.getUnsentCount()
    }

    override suspend fun deleteOldUploaded(beforeTimestamp: Long) {
        batchDao.deleteOldUploaded(beforeTimestamp)
        readingDao.deleteOldReadings(beforeTimestamp)
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
