# Sensor Data Sync Architecture

## Overview
This architecture enables continuous sensor data collection from smartwatch to mobile to backend, working 24/7 even when the app is closed.

## Data Flow

```
Smartwatch (20Hz sampling)
    ↓ (batch every ~2 min, 50KB)
Mobile Foreground Service
    ↓ (store in SQLite)
Local Database Buffer
    ↓ (upload every 15-30 min)
Backend API
```

## Components

### 1. Smartwatch (wear module)
- **SensorService.kt**: Samples accelerometer/gyroscope at 20Hz
- **Batching**: Accumulates ~2438 samples (~50KB)
- **Transmission**: Sends via Wearable MessageClient every ~2 minutes

### 2. Mobile (mobile module)

#### Database Layer
- **SensorDatabase.kt**: Room database
- **SensorBatchEntity.kt**: Stores raw 50KB batches from watch
- **SensorBatchDao.kt**: CRUD operations
- **Converters.kt**: ByteArray ↔ String conversion for Room

#### Service Layer
- **SensorDataService.kt**: Foreground service that:
  - Receives batches from smartwatch (MessageClient.OnMessageReceivedListener)
  - Stores in local SQLite
  - Uploads to backend every 15-30 minutes
  - Shows persistent notification with sync status

### 3. Backend API (to be implemented)
- Endpoint: `POST /api/v1/sensor-data`
- Payload: Multiple 50KB batches compressed
- Authentication: Bearer token

## Configuration

### Current Settings (SensorDataService.kt)
```kotlin
private const val UPLOAD_INTERVAL_MINUTES = 15  // Upload every 15 minutes
private const val MIN_BATCHES_TO_UPLOAD = 5     // Or when we have 5+ batches
```

### Batch Sizes
- **Smartwatch → Mobile**: 50KB per batch (~2438 samples)
- **Mobile → Backend**: Multiple batches (250KB-500KB recommended)

## Setup Instructions

### Step 1: Initialize Database in Application class

Create or update `App.kt`:

```kotlin
package com.example.harbit

import android.app.Application
import androidx.room.Room
import com.example.harbit.data.local.SensorDatabase

class App : Application() {
    
    companion object {
        lateinit var database: SensorDatabase
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Room database
        database = Room.databaseBuilder(
            applicationContext,
            SensorDatabase::class.java,
            "sensor_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
}
```

### Step 2: Update AndroidManifest.xml

Already done! The service is registered with:
```xml
<service
    android:name=".service.SensorDataService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="dataSync" />
```

### Step 3: Inject Database into Service

Update `SensorDataService.kt` onCreate:

```kotlin
override fun onCreate() {
    super.onCreate()
    
    // Initialize database
    sensorBatchDao = App.database.sensorBatchDao()
    
    // ... rest of onCreate
}
```

### Step 4: Start Service from MainActivity

Add to `MainActivity.kt`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Start foreground service
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(Intent(this, SensorDataService::class.java))
    } else {
        startService(Intent(this, SensorDataService::class.java))
    }
    
    // ... rest of onCreate
}
```

### Step 5: Implement Backend Upload

Replace the `uploadToBackendAPI()` placeholder in `SensorDataService.kt`:

```kotlin
private suspend fun uploadToBackendAPI(batches: List<SensorBatchEntity>): Boolean {
    return try {
        val payload = BackendPayload(
            userId = getUserId(), // Get from auth
            batches = batches.map { batch ->
                BatchData(
                    timestamp = batch.timestamp,
                    deviceId = batch.deviceId,
                    sampleCount = batch.sampleCount,
                    data = Base64.encodeToString(batch.batchData, Base64.NO_WRAP)
                )
            }
        )
        
        val response = retrofitClient.uploadSensorData(payload)
        response.isSuccessful
        
    } catch (e: Exception) {
        Log.e(TAG, "Upload failed", e)
        false
    }
}
```

## Data Format

### Batch Structure (from watch)
Each sample in the 50KB batch:
```
[8 bytes: timestamp]
[1 byte: sensor type] (1 = accelerometer, 2 = gyroscope)
[4 bytes: x value]
[4 bytes: y value]
[4 bytes: z value]
Total: 21 bytes per sample
```

### Backend Payload (JSON)
```json
{
  "userId": "user123",
  "batches": [
    {
      "timestamp": 1234567890123,
      "deviceId": "watch_node_id",
      "sampleCount": 2438,
      "data": "base64_encoded_50kb_binary..."
    }
  ]
}
```

## Monitoring & Debugging

### Check Service Status
```kotlin
// In your app UI
lifecycleScope.launch {
    val unsentCount = App.database.sensorBatchDao().getUnsentCount()
    val unsentSize = App.database.sensorBatchDao().getUnsentDataSize()
    Log.d("Sync", "Pending: $unsentCount batches, ${unsentSize / 1024} KB")
}
```

### Logcat Filters
- `SensorDataService`: Mobile service logs
- `SensorService`: Watch service logs

## Performance Characteristics

### Battery Impact
- **Smartwatch**: ~5-10% per day (20Hz sampling + 2min batching)
- **Mobile**: ~1-2% per day (passive receiver + 15min uploads)

### Data Volume
- **Per day**: ~36MB raw sensor data
- **With compression**: ~10-15MB (gzip reduces by ~70%)
- **Monthly**: ~300-450MB

### Storage
- **Mobile SQLite**: Stores ~7 days of data
- **Auto-cleanup**: Deletes uploaded data older than 7 days

## Testing

1. **Watch → Mobile**: Check logcat for "Received batch from watch"
2. **Database**: Query `sensor_batches` table
3. **Upload**: Check "Successfully uploaded X batches"
4. **Notification**: Shows pending batch count

## Next Steps

1. ✅ Database setup complete
2. ✅ Foreground service implemented
3. ✅ Manifest updated
4. ⏳ Initialize database in App.kt
5. ⏳ Start service from MainActivity
6. ⏳ Implement backend API upload
7. ⏳ Add retry logic for failed uploads
8. ⏳ Add network type detection (WiFi vs mobile data)
9. ⏳ Add battery optimization handling
