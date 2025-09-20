package com.example.harbit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gyro")
data class GyroEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val t: Long,  // sensor ns timestamp
    val x: Float,
    val y: Float,
    val z: Float
)