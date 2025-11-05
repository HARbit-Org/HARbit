package com.example.harbit.domain

import com.example.harbit.domain.model.AlertType

data class Alert(
    val type: AlertType,
    val message: String,
    val priority: Int // 0 = highest priority
)
