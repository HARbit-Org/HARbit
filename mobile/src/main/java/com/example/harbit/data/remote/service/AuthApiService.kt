package com.example.harbit.data.remote.service

import com.example.harbit.data.remote.dto.AuthResponseDto
import com.example.harbit.data.remote.dto.GoogleAuthRequestDto
import com.example.harbit.data.remote.dto.RefreshTokenRequestDto
import com.example.harbit.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("/auth/google")
    suspend fun authenticateWithGoogle(
        @Body request: GoogleAuthRequestDto
    ): Response<AuthResponseDto>
    
    @POST("/auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequestDto
    ): Response<AuthResponseDto>
    
    @POST("/auth/logout")
    suspend fun logout(): Response<Unit>
    
    @POST("/auth/logout-all")
    suspend fun logoutAll(): Response<Unit>
    
    @GET("/auth/me")
    suspend fun getCurrentUser(): Response<UserDto>
}
