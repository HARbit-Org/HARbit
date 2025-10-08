package com.example.harbit

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.harbit.data.local.SensorDatabase

class App : Application() {
    
    companion object {
        private var _database: SensorDatabase? = null
        
        fun getDatabase(context: Context): SensorDatabase {
            return _database ?: synchronized(this) {
                _database ?: Room.databaseBuilder(
                    context.applicationContext,
                    SensorDatabase::class.java,
                    "sensor_database"
                )
                .fallbackToDestructiveMigration()
                .build().also { _database = it }
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database eagerly
        _database = getDatabase(this)
    }
}