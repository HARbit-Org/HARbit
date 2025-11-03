package com.example.harbit.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harbit.data.remote.dto.ProgressInsightDto
import com.example.harbit.data.repository.ProgressRepository
import com.example.harbit.ui.screens.dashboard.ActivityDistributionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProgressInsightsState {
    data object Loading : ProgressInsightsState()

    data class Success(
        val insights: List<ProgressInsightDto>
    ) : ProgressInsightsState()

    data class Error(val message: String) : ProgressInsightsState()

    data object Empty : ProgressInsightsState()
}

@HiltViewModel
class ProgressInsightsViewModel @Inject constructor(
    private val progressRepository: ProgressRepository
) : ViewModel() {
    private val _state = MutableStateFlow<ProgressInsightsState>(ProgressInsightsState.Loading)
    val state: StateFlow<ProgressInsightsState> = _state.asStateFlow()

    fun loadProgressInsights() {
        viewModelScope.launch {
            try {
                val result = progressRepository.getAllProgressInsights()

                handleProgressInsightsResult(result)
            } catch (e: Exception) {
                _state.value = ProgressInsightsState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    private fun handleProgressInsightsResult(result: Result<List<ProgressInsightDto>>) {
        result.fold(
            onSuccess = { insights ->
                if (insights.isEmpty()) {
                    _state.value = ProgressInsightsState.Empty
                } else {
                    _state.value = ProgressInsightsState.Success(
                        insights = insights
                    )
                }
            },
            onFailure = { error ->
                _state.value = ProgressInsightsState.Error(
                    error.message ?: "Failed to load activity distribution"
                )
            }
        )
    }
}