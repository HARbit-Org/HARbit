package com.example.harbit.data.repository

import com.example.harbit.data.local.preferences.AuthPreferencesRepository
import com.example.harbit.data.remote.dto.AuthResponseDto
import com.example.harbit.data.remote.dto.GoogleAuthRequestDto
import com.example.harbit.data.remote.dto.RefreshTokenRequestDto
import com.example.harbit.data.remote.dto.UserDto
import com.example.harbit.data.remote.service.AuthApiService
import com.example.harbit.domain.repository.AuthRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val authPreferences: AuthPreferencesRepository
) : AuthRepository {

    override suspend fun authenticateWithGoogle(idToken: String): Result<AuthResponseDto> {
        return try {
            val request = GoogleAuthRequestDto(idToken = idToken)
            println("DEBUG: Sending auth request with idToken: ${idToken.take(20)}...")
            println("DEBUG: Request object: $request")
            
            val response = authApiService.authenticateWithGoogle(request)
            
            println("DEBUG: Response code: ${response.code()}")
            if (!response.isSuccessful) {
                println("DEBUG: Response error body: ${response.errorBody()?.string()}")
            }
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                
                // Save tokens and user info
                authPreferences.saveTokens(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    expiresIn = authResponse.expiresIn
                )
                
                authPreferences.saveUserInfo(
                    userId = authResponse.user.id.toString(),
                    email = authResponse.user.email,
                    displayName = authResponse.user.displayName,
                    pictureUrl = authResponse.user.pictureUrl,
                    isProfileComplete = authResponse.isProfileComplete
                )
                
                Result.success(authResponse)
            } else {
                Result.failure(Exception("Authentication failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(): Result<AuthResponseDto> {
        return try {
            val refreshToken = authPreferences.getRefreshToken()
                ?: return Result.failure(Exception("No refresh token available"))
            
            val response = authApiService.refreshToken(
                RefreshTokenRequestDto(refreshToken = refreshToken)
            )
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                
                // Save new tokens and update profile status
                authPreferences.saveTokens(
                    accessToken = authResponse.accessToken,
                    refreshToken = authResponse.refreshToken,
                    expiresIn = authResponse.expiresIn
                )
                
                authPreferences.updateProfileCompleteStatus(authResponse.isProfileComplete)
                
                Result.success(authResponse)
            } else {
                // If refresh fails, clear all tokens
                authPreferences.clearTokens()
                Result.failure(Exception("Token refresh failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            authPreferences.clearTokens()
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val response = authApiService.logout()
            
            // For logout, both 200 and 401 are acceptable (401 means already logged out)
            if (response.isSuccessful || response.code() == 401) {
                authPreferences.clearTokens()
                Result.success(Unit)
            } else {
                // Clear tokens anyway on any response
                authPreferences.clearTokens()
                Result.failure(Exception("Logout failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Still clear local tokens even if API call fails
            authPreferences.clearTokens()
            Result.failure(e)
        }
    }

    override suspend fun logoutAll(): Result<Unit> {
        return try {
            val response = authApiService.logoutAll()
            
            if (response.isSuccessful) {
                authPreferences.clearTokens()
                Result.success(Unit)
            } else {
                // Clear tokens anyway on server rejection
                authPreferences.clearTokens()
                Result.failure(Exception("Logout all failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            // Still clear local tokens even if API call fails
            authPreferences.clearTokens()
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): Result<UserDto> {
        return try {
            val response = authApiService.getCurrentUser()
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return authPreferences.isLoggedIn.first()
    }
}
