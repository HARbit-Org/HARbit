package com.example.harbit.data.repository

import com.example.harbit.data.local.dao.ActivityDistribution
import java.time.LocalDate

interface ActivityRepository {
    
    /**
     * Get activity distribution from local database for a date range.
     * Uses Room to query locally stored processed activities.
     * 
     * For a single day, pass the same date for both parameters.
     */
    suspend fun getLocalActivityDistribution(
        userId: String,
        dateStart: LocalDate,
        dateEnd: LocalDate
    ): List<ActivityDistribution>
    
    /**
     * Fetch activity distribution from backend API for a date range and sync to local database.
     * Returns cached data immediately if available, otherwise fetches from API.
     * 
     * For a single day, pass the same date for both parameters.
     */
    suspend fun fetchAndSyncActivityDistribution(
        dateStart: LocalDate,
        dateEnd: LocalDate
    ): Result<List<ActivityDistribution>>
    
    /**
     * Refresh activity distribution from backend in the background.
     * Use this after initial cached data is displayed to get fresh data.
     * 
     * For a single day, pass the same date for both parameters.
     */
    suspend fun refreshActivityDistributionInBackground(
        dateStart: LocalDate,
        dateEnd: LocalDate
    ): Result<List<ActivityDistribution>>
    
    /**
     * Get total hours of activity for a date range from local database.
     * 
     * For a single day, pass the same date for both parameters.
     */
    suspend fun getTotalActivityHours(
        userId: String,
        dateStart: LocalDate,
        dateEnd: LocalDate
    ): Double
}
