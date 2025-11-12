package com.example.harbit.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harbit.data.remote.dto.UserDto
import com.example.harbit.domain.repository.AuthRepository
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
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _logoutState = MutableStateFlow<LogoutState>(LogoutState.Idle)
    val logoutState: StateFlow<LogoutState> = _logoutState.asStateFlow()

    private val _updateState = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Idle)
    val updateState: StateFlow<ProfileUpdateState> = _updateState.asStateFlow()

    // Form state
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _height = MutableStateFlow("")
    val height: StateFlow<String> = _height.asStateFlow()

    private val _weight = MutableStateFlow("")
    val weight: StateFlow<String> = _weight.asStateFlow()

    private val _pictureUrl = MutableStateFlow<String?>(null)
    val pictureUrl: StateFlow<String?> = _pictureUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Store original user data to detect changes
    private var originalUserData: UserDto? = null

    fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = userRepository.getCurrentUser()
            result.onSuccess { user ->
                // Store original data
                originalUserData = user
                
                _userName.value = user.displayName ?: ""
                _email.value = user.preferredEmail ?: user.email
                _phone.value = user.phone ?: ""
                _height.value = user.height?.toString() ?: ""
                _weight.value = user.weight?.toString() ?: ""
                _pictureUrl.value = user.pictureUrl
            }.onFailure { error ->
                // Log error or handle it
                println("Error loading user data: ${error.message}")
            }
            _isLoading.value = false
        }
    }

    // Update functions for form fields
    fun updateUserName(value: String) { _userName.value = value }
    fun updateEmail(value: String) { _email.value = value }
    fun updatePhone(value: String) { _phone.value = value }
    fun updateHeight(value: String) { _height.value = value }
    fun updateWeight(value: String) { _weight.value = value }

    fun updateProfile() {
        viewModelScope.launch {
            _updateState.value = ProfileUpdateState.Loading
            
            try {
                val heightFloat = _height.value.toFloatOrNull()
                val weightFloat = _weight.value.toFloatOrNull()
                
                val result = userRepository.updateProfile(
                    displayName = _userName.value.ifBlank { null },
                    preferredEmail = _email.value.ifBlank { null },
                    phone = _phone.value.ifBlank { null },
                    height = heightFloat,
                    weight = weightFloat
                )
                
                result.fold(
                    onSuccess = { updatedUser ->
                        // Update original data after successful save
                        originalUserData = updatedUser
                        
                        // Update local state with the response
                        _userName.value = updatedUser.displayName ?: ""
                        _email.value = updatedUser.preferredEmail ?: updatedUser.email
                        _phone.value = updatedUser.phone ?: ""
                        _height.value = updatedUser.height?.toString() ?: ""
                        _weight.value = updatedUser.weight?.toString() ?: ""
                        _pictureUrl.value = updatedUser.pictureUrl
                        
                        _updateState.value = ProfileUpdateState.Success
                    },
                    onFailure = { error ->
                        _updateState.value = ProfileUpdateState.Error(
                            error.message ?: "Error al actualizar perfil"
                        )
                    }
                )
            } catch (e: Exception) {
                _updateState.value = ProfileUpdateState.Error(
                    e.message ?: "Ocurrió un error"
                )
            }
        }
    }

    fun hasUserDataChanged(): Boolean {
        val original = originalUserData ?: return true // If no original data, consider it changed
        
        // Compare current form values with original data
        val displayNameChanged = _userName.value != (original.displayName ?: "")
        val emailChanged = _email.value != (original.preferredEmail ?: original.email)
        val phoneChanged = _phone.value != (original.phone ?: "")
        val heightChanged = _height.value != (original.height?.toString() ?: "")
        val weightChanged = _weight.value != (original.weight?.toString() ?: "")
        
        return displayNameChanged || emailChanged || phoneChanged || heightChanged || weightChanged
    }

    fun resetUpdateState() {
        _updateState.value = ProfileUpdateState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            _logoutState.value = LogoutState.Loading
            val result = authRepository.logout()
            
            _logoutState.value = if (result.isSuccess) {
                LogoutState.Success
            } else {
                LogoutState.Error(result.exceptionOrNull()?.message ?: "Error al cerrar sesión")
            }
        }
    }

    fun resetLogoutState() {
        _logoutState.value = LogoutState.Idle
    }
}

sealed class LogoutState {
    object Idle : LogoutState()
    object Loading : LogoutState()
    object Success : LogoutState()
    data class Error(val message: String) : LogoutState()
}
