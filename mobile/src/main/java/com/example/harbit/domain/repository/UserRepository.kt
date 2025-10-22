package com.example.harbit.domain.repository

import com.example.harbit.data.remote.dto.UserDto

interface UserRepository {
    suspend fun getCurrentUser(): Result<UserDto>
    suspend fun updateProfile(
        displayName: String? = null,
        preferredEmail: String? = null,
        phone: String? = null,
        sex: String? = null,
        birthYear: Int? = null,
        dailyStepGoal: Int? = null,
        timezone: String? = null,
        height: Float? = null,
        weight: Float? = null
    ): Result<UserDto>
}
