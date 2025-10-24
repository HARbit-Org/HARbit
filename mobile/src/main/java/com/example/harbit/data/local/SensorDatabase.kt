package com.example.harbit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.harbit.data.local.dao.CachedActivityDistributionDao
import com.example.harbit.data.local.dao.ProcessedActivityDao
import com.example.harbit.data.local.dao.SensorBatchDao
import com.example.harbit.data.local.dao.SensorReadingDao
import com.example.harbit.data.local.entity.CachedActivityDistributionEntity
import com.example.harbit.data.local.entity.ProcessedActivityEntity
import com.example.harbit.data.local.entity.SensorBatchEntity
import com.example.harbit.data.local.entity.SensorReadingEntity

@Database(
    entities = [
        SensorBatchEntity::class,
        SensorReadingEntity::class,
        ProcessedActivityEntity::class,
        CachedActivityDistributionEntity::class
    ],
    version = 6,  // Incremented version for cached distribution entity
    exportSchema = false
)
abstract class SensorDatabase : RoomDatabase() {
    abstract fun sensorBatchDao(): SensorBatchDao
    abstract fun sensorReadingDao(): SensorReadingDao
    abstract fun processedActivityDao(): ProcessedActivityDao
    abstract fun cachedActivityDistributionDao(): CachedActivityDistributionDao
}
