package com.example.harbit.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harbit.data.local.preferences.AuthPreferencesRepository
import com.example.harbit.data.remote.dto.UserDto
import com.example.harbit.domain.repository.AuthRepository
import com.example.harbit.domain.usecase.GoogleSignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    data object Initial : AuthState()
    data object Loading : AuthState()
    data class Success(val user: UserDto) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val authPreferences: AuthPreferencesRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    val isLoggedIn: StateFlow<Boolean> = authPreferences.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val userEmail: StateFlow<String?> = authPreferences.userEmail
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val userDisplayName: StateFlow<String?> = authPreferences.userDisplayName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    val userPictureUrl: StateFlow<String?> = authPreferences.userPictureUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Initiates Google Sign-In flow
     * @param serverClientId Your web client ID from Google Cloud Console
     */
    fun signInWithGoogle(serverClientId: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            // Step 1: Get Google ID token
            val idTokenResult = googleSignInUseCase.signIn(serverClientId)
            
            if (idTokenResult.isFailure) {
                _authState.value = AuthState.Error(
                    idTokenResult.exceptionOrNull()?.message ?: "Google Sign-In failed"
                )
                return@launch
            }
            
            val idToken = idTokenResult.getOrNull()!!
            
            // Step 2: Authenticate with backend
            val authResult = authRepository.authenticateWithGoogle(idToken)
            
            if (authResult.isSuccess) {
                val authResponse = authResult.getOrNull()!!
                _authState.value = AuthState.Success(authResponse.user)
            } else {
                _authState.value = AuthState.Error(
                    authResult.exceptionOrNull()?.message ?: "Authentication failed"
                )
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            val result = authRepository.logout()
            if (result.isSuccess) {
                _authState.value = AuthState.Initial
            }
        }
    }
    
    fun logoutAll() {
        viewModelScope.launch {
            val result = authRepository.logoutAll()
            if (result.isSuccess) {
                _authState.value = AuthState.Initial
            }
        }
    }
    
    fun getCurrentUser() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            val result = authRepository.getCurrentUser()
            if (result.isSuccess) {
                _authState.value = AuthState.Success(result.getOrNull()!!)
            } else {
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to get user"
                )
            }
        }
    }
    
    fun resetAuthState() {
        _authState.value = AuthState.Initial
    }
}
