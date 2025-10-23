package com.example.harbit.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processed_activities")
data class ProcessedActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "ts_start")
    val tsStart: Long,  // Unix timestamp in milliseconds
    
    @ColumnInfo(name = "ts_end")
    val tsEnd: Long,    // Unix timestamp in milliseconds
    
    @ColumnInfo(name = "activity_label")
    val activityLabel: String,
    
    @ColumnInfo(name = "model_version")
    val modelVersion: String?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long  // Unix timestamp in milliseconds
)
