# Mobile Authentication Implementation

This document describes the complete authentication system for the HARbit mobile app, including Google OAuth integration and automatic token refresh.

## Architecture Overview

The authentication system follows Clean Architecture principles with:
- **Data Layer**: DTOs, API services, repositories, and local storage
- **Domain Layer**: Repository interfaces and use cases
- **Presentation Layer**: ViewModels and UI components

## Components

### 1. Data Transfer Objects (DTOs)

Located in `data/remote/dto/`:

- **GoogleAuthRequestDto**: Request to backend with Google ID token
- **RefreshTokenRequestDto**: Request to refresh access token
- **UserDto**: User information from backend
- **AuthResponseDto**: Authentication response with tokens and user data

### 2. API Service

**AuthApiService** (`data/remote/service/AuthApiService.kt`):
- `POST /auth/google` - Authenticate with Google ID token
- `POST /auth/refresh` - Refresh access token
- `POST /auth/logout` - Logout from current session
- `POST /auth/logout-all` - Logout from all sessions
- `GET /auth/me` - Get current user information

### 3. Local Storage

**AuthPreferencesRepository** (`data/local/preferences/AuthPreferencesRepository.kt`):
- Stores tokens securely using DataStore
- Tracks token expiry time
- Provides flows for reactive UI updates
- Stores user information

### 4. Authentication Interceptor

**AuthInterceptor** (`data/remote/interceptor/AuthInterceptor.kt`):
- Automatically adds access token to requests
- Detects 401 Unauthorized responses
- Automatically refreshes expired tokens
- Retries failed requests with new token
- **KEY FEATURE**: Silent token refresh - users stay logged in

### 5. Repository

**AuthRepository** interface and **AuthRepositoryImpl**:
- Handles all authentication operations
- Manages token storage
- Coordinates between API and local storage

### 6. Use Cases

**GoogleSignInUseCase** (`domain/usecase/GoogleSignInUseCase.kt`):
- Uses Android Credential Manager API
- Initiates Google Sign-In flow
- Returns Google ID token for backend authentication

### 7. ViewModel

**AuthViewModel** (`presentation/auth/AuthViewModel.kt`):
- Manages authentication state
- Provides flows for reactive UI
- Orchestrates sign-in/sign-out flows

## Token Lifecycle

### Access Token
- **Lifetime**: 1 hour
- **Usage**: Attached to every API request via AuthInterceptor
- **Storage**: DataStore (encrypted by Android)

### Refresh Token
- **Lifetime**: 30 days
- **Usage**: Automatically used to get new access token when expired
- **Storage**: DataStore (encrypted by Android)

### Auto-Refresh Flow

```
1. User makes API request
   ↓
2. AuthInterceptor adds access token
   ↓
3. Backend returns 401 (token expired)
   ↓
4. AuthInterceptor catches 401
   ↓
5. Uses refresh token to get new access token
   ↓
6. Saves new tokens to DataStore
   ↓
7. Retries original request with new token
   ↓
8. Request succeeds - user never notices!
```

## Setup Instructions

### 1. Google Cloud Console Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create/select your project
3. Enable **Google+ API**
4. Go to **Credentials** → **Create Credentials** → **OAuth 2.0 Client ID**
5. Create **two** client IDs:
   - **Web application** - for backend verification
   - **Android** - for mobile app
     - Package name: `com.example.harbit`
     - SHA-1 certificate fingerprint (debug): Get with:
       ```bash
       keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
       ```

### 2. Backend Configuration

Add to your `.env` file:
```env
GOOGLE_ANDROID_CLIENT_ID=your-android-client-id.apps.googleusercontent.com
GOOGLE_WEB_CLIENT_ID=your-web-client-id.apps.googleusercontent.com
```

### 3. Mobile App Configuration

The authentication system is now ready to use! You just need to:

1. **Call sign-in in your UI** with your web client ID:
   ```kotlin
   viewModel.signInWithGoogle("your-web-client-id.apps.googleusercontent.com")
   ```

2. **All token management is automatic** - the AuthInterceptor handles everything!

## Usage Example

### In a Composable Screen

```kotlin
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    
    when (authState) {
        is AuthState.Loading -> {
            CircularProgressIndicator()
        }
        is AuthState.Success -> {
            val user = (authState as AuthState.Success).user
            Text("Welcome, ${user.displayName ?: user.email}!")
        }
        is AuthState.Error -> {
            Text("Error: ${(authState as AuthState.Error).message}")
        }
        else -> {
            Button(onClick = {
                viewModel.signInWithGoogle("YOUR_WEB_CLIENT_ID.apps.googleusercontent.com")
            }) {
                Text("Sign in with Google")
            }
        }
    }
}
```

### In a Service (e.g., SensorDataService)

```kotlin
@Inject lateinit var authRepository: AuthRepository

override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    lifecycleScope.launch {
        if (!authRepository.isLoggedIn()) {
            // User not logged in, stop service
            stopSelf()
            return@launch
        }
        
        // Continue with service logic
        startSensorCollection()
    }
    
    return START_STICKY
}
```

## Security Features

1. **Encrypted Storage**: DataStore encrypts data at rest
2. **HTTPS Only**: Tokens only sent over secure connections (configure for production)
3. **Short-lived Access Tokens**: 1-hour lifetime minimizes exposure
4. **Refresh Token Rotation**: New refresh token issued on each refresh
5. **Session Tracking**: Backend tracks all sessions and allows logout from all devices
6. **Automatic Cleanup**: Expired sessions automatically cleaned up

## Testing Checklist

- [ ] Google Sign-In works on physical device
- [ ] Access token automatically added to requests
- [ ] 401 errors trigger automatic token refresh
- [ ] Refreshed requests succeed without user intervention
- [ ] Logout clears all tokens
- [ ] App state persists across restarts (tokens saved)
- [ ] Expired refresh token redirects to login

## Common Issues

### Issue: Google Sign-In fails
**Solution**: Verify SHA-1 certificate matches in Google Cloud Console

### Issue: Backend rejects ID token
**Solution**: Ensure web client ID is used in mobile app and matches backend config

### Issue: Tokens not refreshing
**Solution**: Check AuthInterceptor is properly injected in NetworkModule

### Issue: 401 loop
**Solution**: Refresh token expired - user needs to sign in again (this is expected after 30 days)

## Production Considerations

1. **Update base URL** in NetworkModule to production backend
2. **Use release keystore** SHA-1 in Google Cloud Console
3. **Enable network security config** to enforce HTTPS
4. **Implement certificate pinning** for added security
5. **Add Proguard rules** to keep serialization classes:
   ```proguard
   -keep class com.example.harbit.data.remote.dto.** { *; }
   ```

## API Endpoints Used

- `POST /auth/google` - Initial authentication
- `POST /auth/refresh` - Token refresh (automatic)
- `POST /auth/logout` - Single session logout
- `POST /auth/logout-all` - All sessions logout
- `GET /auth/me` - Get current user info

## Dependencies

Already added to `build.gradle.kts`:
- Google Auth: `play-services-auth:21.4.0`
- Credentials API: `credentials:1.2.2`
- Google ID: `googleid:1.1.0`
- DataStore: `datastore-preferences:1.0.0`
- Retrofit: `2.11.0`
- OkHttp: `5.0.0-alpha.14`

## Flow Diagram

```
┌─────────────┐
│   User UI   │
└──────┬──────┘
       │ Sign in
       ▼
┌─────────────────────┐
│ GoogleSignInUseCase │
└──────┬──────────────┘
       │ Google ID Token
       ▼
┌─────────────────┐
│ AuthRepository  │
└──────┬──────────┘
       │ POST /auth/google
       ▼
┌─────────────────┐        ┌──────────────────┐
│  Backend API    │───────▶│ AuthPreferences  │
└─────────────────┘  Save  └──────────────────┘
                    Tokens
       │
       │ Access Token
       ▼
┌─────────────────┐
│ AuthInterceptor │ ◀─── Automatic on every request
└──────┬──────────┘
       │ 401? Refresh!
       ▼
┌─────────────────┐
│ POST /refresh   │
└─────────────────┘
```

## Next Steps

1. Create login/signup UI screens
2. Add navigation based on `isLoggedIn` state
3. Show user profile information
4. Implement logout UI
5. Handle session expiration gracefully
