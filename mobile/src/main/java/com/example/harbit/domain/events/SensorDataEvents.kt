package com.example.harbit.domain.events

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Event bus for sensor data upload events.
 * Notifies when sensor data has been successfully uploaded and processed.
 */
@Singleton
class SensorDataEvents @Inject constructor() {
    
    private val _dataUploadedEvent = MutableSharedFlow<Unit>(replay = 0)
    val dataUploadedEvent: SharedFlow<Unit> = _dataUploadedEvent.asSharedFlow()
    
    /**
     * Emit event when sensor data has been successfully uploaded.
     * This triggers activity distribution refresh in listening screens.
     */
    suspend fun notifyDataUploaded() {
        _dataUploadedEvent.emit(Unit)
    }
}
