package com.example.harbit.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProgressInsightDto(
    @SerialName("id")
    val id: String,

    @SerialName("userId")
    val userId: String,

    @SerialName("type")
    val type: String,

    @SerialName("category")
    val category: String,

    @SerialName("message_title")
    val title: String,

    @SerialName("message_body")
    val body: String,

    @SerialName("created_at")
    val createdAtStr: String
)