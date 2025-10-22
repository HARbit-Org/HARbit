package com.example.harbit.data.remote.service

import com.example.harbit.data.remote.dto.UpdateProfileDto
import com.example.harbit.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface UserApiService {
    
    @GET("/users/me")
    suspend fun getCurrentUser(): Response<UserDto>
    
    @PATCH("/users/me")
    suspend fun updateProfile(
        @Body profileData: UpdateProfileDto
    ): Response<UserDto>
}
