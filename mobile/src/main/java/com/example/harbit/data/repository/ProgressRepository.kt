package com.example.harbit.data.repository

import com.example.harbit.data.remote.dto.ProgressInsightDto

interface ProgressRepository {
    suspend fun getAllProgressInsights(): Result<List<ProgressInsightDto>>
}