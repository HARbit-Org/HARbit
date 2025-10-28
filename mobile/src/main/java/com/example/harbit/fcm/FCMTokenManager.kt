package com.example.harbit.fcm

import android.util.Log
import com.example.harbit.data.remote.dto.UpdateFcmTokenRequest
import com.example.harbit.data.remote.service.UserApiService
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FCMTokenManager @Inject constructor(
    private val userApiService: UserApiService
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "FCMTokenManager"
    }

    /**
     * Get current FCM token and send it to the backend
     */
    fun refreshToken() {
        scope.launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d(TAG, "FCM Token retrieved: $token")
                sendTokenToBackend(token)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get FCM token", e)
            }
        }
    }

    /**
     * Send FCM token to backend
     */
    private suspend fun sendTokenToBackend(token: String) {
        try {
            val response = userApiService.updateFcmToken(
                UpdateFcmTokenRequest(fcmToken = token)
            )
            if (response.isSuccessful) {
                Log.d(TAG, "FCM token sent to backend successfully")
            } else {
                Log.e(TAG, "Failed to send FCM token: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending FCM token to backend", e)
        }
    }
}
