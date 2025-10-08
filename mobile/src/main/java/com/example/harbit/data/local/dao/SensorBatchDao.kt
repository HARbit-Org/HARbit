package com.example.harbit.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.harbit.data.local.entity.SensorBatchEntity

@Dao
interface SensorBatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: SensorBatchEntity)

    @Query("SELECT * FROM sensor_batches WHERE uploaded = 0 ORDER BY timestamp ASC")
    suspend fun getUnsentBatches(): List<SensorBatchEntity>

    @Query("SELECT COUNT(*) FROM sensor_batches WHERE uploaded = 0")
    suspend fun getUnsentCount(): Int

    @Query("UPDATE sensor_batches SET uploaded = 1 WHERE id IN (:batchIds)")
    suspend fun markAsUploaded(batchIds: List<String>)

    @Query("DELETE FROM sensor_batches WHERE uploaded = 1 AND createdAt < :beforeTimestamp")
    suspend fun deleteOldUploaded(beforeTimestamp: Long)
}