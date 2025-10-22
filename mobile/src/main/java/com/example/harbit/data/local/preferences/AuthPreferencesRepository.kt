package com.example.harbit.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_preferences")

@Singleton
class AuthPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val TOKEN_EXPIRY = longPreferencesKey("token_expiry")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
        val USER_PICTURE_URL = stringPreferencesKey("user_picture_url")
    }

    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        expiresIn: Int
    ) {
        val expiryTime = System.currentTimeMillis() + (expiresIn * 1000L)
        context.dataStore.edit { preferences ->
            preferences[Keys.ACCESS_TOKEN] = accessToken
            preferences[Keys.REFRESH_TOKEN] = refreshToken
            preferences[Keys.TOKEN_EXPIRY] = expiryTime
        }
    }

    suspend fun saveUserInfo(
        userId: String,
        email: String,
        displayName: String?,
        pictureUrl: String?
    ) {
        context.dataStore.edit { preferences ->
            preferences[Keys.USER_ID] = userId
            preferences[Keys.USER_EMAIL] = email
            displayName?.let { preferences[Keys.USER_DISPLAY_NAME] = it }
            pictureUrl?.let { preferences[Keys.USER_PICTURE_URL] = it }
        }
    }

    suspend fun getAccessToken(): String? {
        return context.dataStore.data.first()[Keys.ACCESS_TOKEN]
    }

    suspend fun getRefreshToken(): String? {
        return context.dataStore.data.first()[Keys.REFRESH_TOKEN]
    }

    suspend fun isTokenExpired(): Boolean {
        val expiryTime = context.dataStore.data.first()[Keys.TOKEN_EXPIRY] ?: 0L
        return System.currentTimeMillis() >= expiryTime
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        !preferences[Keys.ACCESS_TOKEN].isNullOrEmpty()
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[Keys.USER_EMAIL]
    }

    val userDisplayName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[Keys.USER_DISPLAY_NAME]
    }

    val userPictureUrl: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[Keys.USER_PICTURE_URL]
    }

    suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences.remove(Keys.ACCESS_TOKEN)
            preferences.remove(Keys.REFRESH_TOKEN)
            preferences.remove(Keys.TOKEN_EXPIRY)
            preferences.remove(Keys.USER_ID)
            preferences.remove(Keys.USER_EMAIL)
            preferences.remove(Keys.USER_DISPLAY_NAME)
            preferences.remove(Keys.USER_PICTURE_URL)
        }
    }
}
