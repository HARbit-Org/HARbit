# 🚀 Quick Start: Sensor Data Sync

## ✅ What's Working Now

Your app now has a **complete sensor data sync system** that:
- ✅ Receives 50KB batches from smartwatch every ~2 minutes
- ✅ Stores batches in local SQLite database
- ✅ Runs 24/7 as a foreground service (even when app is closed)
- ✅ Shows persistent notification with sync status
- ✅ Ready to upload to backend every 15-30 minutes

## 📱 How to Test

### 1. Install on Mobile
```powershell
.\gradlew mobile:installDebug
```

### 2. Launch the App
- Open HARbit app on your phone
- You should see a notification: **"HARbit Sync Active"**
- This means the service is running!

### 3. Check Logcat
```powershell
adb logcat -s SensorDataService
```

You should see:
```
SensorDataService: SensorDataService started - listening for smartwatch data
SensorDataService: Connected to X node(s): [device names]
```

### 4. Send Data from Watch
When your watch sends a batch, you'll see:
```
SensorDataService: Received batch from watch: 51198 bytes, ~2438 samples
SensorDataService: TODO: Upload 1 batches to backend API
```

### 5. Check Database
The data is being stored! You can verify with:
```kotlin
// In any coroutine scope in your app
val count = App.database.sensorBatchDao().getUnsentCount()
Log.d("Test", "Batches in DB: $count")
```

## 🔧 What You Need to Do Next

### Backend Integration

The service is currently logging `"TODO: Upload X batches to backend API"` because you need to implement the actual HTTP upload.

**Open:** `mobile/src/main/java/com/example/harbit/service/SensorDataService.kt`

**Find this function (around line 158):**
```kotlin
private suspend fun uploadToBackendAPI(batches: List<SensorBatchEntity>): Boolean {
    // TODO: Implement actual HTTP upload to your backend
    Log.d(TAG, "TODO: Upload ${batches.size} batches to backend API")
    return false // Replace with actual upload result
}
```

**Replace it with your backend call:**
```kotlin
private suspend fun uploadToBackendAPI(batches: List<SensorBatchEntity>): Boolean {
    return try {
        val payload = BackendPayload(
            userId = "your_user_id", // Get from auth
            batches = batches.map { batch ->
                BatchInfo(
                    timestamp = batch.timestamp,
                    deviceId = batch.deviceId,
                    sampleCount = batch.sampleCount,
                    data = android.util.Base64.encodeToString(
                        batch.batchData, 
                        android.util.Base64.NO_WRAP
                    )
                )
            }
        )
        
        // Use your Retrofit instance
        val response = yourBackendApi.uploadSensorData(payload)
        response.isSuccessful
        
    } catch (e: Exception) {
        Log.e(TAG, "Upload failed", e)
        false
    }
}
```

## ⚙️ Configuration

### Change Upload Frequency

**File:** `SensorDataService.kt` (line 26-27)

```kotlin
// Current: Upload every 15 minutes or when 5+ batches
private const val UPLOAD_INTERVAL_MINUTES = 15
private const val MIN_BATCHES_TO_UPLOAD = 5

// Change to 30 minutes:
private const val UPLOAD_INTERVAL_MINUTES = 30
```

### Data Flow Summary

```
📡 Watch sends 50KB batch every ~2 min
         ↓
💾 Mobile stores in SQLite
         ↓
⏰ Every 15 min (or 5+ batches)
         ↓
🌐 Upload to backend (when you implement it)
         ↓
✅ Mark as uploaded, clean up old data
```

## 📊 Monitoring

### Check Notification
Pull down notification shade:
```
HARbit Sync Active
7 batches pending (0.3 MB)
```

### Check Logs
```
SensorDataService: Received batch from watch: 51198 bytes, ~2438 samples
SensorDataService: Uploading 7 batches to backend...
SensorDataService: Successfully uploaded 7 batches
```

### Query Database
```kotlin
lifecycleScope.launch {
    val dao = App.database.sensorBatchDao()
    val unsent = dao.getUnsentCount()
    val size = dao.getUnsentDataSize() ?: 0L
    Log.d("Sync", "Pending: $unsent batches (${size / 1024}KB)")
}
```

## 🐛 Troubleshooting

### Service not starting?
- Check logcat for "SensorDataService started"
- Make sure permissions are granted
- Restart the app

### Not receiving data from watch?
- Ensure watch and phone are paired
- Check watch SensorService is running
- Verify message path is `/sensor_data` on both sides

### Batches not uploading?
- This is expected! You haven't implemented `uploadToBackendAPI()` yet
- You'll see "TODO: Upload X batches to backend API" in logs
- Once you implement it, uploads will work automatically

## 📂 Key Files

```
mobile/
├── src/main/java/com/example/harbit/
│   ├── App.kt                          ← Initializes database
│   ├── MainActivity.kt                  ← Starts service
│   ├── data/local/
│   │   ├── SensorDatabase.kt           ← Room database
│   │   ├── SensorBatchEntity.kt        ← Data model
│   │   └── SensorBatchDao.kt           ← Database operations
│   └── service/
│       └── SensorDataService.kt        ← Main service (edit this!)
└── src/main/AndroidManifest.xml        ← Service registered

IMPLEMENTATION_SUMMARY.md                ← Full docs
SENSOR_SYNC_README.md                    ← Architecture details
```

## 🎯 Current Status

- ✅ **Database**: Fully working
- ✅ **Service**: Running and receiving data
- ✅ **Storage**: Batches saved to SQLite
- ✅ **Notification**: Shows sync status
- ⏳ **Backend Upload**: Waiting for your implementation

**Next Step:** Implement `uploadToBackendAPI()` with your actual backend endpoint!

---

**Questions?** Check `IMPLEMENTATION_SUMMARY.md` for full documentation.
