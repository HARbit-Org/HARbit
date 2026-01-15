package com.example.harbit.domain.model

data class Alert(
    val type: AlertType,
    val message: String,
    val priority: Int // 0 = highest priority
)