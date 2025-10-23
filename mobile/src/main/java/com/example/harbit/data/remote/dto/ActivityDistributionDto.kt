package com.example.harbit.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActivityDistributionItemDto(
    @SerialName("activity_label")
    val activityLabel: String,
    
    @SerialName("total_seconds")
    val totalSeconds: Double,
    
    @SerialName("total_minutes")
    val totalMinutes: Double,
    
    @SerialName("total_hours")
    val totalHours: Double,
    
    @SerialName("percentage")
    val percentage: Double
)

@Serializable
data class ActivityDistributionResponseDto(
    @SerialName("user_id")
    val userId: String,
    
    @SerialName("start_time")
    val startTime: String,
    
    @SerialName("end_time")
    val endTime: String,
    
    @SerialName("activities")
    val activities: List<ActivityDistributionItemDto>,
    
    @SerialName("total_hours")
    val totalHours: Double
)
