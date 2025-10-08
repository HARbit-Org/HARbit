package com.example.harbit.domain.model

data class SensorBatch(
    val id: String,
    val deviceId: String,
    val batchTimestamp: Long,
    val readings: List<SensorReading>
)
