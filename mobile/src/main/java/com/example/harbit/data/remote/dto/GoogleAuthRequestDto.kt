package com.example.harbit.data.remote.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthRequestDto(
    @SerialName("id_token")
    val idToken: String,
    
    @EncodeDefault
    @SerialName("client_type")
    val clientType: String = "android"
)
