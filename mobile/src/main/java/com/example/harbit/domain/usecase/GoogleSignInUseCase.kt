package com.example.harbit.domain.usecase

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val credentialManager = CredentialManager.create(context)
    
    /**
     * Initiates Google Sign-In button flow and returns the ID token
     * Uses GetSignInWithGoogleOption for the official Sign in with Google button
     * @param serverClientId Your web client ID from Google Cloud Console
     * @return Result with ID token on success
     */
    suspend fun signIn(serverClientId: String): Result<String> {
        return try {
            // Use GetSignInWithGoogleOption for Sign in with Google button
            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()
            
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()
            
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            
            val credential = result.credential
            
            // Handle CustomCredential for GoogleIdToken
            when (credential) {
                is CustomCredential -> {
                    if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        Result.success(googleIdTokenCredential.idToken)
                    } else {
                        Result.failure(Exception("Unexpected credential type: ${credential.type}"))
                    }
                }
                else -> {
                    Result.failure(Exception("Unexpected credential type"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
