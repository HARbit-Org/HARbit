package com.example.harbit.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id")
    val id: String, // UUID as string
    
    @SerialName("email")
    val email: String,
    
    @SerialName("display_name")
    val displayName: String?,

    @SerialName("preferred_email")
    val preferredEmail: String?,

    @SerialName("phone")
    val phone: String?,

    @SerialName("height")
    val height: Float?,

    @SerialName("weight")
    val weight: Float?,
    
    @SerialName("picture_url")
    val pictureUrl: String?,
    
    @SerialName("sex")
    val sex: String? = null,
    
    @SerialName("birth_year")
    val birthYear: Int? = null,
    
    @SerialName("daily_step_goal")
    val dailyStepGoal: Int = 10000,
    
    @SerialName("timezone")
    val timezone: String = "America/Lima"
)
