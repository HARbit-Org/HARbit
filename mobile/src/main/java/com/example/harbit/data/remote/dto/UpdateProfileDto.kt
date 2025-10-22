package com.example.harbit.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileDto(
    @SerialName("display_name")
    val displayName: String? = null,
    
    @SerialName("preferred_email")
    val preferredEmail: String? = null,
    
    @SerialName("phone")
    val phone: String? = null,
    
    @SerialName("sex")
    val sex: String? = null,
    
    @SerialName("birth_year")
    val birthYear: Int? = null,
    
    @SerialName("daily_step_goal")
    val dailyStepGoal: Int? = null,
    
    @SerialName("timezone")
    val timezone: String? = null,
    
    @SerialName("height")
    val height: Float? = null,
    
    @SerialName("weight")
    val weight: Float? = null
)
