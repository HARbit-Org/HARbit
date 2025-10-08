package com.example.harbit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SensorBatchUploadRequest(
    val userId: String,
    val batches: List<BatchData>
)

@Serializable
data class BatchData(
    val timestamp: Long,
    val deviceId: String,
    val sampleCount: Int,
    val data: String  // Base64 encoded binary data
)

@Serializable
data class SensorBatchUploadResponse(
    val success: Boolean,
    val message: String? = null,
    val uploadedCount: Int
)
