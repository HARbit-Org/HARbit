package com.example.harbit.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.harbit.MainActivity
import com.example.harbit.R
import com.example.harbit.data.remote.dto.UpdateFcmTokenRequest
import com.example.harbit.data.remote.service.UserApiService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HARbitFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var userApiService: UserApiService

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "harbit_notifications"
        private const val NOTIFICATION_ID = 1
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // Send token to backend
        serviceScope.launch {
            try {
                updateFcmTokenOnBackend(token)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update FCM token on backend", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "Message received from: ${message.from}")
        
        // Handle notification
        message.notification?.let { notification ->
            showNotification(
                title = notification.title ?: "HARbit",
                body = notification.body ?: ""
            )
        }

        // Handle data payload (if any)
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${message.data}")
            // Handle custom data here if needed
        }
    }

    private suspend fun updateFcmTokenOnBackend(token: String) {
        try {
            val response = userApiService.updateFcmToken(
                UpdateFcmTokenRequest(fcmToken = token)
            )
            if (response.isSuccessful) {
                Log.d(TAG, "FCM token updated successfully on backend")
            } else {
                Log.e(TAG, "Failed to update FCM token: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating FCM token", e)
        }
    }

    private fun showNotification(title: String, body: String) {
        createNotificationChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // We'll update this later
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "HARbit Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de actividad y recordatorios"
            }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
