package com.example.harbit.data.remote.dto.request

import com.example.harbit.data.remote.dto.SensorBatchDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SensorBatchUploadRequest(
    @SerialName("userId")
    val userId: String,

    @SerialName("batches")
    val batches: List<SensorBatchDto>
)
