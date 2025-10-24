package com.example.harbit.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity to cache activity distribution responses for offline access.
 * Stores the last successful API response for each date range.
 */
@Entity(
    tableName = "cached_activity_distribution",
    indices = [
        Index(value = ["user_id", "date_start", "date_end"])
    ]
)
data class CachedActivityDistributionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "date_start")
    val dateStart: String, // ISO date format (YYYY-MM-DD)
    
    @ColumnInfo(name = "date_end")
    val dateEnd: String, // ISO date format (YYYY-MM-DD)
    
    @ColumnInfo(name = "activity_label")
    val activityLabel: String,
    
    @ColumnInfo(name = "total_seconds")
    val totalSeconds: Double,
    
    @ColumnInfo(name = "total_minutes")
    val totalMinutes: Double,
    
    @ColumnInfo(name = "total_hours")
    val totalHours: Double,
    
    @ColumnInfo(name = "percentage")
    val percentage: Double,
    
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis()
)
