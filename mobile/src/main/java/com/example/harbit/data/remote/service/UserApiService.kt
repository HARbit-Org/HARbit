package com.example.harbit.data.remote.service

import com.example.harbit.data.remote.dto.UpdateFcmTokenRequest
import com.example.harbit.data.remote.dto.UpdateFcmTokenResponse
import com.example.harbit.data.remote.dto.UpdateProfileDto
import com.example.harbit.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UserApiService {
    
    @GET("/users/me")
    suspend fun getCurrentUser(): Response<UserDto>
    
    @PATCH("/users/me")
    suspend fun updateProfile(
        @Body profileData: UpdateProfileDto
    ): Response<UserDto>
    
    @PATCH("/users/fcm-token")
    suspend fun updateFcmToken(
        @Body request: UpdateFcmTokenRequest
    ): Response<UpdateFcmTokenResponse>
}
