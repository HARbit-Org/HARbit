package com.example.harbit.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SensorReadingDto(
    @SerialName("timestamp")
    val timestamp: Long,

    @SerialName("sensor_type")
    val sensorType: Int,  // 1 = accel, 2 = gyro

    @SerialName("x")
    val x: Float,

    @SerialName("y")
    val y: Float,

    @SerialName("z")
    val z: Float
)
