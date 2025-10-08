package com.example.harbit.di

import com.example.harbit.data.repository.SensorRepositoryImpl
import com.example.harbit.domain.repository.SensorRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindSensorRepository(
        sensorRepositoryImpl: SensorRepositoryImpl
    ): SensorRepository
}
