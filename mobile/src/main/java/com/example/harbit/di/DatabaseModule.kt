package com.example.harbit.di

import android.content.Context
import androidx.room.Room
import com.example.harbit.data.local.SensorDatabase
import com.example.harbit.data.local.dao.SensorBatchDao
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
}
