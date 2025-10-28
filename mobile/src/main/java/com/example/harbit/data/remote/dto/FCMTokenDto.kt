package com.example.harbit.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateFcmTokenRequest(
    val fcmToken: String
)

@Serializable
data class UpdateFcmTokenResponse(
    val status: String,
    val message: String
)
