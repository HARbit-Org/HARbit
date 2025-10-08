package com.example.harbit.domain.model.enum

enum class SensorType(val value: Int) {
    ACCELEROMETER(1),
    GYROSCOPE(2);

    companion object {
        fun fromInt(value: Int) = entries.firstOrNull() { it.value == value }
    }
}