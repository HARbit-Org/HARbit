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

    // Form state that persists across navigation
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _birthDate = MutableStateFlow("")
    val birthDate: StateFlow<String> = _birthDate.asStateFlow()

    private val _selectedGender = MutableStateFlow("Masculino")
    val selectedGender: StateFlow<String> = _selectedGender.asStateFlow()

    private val _height = MutableStateFlow("")
    val height: StateFlow<String> = _height.asStateFlow()

    private val _weight = MutableStateFlow("")
    val weight: StateFlow<String> = _weight.asStateFlow()

    private val _privacyAccepted = MutableStateFlow(false)
    val privacyAccepted: StateFlow<Boolean> = _privacyAccepted.asStateFlow()

    // Update functions for form fields
    fun updateUserName(value: String) { _userName.value = value }
    fun updateEmail(value: String) { _email.value = value }
    fun updatePhone(value: String) { _phone.value = value }
    fun updateBirthDate(value: String) { _birthDate.value = value }
    fun updateSelectedGender(value: String) { _selectedGender.value = value }
    fun updateHeight(value: String) { _height.value = value }
    fun updateWeight(value: String) { _weight.value = value }
    fun updatePrivacyAccepted(value: Boolean) { _privacyAccepted.value = value }

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
