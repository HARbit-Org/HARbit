package com.example.harbit.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harbit.data.local.dao.ActivityDistribution
import com.example.harbit.data.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ActivityDistributionState>(ActivityDistributionState.Loading)
    val state: StateFlow<ActivityDistributionState> = _state.asStateFlow()

    private val _selectedDateRange = MutableStateFlow<Pair<LocalDate, LocalDate>>(
        Pair(LocalDate.now(), LocalDate.now())
    )
    val selectedDateRange: StateFlow<Pair<LocalDate, LocalDate>> = _selectedDateRange.asStateFlow()

    // init {
    //     loadActivityDistribution()
    // }

    /**
     * Load activity distribution for a date range.
     * For a single day, pass the same date for both parameters.
     */
    fun loadActivityDistribution(dateStart: LocalDate = LocalDate.now(), dateEnd: LocalDate = LocalDate.now()) {
        _selectedDateRange.value = Pair(dateStart, dateEnd)
        _state.value = ActivityDistributionState.Loading

        viewModelScope.launch {
            try {
                // Try to fetch from backend and sync
                val result = activityRepository.fetchAndSyncActivityDistribution(dateStart, dateEnd)
                
                handleDistributionResult(result)
            } catch (e: Exception) {
                _state.value = ActivityDistributionState.Error(
                    e.message ?: "An unexpected error occurred"
                )
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
