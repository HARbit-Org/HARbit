package com.example.harbit.data.remote.service

import com.example.harbit.data.remote.dto.ProgressInsightDto
import retrofit2.Response
import retrofit2.http.GET

interface ProgressApiService {
    @GET("/progress/insights")
    suspend fun getProgressInsights(): Response<List<ProgressInsightDto>>
}