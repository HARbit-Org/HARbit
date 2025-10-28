# FCM Token Endpoint Implementation

## ✅ Endpoint Implemented

### `PATCH /users/fcm-token`

**Description:** Updates the FCM token for the authenticated user.

**Authentication:** Bearer token required

**Request Body:**
```json
{
  "fcmToken": "dQw4w9WgXcQ:APA91bF..."
}
```

**Response (200 OK):**
```json
{
  "status": "success",
  "message": "FCM token updated successfully"
}
```

**Response (401 Unauthorized):**
```json
{
  "detail": "Not authenticated"
}
```

**Response (500 Internal Server Error):**
```json
{
  "detail": "Failed to update FCM token: <error_message>"
}
```

## Files Created

1. **`model/dto/request/updateFcmTokenRequestDto.py`**
   - Request DTO with `fcmToken` field
   - Uses Pydantic alias for camelCase/snake_case conversion

2. **`model/dto/response/updateFcmTokenResponseDto.py`**
   - Response DTO with `status` and `message` fields

3. **`test_fcm_endpoint.py`**
   - Test script to verify the endpoint works

## Files Modified

1. **`service/userService.py`**
   - Added `update_fcm_token()` method
   - Updates `fcm_token` and `fcm_updated_at` fields
   - Uses UTC timezone for timestamp

2. **`api/v1/userController.py`**
   - Added `PATCH /fcm-token` endpoint
   - Extracts user ID from JWT token
   - Calls `UserService.update_fcm_token()`
   - Returns success/error response

## Database Schema

The `users` table already has the required fields:

```sql
fcm_token TEXT NULL
fcm_updated_at TIMESTAMPTZ NULL
```

These fields are updated when the endpoint is called.

## How It Works

1. **Mobile app** gets FCM token from Firebase
2. **Mobile app** sends token to `PATCH /users/fcm-token`
3. **Backend** extracts user ID from JWT
4. **Backend** updates `users.fcm_token` and `users.fcm_updated_at`
5. **Backend** returns success response

## Testing

### Using curl:

```bash
curl -X PATCH http://localhost:8000/users/fcm-token \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fcmToken": "dQw4w9WgXcQ:APA91bF_test_token"
  }'
```

### Using Python script:

```bash
cd backend
python test_fcm_endpoint.py
```

(Remember to update the `ACCESS_TOKEN` in the script first)

### Using Swagger/OpenAPI:

1. Go to `http://localhost:8000/docs`
2. Authorize with your JWT token
3. Find `PATCH /users/fcm-token` endpoint
4. Click "Try it out"
5. Enter FCM token in the request body
6. Click "Execute"

## Integration with Mobile App

The mobile app automatically calls this endpoint:

1. **On app start** - via `FCMTokenManager.refreshToken()`
2. **On token refresh** - via `HARbitFirebaseMessagingService.onNewToken()`

The token is automatically sent to the backend without manual intervention.

## Next Steps

Now that the endpoint is implemented, you can:

1. **Test the endpoint** using the provided test script
2. **Run the mobile app** and verify the token is sent
3. **Check the database** to confirm the token is stored:
   ```sql
   SELECT id, email, fcm_token, fcm_updated_at 
   FROM users 
   WHERE email = 'your_email@example.com';
   ```
4. **Implement notification sending** using Firebase Admin SDK

## Security Notes

- ✅ Endpoint requires authentication (JWT token)
- ✅ User can only update their own FCM token
- ✅ FCM token is stored in the database (not exposed in responses)
- ✅ Timestamp tracks when token was last updated

## Error Handling

The endpoint handles the following errors:

- **401**: User not authenticated
- **404**: User not found in database
- **500**: Database error or other internal error

All errors are logged with detailed information for debugging.
