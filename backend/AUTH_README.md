# HARbit Authentication System

## Overview

Complete Google OAuth authentication system with JWT session management, implementing:
- Google OAuth 2.0 (Android & Web clients)
- JWT-based access tokens
- Refresh tokens for extended sessions
- User and provider management
- Session tracking with device info

## Architecture

```
┌──────────────┐
│   Mobile App │
│  (Android)   │
└──────┬───────┘
       │ 1. Google Sign In
       │    (ID Token)
       ▼
┌──────────────────────────────────┐
│  FastAPI Backend                 │
│  ┌────────────────────────────┐  │
│  │ Auth Controller            │  │
│  │  - /auth/google           │  │
│  │  - /auth/refresh          │  │
│  │  - /auth/logout           │  │
│  └────────┬───────────────────┘  │
│           ▼                      │
│  ┌────────────────────────────┐  │
│  │ Auth Service               │  │
│  │  - Verify Google token    │  │
│  │  - Create/find user       │  │
│  │  - Generate JWT           │  │
│  └────────┬───────────────────┘  │
│           ▼                      │
│  ┌─────────────────┬──────────┐  │
│  │ UserRepository  │ SessionRepo │
│  └─────────┬───────┴──────────┘  │
│            ▼                      │
│  ┌────────────────────────────┐  │
│  │ PostgreSQL Database        │  │
│  │  - users                   │  │
│  │  - user_providers          │  │
│  │  - user_sessions           │  │
│  └────────────────────────────┘  │
└──────────────────────────────────┘
```

## Setup

### 1. Install Dependencies

```bash
cd backend
pip install -r requirements-auth.txt
```

### 2. Run Database Migration

```bash
psql -U your_user -d harbit_db -f migrations/001_add_auth_tables.sql
```

### 3. Configure Environment Variables

Create a `.env` file in the backend directory:

```bash
cp .env.example .env
```

Edit `.env` with your actual values:

```env
# Google OAuth Configuration
GOOGLE_ANDROID_CLIENT_ID=your-android-client-id.apps.googleusercontent.com
GOOGLE_WEB_CLIENT_ID=your-web-client-id.apps.googleusercontent.com

# JWT Configuration (CHANGE THIS!)
JWT_SECRET_KEY=use-a-random-secure-key-here

# Database
DATABASE_URL=postgresql://user:password@localhost:5432/harbit_db
```

### 4. Update main.py

Add the auth router to your FastAPI app:

```python
from api.v1 import authController

app = FastAPI()

# Include auth routes
app.include_router(authController.router, prefix="/api/v1")
```

## API Endpoints

### 1. Google Authentication

**POST `/api/v1/auth/google`**

Request:
```json
{
  "id_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6...",
  "client_type": "android"
}
```

Response:
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "abc123xyz456...",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "email": "user@example.com",
    "display_name": "John Doe",
    "picture_url": "https://lh3.googleusercontent.com/...",
    "daily_step_goal": 10000,
    "timezone": "America/Lima"
  },
  "expires_in": 3600,
  "token_type": "Bearer"
}
```

### 2. Refresh Token

**POST `/api/v1/auth/refresh`**

Request:
```json
{
  "refresh_token": "abc123xyz456..."
}
```

Response: Same as Google auth

### 3. Logout

**POST `/api/v1/auth/logout`**

Headers:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Response:
```json
{
  "message": "Logged out successfully"
}
```

## Protecting Routes

Use the `get_current_user` dependency in your endpoints:

```python
from api.v1.authController import get_current_user
from fastapi import Depends

@router.post("/api/sensor-data/upload")
async def upload_sensor_data(
    request: SensorBatchUploadRequest,
    current_user: dict = Depends(get_current_user)
):
    user_id = current_user['user_id']
    user_email = current_user['email']
    
    # Process sensor data for this authenticated user
    # ...
```

## Mobile Integration

See the main implementation guide for complete Android setup with:
- Google Sign-In credential manager
- Token storage with PreferencesRepository
- Auth interceptor for automatic token inclusion
- Service authentication checks

## Security Notes

1. **JWT Secret**: Use a strong random key in production
2. **HTTPS**: Always use HTTPS in production
3. **Token Expiry**: Access tokens expire in 1 hour
4. **Session Management**: Sessions are tracked with device info
5. **Google Tokens**: Verified server-side with Google's API

## Database Schema

### users
- `id`: UUID primary key
- `email`: Unique user email
- `display_name`: User's display name
- `picture_url`: Profile picture URL
- `sex`, `birth_year`: Optional profile data
- `daily_step_goal`: Default 10,000
- `timezone`: Default "America/Lima"
- `created_at`, `updated_at`: Timestamps

### user_providers
- `id`: UUID primary key
- `user_id`: Foreign key to users
- `provider`: "google"
- `provider_user_id`: Google's user ID (sub claim)
- `created_at`: Timestamp

### user_sessions
- `id`: UUID primary key
- `user_id`: Foreign key to users
- `access_token`: JWT token
- `refresh_token`: Random secure token
- `expires_at`: Token expiration
- `created_at`, `last_used_at`: Timestamps
- `user_agent`, `ip_address`: Device tracking

## Testing

Test authentication with curl:

```bash
# 1. Get ID token from mobile app (log it)
# 2. Test authentication
curl -X POST http://localhost:8000/api/v1/auth/google \
  -H "Content-Type: application/json" \
  -d '{"id_token": "YOUR_ID_TOKEN", "client_type": "android"}'

# 3. Test protected endpoint
curl -X GET http://localhost:8000/api/sensor-data/upload \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Troubleshooting

**"Invalid Google token"**
- Check client_id matches Google Cloud Console
- Ensure token is not expired
- Verify `client_type` is correct

**"Session not found"**
- Access token may have expired
- Use refresh token to get new access token

**"Missing authorization header"**
- Include `Authorization: Bearer <token>` header
- Check token format
