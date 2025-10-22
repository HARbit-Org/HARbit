package com.example.harbit.data.repository

import com.example.harbit.data.local.preferences.AuthPreferencesRepository
import com.example.harbit.data.remote.dto.UpdateProfileDto
import com.example.harbit.data.remote.dto.UserDto
import com.example.harbit.data.remote.service.UserApiService
import com.example.harbit.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
    private val authPreferences: AuthPreferencesRepository
) : UserRepository {
    
    override suspend fun getCurrentUser(): Result<UserDto> {
        return try {
            val response = userApiService.getCurrentUser()
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get user: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateProfile(
        displayName: String?,
        preferredEmail: String?,
        phone: String?,
        sex: String?,
        birthYear: Int?,
        dailyStepGoal: Int?,
        timezone: String?,
        height: Float?,
        weight: Float?
    ): Result<UserDto> {
        return try {
            val updateDto = UpdateProfileDto(
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
            
            val response = userApiService.updateProfile(updateDto)
            
            if (response.isSuccessful && response.body() != null) {
                val updatedUser = response.body()!!
                
                // Update profile complete status if sex and birth_year are now filled
                if (updatedUser.sex != null && updatedUser.birthYear != null) {
                    authPreferences.updateProfileCompleteStatus(true)
                }
                
                Result.success(updatedUser)
            } else {
                Result.failure(Exception("Failed to update profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
