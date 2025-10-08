package com.example.harbit.data.source.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RawSensorDto (
    val accel: List<RawSensorReadingDto>
)