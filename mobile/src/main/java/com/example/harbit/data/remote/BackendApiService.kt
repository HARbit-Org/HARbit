package com.example.harbit.data.remote

import com.example.harbit.data.remote.dto.SensorBatchUploadRequest
import com.example.harbit.data.remote.dto.SensorBatchUploadResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BackendApiService {
    
    @POST("api/v1/sensor-data")
    suspend fun uploadSensorData(
        @Body request: SensorBatchUploadRequest
    ): Response<SensorBatchUploadResponse>
}
