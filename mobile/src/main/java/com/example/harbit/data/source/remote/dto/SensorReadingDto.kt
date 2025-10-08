package com.example.harbit.data.source.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RawSensorReadingDto(
    val timestamp: Long,
    val x: Float,
    val y: Float,
    val z: Float
)