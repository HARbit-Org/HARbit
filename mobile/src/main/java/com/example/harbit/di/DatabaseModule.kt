package com.example.harbit.di

import android.content.Context
import androidx.room.Room
import com.example.harbit.data.local.SensorDatabase
import com.example.harbit.data.local.dao.CachedActivityDistributionDao
import com.example.harbit.data.local.dao.ProcessedActivityDao
import com.example.harbit.data.local.dao.SensorBatchDao
import com.example.harbit.data.local.dao.SensorReadingDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSensorDatabase(
        @ApplicationContext context: Context
    ): SensorDatabase {
        return Room.databaseBuilder(
            context,
            SensorDatabase::class.java,
            "sensor_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideSensorBatchDao(database: SensorDatabase): SensorBatchDao {
        return database.sensorBatchDao()
    }

    @Provides
    @Singleton
    fun provideSensorReadingDao(database: SensorDatabase): SensorReadingDao {
        return database.sensorReadingDao()
    }

    @Provides
    @Singleton
    fun provideProcessedActivityDao(database: SensorDatabase): ProcessedActivityDao {
        return database.processedActivityDao()
    }

    @Provides
    @Singleton
    fun provideCachedActivityDistributionDao(database: SensorDatabase): CachedActivityDistributionDao {
        return database.cachedActivityDistributionDao()
    }
}