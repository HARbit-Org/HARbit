package com.example.harbit.data.repository

import android.util.Log
import com.example.harbit.data.local.dao.ActivityDistribution
import com.example.harbit.data.local.dao.CachedActivityDistributionDao
import com.example.harbit.data.local.dao.ProcessedActivityDao
import com.example.harbit.data.local.entity.CachedActivityDistributionEntity
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
    private val cachedActivityDistributionDao: CachedActivityDistributionDao,
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
            val userId = authPreferences.userId.first()
            if (userId == null) {
                return Result.failure(Exception("User not logged in"))
            }
            
            // FIRST: Try to load cached data immediately (for instant display)
            val cachedData = getCachedDistribution(userId, dateStart, dateEnd)
            if (cachedData.isNotEmpty()) {
                Log.d("ActivityRepository", "Found cached distribution (${cachedData.size} items), returning immediately")
                // Return cached data immediately - ViewModel can update UI with this
                // The caller can choose to refresh in the background if needed
                return Result.success(cachedData)
            }
            
            // If no cache, try to fetch from API
            Log.d("ActivityRepository", "No cached data, fetching from API...")
            
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
                
                Log.d("ActivityRepository", "Received ${distributionResponse.activities.size} activities from API")
                
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
                
                // Cache the successful response
                try {
                    val dateStartStr = dateStart.format(dateFormatter)
                    val dateEndStr = dateEnd.format(dateFormatter)
                    
                    // Delete old cache for this date range
                    cachedActivityDistributionDao.deleteCachedDistribution(userId, dateStartStr, dateEndStr)
                    
                    // Save new cache
                    val cachedEntities = distributionResponse.activities.map { item ->
                        CachedActivityDistributionEntity(
                            userId = userId,
                            dateStart = dateStartStr,
                            dateEnd = dateEndStr,
                            activityLabel = item.activityLabel,
                            totalSeconds = item.totalSeconds,
                            totalMinutes = item.totalMinutes,
                            totalHours = item.totalHours,
                            percentage = item.percentage
                        )
                    }
                    cachedActivityDistributionDao.insertCachedDistributions(cachedEntities)
                    Log.d("ActivityRepository", "Cached ${cachedEntities.size} distribution items")
                } catch (cacheError: Exception) {
                    Log.e("ActivityRepository", "Failed to cache distribution", cacheError)
                    // Don't fail the whole request if caching fails
                }
                
                Result.success(distributions)
            } else {
                Log.e("ActivityRepository", "API error: ${response.code()} - ${response.message()}")
                Result.failure(Exception("Failed to fetch activity distribution: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("ActivityRepository", "Error fetching activity distribution from API", e)
            Result.failure(e)
        }
    }
    
    /**
     * Refresh activity distribution in the background after initial cached load.
     * This should be called after the ViewModel has already displayed cached data.
     */
    override suspend fun refreshActivityDistributionInBackground(
        dateStart: LocalDate,
        dateEnd: LocalDate
    ): Result<List<ActivityDistribution>> {
        return try {
            val userId = authPreferences.userId.first()
            if (userId == null) {
                return Result.failure(Exception("User not logged in"))
            }
            
            val timezoneOffset = getCurrentTimezoneOffsetMinutes()
            
            Log.d("ActivityRepository", "Background refresh for range: $dateStart to $dateEnd")
            
            val response = activityApiService.getActivityDistribution(
                dateStart = dateStart.format(dateFormatter),
                dateEnd = dateEnd.format(dateFormatter),
                timezoneOffset = timezoneOffset
            )
            
            if (response.isSuccessful && response.body() != null) {
                val distributionResponse = response.body()!!
                
                Log.d("ActivityRepository", "Background refresh received ${distributionResponse.activities.size} activities")
                
                val distributions = distributionResponse.activities.map { item ->
                    ActivityDistribution(
                        activityLabel = item.activityLabel,
                        totalSeconds = item.totalSeconds,
                        totalMinutes = item.totalMinutes,
                        totalHours = item.totalHours,
                        percentage = item.percentage
                    )
                }
                
                // Update cache
                try {
                    val dateStartStr = dateStart.format(dateFormatter)
                    val dateEndStr = dateEnd.format(dateFormatter)
                    
                    cachedActivityDistributionDao.deleteCachedDistribution(userId, dateStartStr, dateEndStr)
                    
                    val cachedEntities = distributionResponse.activities.map { item ->
                        CachedActivityDistributionEntity(
                            userId = userId,
                            dateStart = dateStartStr,
                            dateEnd = dateEndStr,
                            activityLabel = item.activityLabel,
                            totalSeconds = item.totalSeconds,
                            totalMinutes = item.totalMinutes,
                            totalHours = item.totalHours,
                            percentage = item.percentage
                        )
                    }
                    cachedActivityDistributionDao.insertCachedDistributions(cachedEntities)
                    Log.d("ActivityRepository", "Background refresh updated cache")
                } catch (cacheError: Exception) {
                    Log.e("ActivityRepository", "Failed to update cache", cacheError)
                }
                
                Result.success(distributions)
            } else {
                Log.w("ActivityRepository", "Background refresh failed: ${response.code()}")
                Result.failure(Exception("Background refresh failed"))
            }
        } catch (e: Exception) {
            Log.w("ActivityRepository", "Background refresh error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get cached distribution from local database.
     */
    private suspend fun getCachedDistribution(
        userId: String,
        dateStart: LocalDate,
        dateEnd: LocalDate
    ): List<ActivityDistribution> {
        val dateStartStr = dateStart.format(dateFormatter)
        val dateEndStr = dateEnd.format(dateFormatter)
        
        val cached = cachedActivityDistributionDao.getCachedDistribution(userId, dateStartStr, dateEndStr)
        
        return cached.map { entity ->
            ActivityDistribution(
                activityLabel = entity.activityLabel,
                totalSeconds = entity.totalSeconds,
                totalMinutes = entity.totalMinutes,
                totalHours = entity.totalHours,
                percentage = entity.percentage
            )
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
