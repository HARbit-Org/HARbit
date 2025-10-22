package com.example.harbit.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harbit.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileUpdateState {
    data object Idle : ProfileUpdateState()
    data object Loading : ProfileUpdateState()
    data object Success : ProfileUpdateState()
    data class Error(val message: String) : ProfileUpdateState()
}

@HiltViewModel
class ProfileCompletionViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _updateState = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Idle)
    val updateState: StateFlow<ProfileUpdateState> = _updateState.asStateFlow()

    fun updateProfile(
        displayName: String,
        preferredEmail: String,
        phone: String,
        sex: String,
        birthYear: Int,
        height: Float,
        weight: Float,
        dailyStepGoal: Int = 10000,
        timezone: String = "America/Lima"
    ) {
        viewModelScope.launch {
            _updateState.value = ProfileUpdateState.Loading
            
            try {
                val result = userRepository.updateProfile(
                    displayName = displayName,
                    preferredEmail = preferredEmail,
                    phone = phone,
                    sex = sex,
                    birthYear = birthYear,
                    dailyStepGoal = dailyStepGoal,
                    timezone = timezone,
                    height = height,
                    weight = weight
                )
                
                result.fold(
                    onSuccess = {
                        _updateState.value = ProfileUpdateState.Success
                    },
                    onFailure = { error ->
                        _updateState.value = ProfileUpdateState.Error(
                            error.message ?: "Failed to update profile"
                        )
                    }
                )
            } catch (e: Exception) {
                _updateState.value = ProfileUpdateState.Error(
                    e.message ?: "An error occurred"
                )
            }
        }
    }
}
