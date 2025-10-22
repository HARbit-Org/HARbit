package com.example.harbit.ui.screens.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.harbit.data.local.preferences.AuthPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    data object Welcome : SplashDestination()
    data object ProfileCompletion : SplashDestination()
    data object Main : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authPreferences: AuthPreferencesRepository
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            // Check if user is logged in
            val isLoggedIn = authPreferences.isLoggedIn.first()
            Log.d("SplashViewModel", "isLoggedIn: $isLoggedIn")
            
            _destination.value = if (isLoggedIn) {
                // User has tokens - check if profile is complete
                val isProfileComplete = authPreferences.isProfileComplete.first()
                Log.d("SplashViewModel", "isProfileComplete: $isProfileComplete")
                
                if (isProfileComplete) {
                    Log.d("SplashViewModel", "Navigating to Main")
                    SplashDestination.Main  // ✅ Go to main app
                } else {
                    Log.d("SplashViewModel", "Navigating to ProfileCompletion")
                    SplashDestination.ProfileCompletion  // ✅ Complete profile first
                }
            } else {
                Log.d("SplashViewModel", "Navigating to Welcome")
                SplashDestination.Welcome  // ✅ Show welcome/login screen
            }
        }
    }
}
