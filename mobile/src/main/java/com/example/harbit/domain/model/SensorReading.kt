package com.example.harbit.domain.model

import com.example.harbit.domain.model.enum.SensorType
import kotlinx.serialization.Serializable

@Serializable
data class SensorReading(
    val timestamp: Long,
    val sensorType: SensorType,
    val x: Float,
    val y: Float,
    val z: Float
)