package com.example.harbit.data.remote.interceptor

import com.example.harbit.data.local.preferences.AuthPreferencesRepository
import com.example.harbit.data.remote.dto.RefreshTokenRequestDto
import com.example.harbit.data.remote.service.AuthApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val authPreferences: AuthPreferencesRepository
) : Interceptor {
    
    @Volatile
    private var authApiService: AuthApiService? = null
    
    fun setAuthApiService(service: AuthApiService) {
        authApiService = service
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth for auth endpoints
        if (originalRequest.url.encodedPath.startsWith("/auth/")) {
            return chain.proceed(originalRequest)
        }

        // Add access token to request
        val accessToken = runBlocking { authPreferences.getAccessToken() }
        val requestWithAuth = if (accessToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        var response = chain.proceed(requestWithAuth)

        // If we get 401 Unauthorized, try to refresh the token
        if (response.code == 401 && authApiService != null) {
            response.close()
            
            synchronized(this) {
                val refreshToken = runBlocking { authPreferences.getRefreshToken() }
                
                if (refreshToken != null) {
                    try {
                        // Attempt to refresh the token
                        val refreshResponse = runBlocking {
                            authApiService!!.refreshToken(
                                RefreshTokenRequestDto(refreshToken)
                            )
                        }
                        
                        if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                            val authResponse = refreshResponse.body()!!
                            
                            // Save new tokens
                            runBlocking {
                                authPreferences.saveTokens(
                                    accessToken = authResponse.accessToken,
                                    refreshToken = authResponse.refreshToken,
                                    expiresIn = authResponse.expiresIn
                                )
                            }
                            
                            // Retry the original request with new token
                            val newRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer ${authResponse.accessToken}")
                                .build()
                            
                            response = chain.proceed(newRequest)
                        } else {
                            // Refresh failed, clear tokens
                            runBlocking { authPreferences.clearTokens() }
                        }
                    } catch (e: Exception) {
                        // Refresh failed, clear tokens
                        runBlocking { authPreferences.clearTokens() }
                    }
                }
            }
        }

        return response
    }
}
