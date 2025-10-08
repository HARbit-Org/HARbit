package com.example.harbit.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SensorBatchUploadResponse(
    @SerialName("status")
    val status: String
)
