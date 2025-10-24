package com.example.harbit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.harbit.data.local.entity.CachedActivityDistributionEntity

@Dao
interface CachedActivityDistributionDao {
    
    /**
     * Save cached activity distribution for a date range.
     * Replaces any existing cache for the same user/date range.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedDistributions(distributions: List<CachedActivityDistributionEntity>)
    
    /**
     * Get cached activity distribution for a specific user and date range.
     */
    @Query("""
        SELECT * FROM cached_activity_distribution
        WHERE user_id = :userId
            AND date_start = :dateStart
            AND date_end = :dateEnd
        ORDER BY percentage DESC
    """)
    suspend fun getCachedDistribution(
        userId: String,
        dateStart: String,
        dateEnd: String
    ): List<CachedActivityDistributionEntity>
    
    /**
     * Delete cached data for a specific user and date range.
     */
    @Query("""
        DELETE FROM cached_activity_distribution
        WHERE user_id = :userId
            AND date_start = :dateStart
            AND date_end = :dateEnd
    """)
    suspend fun deleteCachedDistribution(userId: String, dateStart: String, dateEnd: String)
    
    /**
     * Delete old cached data (older than specified timestamp).
     */
    @Query("DELETE FROM cached_activity_distribution WHERE cached_at < :beforeTimestamp")
    suspend fun deleteOldCache(beforeTimestamp: Long)
    
    /**
     * Delete all cached data for a user.
     */
    @Query("DELETE FROM cached_activity_distribution WHERE user_id = :userId")
    suspend fun deleteUserCache(userId: String)
}
