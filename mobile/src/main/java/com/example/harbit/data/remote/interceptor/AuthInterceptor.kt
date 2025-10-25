package com.example.harbit.data.remote.interceptor

import android.util.Log
import com.example.harbit.data.local.preferences.AuthPreferencesRepository
import com.example.harbit.data.remote.dto.RefreshTokenRequestDto
import com.example.harbit.data.remote.service.AuthApiService
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val authPreferences: AuthPreferencesRepository,
    @javax.inject.Named("AuthApiService") private val authApiServiceProvider: Provider<AuthApiService>
) : Interceptor {

    companion object {
        private const val TAG = "AuthInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // Determine if this is a logout request
        val isLogoutRequest = path.contains("/logout")
        
        // Skip auth header for auth endpoints (except logout which needs the token)
        val shouldSkipAuth = path.startsWith("/auth/") && !isLogoutRequest
        
        if (shouldSkipAuth) {
            Log.d(TAG, "Skipping auth for: $path")
            return chain.proceed(originalRequest)
        }

        // Add access token to request
        val accessToken = runBlocking { authPreferences.getAccessToken() }
        Log.d(
            TAG,
            "Intercepting request to $path, has token: ${accessToken != null}, isLogout: $isLogoutRequest"
        )

        val requestWithAuth = if (accessToken != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        var response = chain.proceed(requestWithAuth)

        // If we get 401 Unauthorized, try to refresh the token (but NOT for logout endpoints)
        if (response.code == 401 && !isLogoutRequest) {
            Log.d(TAG, "Got 401 response for $path, attempting token refresh...")
            
            try {
                val authApiService = authApiServiceProvider.get()
                Log.d(TAG, "Retrieved authApiService from provider")
                response.close()

                synchronized(this) {
                    val refreshToken = runBlocking { authPreferences.getRefreshToken() }
                    Log.d(TAG, "Have refresh token: ${refreshToken != null}")

                    if (refreshToken != null) {
                        try {
                            // Attempt to refresh the token
                            Log.d(TAG, "Calling refresh token endpoint...")
                            val refreshResponse = runBlocking {
                                authApiService.refreshToken(
                                    RefreshTokenRequestDto(refreshToken)
                                )
                            }

                            Log.d(
                                TAG,
                                "Refresh response code: ${refreshResponse.code()}, successful: ${refreshResponse.isSuccessful}"
                            )

                            if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                                val authResponse = refreshResponse.body()!!

                                // Save new tokens
                                Log.d(TAG, "Saving new tokens and retrying request...")
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
                                Log.d(TAG, "Retry response code: ${response.code}")
                            } else {
                                // Refresh failed, clear tokens
                                Log.e(
                                    TAG,
                                    "Refresh failed, clearing tokens. Response: ${
                                        refreshResponse.errorBody()?.string()
                                    }"
                                )
                                runBlocking { authPreferences.clearTokens() }
                            }
                        } catch (e: Exception) {
                            // Refresh failed, clear tokens
                            Log.e(TAG, "Exception during token refresh: ${e.message}", e)
                            runBlocking { authPreferences.clearTokens() }
                        }
                    } else {
                        Log.w(TAG, "No refresh token available, clearing tokens")
                        runBlocking { authPreferences.clearTokens() }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Cannot refresh token: ${e.message}", e)
                response.close()
                runBlocking { authPreferences.clearTokens() }
            }
        } else if (response.code == 401 && isLogoutRequest) {
            Log.d(TAG, "Got 401 for logout request - this is expected, not refreshing token")
        }

        return response
    }
}
