package com.example.harbit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.harbit.data.local.entity.ProcessedActivityEntity

data class ActivityDistribution(
    val activityLabel: String,
    val totalSeconds: Double,
    val totalMinutes: Double,
    val totalHours: Double,
    val percentage: Double
)

@Dao
interface ProcessedActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ProcessedActivityEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<ProcessedActivityEntity>)

    /**
     * Get activity distribution for a given user and date range.
     * 
     * Each row represents ~2.5 seconds of activity (50% overlap).
     * Returns activity label, total seconds/minutes/hours, and percentage.
     */
    @Query("""
        SELECT 
            activity_label as activityLabel,
            COUNT(*) * 2.5 AS totalSeconds,
            ROUND(COUNT(*) * 2.5 / 60.0, 2) AS totalMinutes,
            ROUND(COUNT(*) * 2.5 / 3600.0, 2) AS totalHours,
            ROUND(100.0 * COUNT(*) / (SELECT COUNT(*) FROM processed_activities 
                WHERE user_id = :userId 
                AND ts_start >= :startTime 
                AND ts_end <= :endTime), 2) AS percentage
        FROM processed_activities
        WHERE user_id = :userId
            AND ts_start >= :startTime
            AND ts_end <= :endTime
        GROUP BY activity_label
        ORDER BY percentage DESC
    """)
    suspend fun getActivityDistribution(
        userId: String,
        startTime: Long,
        endTime: Long
    ): List<ActivityDistribution>

    @Query("SELECT COUNT(*) FROM processed_activities WHERE user_id = :userId AND ts_start >= :startTime AND ts_end <= :endTime")
    suspend fun getActivityCount(userId: String, startTime: Long, endTime: Long): Int

    @Query("DELETE FROM processed_activities WHERE user_id = :userId AND ts_start >= :startTime AND ts_end <= :endTime")
    suspend fun deleteActivitiesInRange(userId: String, startTime: Long, endTime: Long)

    @Query("DELETE FROM processed_activities WHERE created_at < :beforeTimestamp")
    suspend fun deleteOldActivities(beforeTimestamp: Long)
}
