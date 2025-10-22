package com.example.harbit.domain.repository

import com.example.harbit.data.remote.dto.AuthResponseDto
import com.example.harbit.data.remote.dto.UserDto

interface AuthRepository {
    suspend fun authenticateWithGoogle(idToken: String): Result<AuthResponseDto>
    suspend fun refreshToken(): Result<AuthResponseDto>
    suspend fun logout(): Result<Unit>
    suspend fun logoutAll(): Result<Unit>
    suspend fun getCurrentUser(): Result<UserDto>
    suspend fun isLoggedIn(): Boolean
}
