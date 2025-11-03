package com.example.harbit.data.repository

import android.util.Log
import com.example.harbit.data.local.preferences.AuthPreferencesRepository
import com.example.harbit.data.remote.dto.ProgressInsightDto
import com.example.harbit.data.remote.service.ProgressApiService
import kotlinx.coroutines.flow.first
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val progressApiService: ProgressApiService,
    private val authPreferences: AuthPreferencesRepository
) : ProgressRepository {

    override suspend fun getAllProgressInsights(): Result<List<ProgressInsightDto>> {
        return try {
            val userId = authPreferences.userId.first()
            if (userId == null) {
                return Result.failure(Exception("User not logged in"))
            }

            val response = progressApiService.getProgressInsights()

            if (response.isSuccessful && response.body() != null) {
                val insightResponse = response.body()!!

                Result.success(insightResponse)
            } else {
                Log.e("ProgressRepository", "API error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("Failed to fetch progress insights: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ProgressRepository", "Error fetching progress insights from API", e)
            Result.failure(e)
        }
    }
}