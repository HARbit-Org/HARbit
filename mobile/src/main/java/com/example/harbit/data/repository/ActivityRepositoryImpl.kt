package com.example.harbit.data.repository

import android.util.Log
import com.example.harbit.data.local.dao.ActivityDistribution
import com.example.harbit.data.local.dao.ProcessedActivityDao
import com.example.harbit.data.local.entity.ProcessedActivityEntity
import com.example.harbit.data.local.preferences.AuthPreferencesRepository
import com.example.harbit.data.remote.service.ActivityApiService
import com.example.harbit.util.getCurrentTimezoneOffsetMinutes
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepositoryImpl @Inject constructor(
    private val activityApiService: ActivityApiService,
    private val processedActivityDao: ProcessedActivityDao,
    private val authPreferences: AuthPreferencesRepository
) : ActivityRepository {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override suspend fun getLocalActivityDistribution(
        userId: String,
        dateStart: LocalDate,
        dateEnd: LocalDate
    ): List<ActivityDistribution> {
        val startTime = dateStart.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val endTime = dateEnd.plusDays(1).atStartOfDay(ZoneOffset.UTC).minusNanos(1).toInstant().toEpochMilli()
        
        return processedActivityDao.getActivityDistribution(
            userId = userId,
            startTime = startTime,
            endTime = endTime
        )
    }

    override suspend fun fetchAndSyncActivityDistribution(
        dateStart: LocalDate,
        dateEnd: LocalDate
    ): Result<List<ActivityDistribution>> {
        return try {
            // Get user ID from preferences
            val userId = authPreferences.userEmail.first() // You might want to use userId instead
            if (userId == null) {
                return Result.failure(Exception("User not logged in"))
            }
            
            // Get timezone offset in minutes from UTC
            val timezoneOffset = getCurrentTimezoneOffsetMinutes()
            
            Log.d("ActivityRepository", "Fetching activity distribution for range: $dateStart to $dateEnd (timezone offset: $timezoneOffset minutes)")
            
            // Call backend API with date range and timezone offset
            val response = activityApiService.getActivityDistribution(
                dateStart = dateStart.format(dateFormatter),
                dateEnd = dateEnd.format(dateFormatter),
                timezoneOffset = timezoneOffset
            )
            
            if (response.isSuccessful && response.body() != null) {
                val distributionResponse = response.body()!!
                
                Log.d("ActivityRepository", "Received ${distributionResponse.activities.size} activities")
                
                // Convert DTOs to entities and save to local database
                // Note: This assumes the backend returns the detailed activity records
                // If not, you'll need a separate endpoint to fetch the raw activity data
                
                // For now, just return the distribution from the API response
                // You would typically sync the raw processed_activities table here
                
                // Convert API response to ActivityDistribution format
                val distributions = distributionResponse.activities.map { item ->
                    ActivityDistribution(
                        activityLabel = item.activityLabel,
                        totalSeconds = item.totalSeconds,
                        totalMinutes = item.totalMinutes,
                        totalHours = item.totalHours,
                        percentage = item.percentage
                    )
                }
                
                Result.success(distributions)
            } else {
                Log.e("ActivityRepository", "API error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("Failed to fetch activity distribution: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ActivityRepository", "Error fetching activity distribution", e)
            Result.failure(e)
        }
    }

    override suspend fun getTotalActivityHours(
        userId: String,
        dateStart: LocalDate,
        dateEnd: LocalDate
    ): Double {
        val distribution = getLocalActivityDistribution(userId, dateStart, dateEnd)
        return distribution.sumOf { it.totalHours }
    }

    /**
     * Helper function to get start and end timestamps for a specific date (in UTC).
     * Kept for backward compatibility if needed.
     */
    private fun getDateRange(date: LocalDate): Pair<Long, Long> {
        val startOfDay = date.atStartOfDay(ZoneOffset.UTC)
        val endOfDay = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).minusNanos(1)
        
        return Pair(
            startOfDay.toInstant().toEpochMilli(),
            endOfDay.toInstant().toEpochMilli()
        )
    }
}
