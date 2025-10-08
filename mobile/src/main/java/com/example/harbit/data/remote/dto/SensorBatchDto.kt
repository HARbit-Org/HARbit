package com.example.harbit.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SensorBatchDto(
    @SerialName("id")
    val id: String,

    @SerialName("device_id")
    val deviceId: String,

    @SerialName("timestamp")
    val timestamp: Long,

    @SerialName("sample_count")
    val sampleCount: Int,

    @SerialName("readings")
    val readings: List<SensorReadingDto>
)
