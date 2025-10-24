package com.example.harbit.ui.screens.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harbit.data.local.dao.ActivityDistribution
import com.example.harbit.data.repository.ActivityRepository
import com.example.harbit.domain.events.SensorDataEvents
import com.example.harbit.service.WatchConnectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class ActivityDistributionState {
    data object Loading : ActivityDistributionState()
    data class Success(
        val activities: List<ActivityDistribution>,
        val totalHours: Double
    ) : ActivityDistributionState()
    data class Error(val message: String) : ActivityDistributionState()
    data object Empty : ActivityDistributionState()
}

@HiltViewModel
class ActivityDistributionViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val sensorDataEvents: SensorDataEvents,
    private val watchConnectionManager: WatchConnectionManager
) : ViewModel() {

    private val _state = MutableStateFlow<ActivityDistributionState>(ActivityDistributionState.Loading)
    val state: StateFlow<ActivityDistributionState> = _state.asStateFlow()

    private val _selectedDateRange = MutableStateFlow<Pair<LocalDate, LocalDate>>(
        Pair(LocalDate.now(), LocalDate.now())
    )
    val selectedDateRange: StateFlow<Pair<LocalDate, LocalDate>> = _selectedDateRange.asStateFlow()

    // Use the WatchConnectionManager's state instead of our own
    val isWatchConnected: StateFlow<Boolean> = watchConnectionManager.isWatchConnected

    private var dataUploadListenerJob: Job? = null

    /**
     * Start listening for sensor data upload events.
     * When data is uploaded, automatically refresh the distribution.
     */
    fun startListeningForDataUploads() {
        Log.d("ActivityDistViewModel", "Starting to listen for data upload events")
        dataUploadListenerJob?.cancel()
        dataUploadListenerJob = viewModelScope.launch {
            sensorDataEvents.dataUploadedEvent.collect {
                Log.d("ActivityDistViewModel", "Data upload event received, refreshing...")
                refreshInBackground()
            }
        }
        
        // Start monitoring watch connection (this sends immediate ping)
        watchConnectionManager.startMonitoring()
        Log.d("ActivityDistViewModel", "Watch connection monitoring started")
    }

    /**
     * Stop listening for data upload events.
     */
    fun stopListeningForDataUploads() {
        Log.d("ActivityDistViewModel", "Stopped listening for data upload events")
        dataUploadListenerJob?.cancel()
        dataUploadListenerJob = null
        
        // Stop monitoring watch connection
        watchConnectionManager.stopMonitoring()
    }
    
    /**
     * Check if watch is connected by sending an immediate ping.
     * Note: This is now handled automatically by startListeningForDataUploads()
     */
    fun checkWatchConnection() {
        Log.d("ActivityDistViewModel", "Manual connection check triggered")
        watchConnectionManager.checkConnection()
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningForDataUploads()
    }

    /**
     * Load activity distribution for a date range.
     * Loads cached data immediately, then refreshes from backend in background.
     * For a single day, pass the same date for both parameters.
     */
    fun loadActivityDistribution(dateStart: LocalDate = LocalDate.now(), dateEnd: LocalDate = LocalDate.now()) {
        _selectedDateRange.value = Pair(dateStart, dateEnd)
        _state.value = ActivityDistributionState.Loading

        viewModelScope.launch {
            try {
                // STEP 1: Load from cache first (instant display)
                val cachedResult = activityRepository.fetchAndSyncActivityDistribution(dateStart, dateEnd)
                
                handleDistributionResult(cachedResult)
                
                // STEP 2: If we got cached data, refresh in background
                if (cachedResult.isSuccess) {
                    refreshInBackground()
                }
            } catch (e: Exception) {
                _state.value = ActivityDistributionState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    /**
     * Refresh data from backend in background without showing loading state.
     */
    private fun refreshInBackground() {
        viewModelScope.launch {
            try {
                val (dateStart, dateEnd) = _selectedDateRange.value
                val refreshResult = activityRepository.refreshActivityDistributionInBackground(dateStart, dateEnd)
                
                // Only update UI if refresh succeeded
                if (refreshResult.isSuccess) {
                    Log.d("ActivityDistViewModel", "Background refresh successful")
                    handleDistributionResult(refreshResult)
                } else {
                    Log.d("ActivityDistViewModel", "Background refresh failed, keeping cached data")
                }
            } catch (e: Exception) {
                Log.d("ActivityDistViewModel", "Background refresh error: ${e.message}")
                // Silently fail - keep showing current data
            }
        }
    }

    private fun handleDistributionResult(result: Result<List<ActivityDistribution>>) {
        result.fold(
            onSuccess = { activities ->
                if (activities.isEmpty()) {
                    _state.value = ActivityDistributionState.Empty
                } else {
                    val totalHours = activities.sumOf { it.totalHours }
                    _state.value = ActivityDistributionState.Success(
                        activities = activities,
                        totalHours = totalHours
                    )
                }
            },
            onFailure = { error ->
                _state.value = ActivityDistributionState.Error(
                    error.message ?: "Failed to load activity distribution"
                )
            }
        )
    }

    fun refreshData() {
        val (dateStart, dateEnd) = _selectedDateRange.value
        loadActivityDistribution(dateStart, dateEnd)
    }
}
