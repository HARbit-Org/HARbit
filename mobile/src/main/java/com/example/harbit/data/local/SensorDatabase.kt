package com.example.harbit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.harbit.data.repository.SensorBatchDao

@Database(
    entities = [SensorBatchEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SensorDatabase : RoomDatabase() {
    abstract fun sensorBatchDao(): SensorBatchDao
}
