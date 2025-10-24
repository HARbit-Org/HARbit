# Watch Connection Monitoring System

## Overview
This document describes the ping/pong system that monitors the connection status between the mobile app and the smartwatch in real-time.

## How It Works

### Architecture
The system uses a bidirectional ping/pong mechanism over the Wearable MessageClient API:

```
Mobile App                          Smartwatch
    |                                    |
    |---- PING (/ping) ---------------->|
    |                                    |
    |<--- PONG (/pong) ------------------|
    |                                    |
    | ✓ Connection confirmed             |
```

### Components

#### 1. **WatchConnectionManager** (Mobile - Singleton)
- **Location**: `mobile/src/main/java/com/example/harbit/service/WatchConnectionManager.kt`
- **Responsibilities**:
  - Sends ping messages every 5 minutes to all connected watches
  - Listens for pong responses
  - Manages connection state (`isWatchConnected: StateFlow<Boolean>`)
  - Implements timeout mechanism (10 seconds) - if no pong received, marks as disconnected

**Key Methods**:
- `startMonitoring()`: Begins periodic pings and registers message listener
- `stopMonitoring()`: Stops pings and unregisters listener
- `checkConnection()`: Sends immediate ping for instant connection check
- `onMessageReceived(messageEvent)`: Handles pong responses from watch

**Timing Configuration**:
```kotlin
private const val PING_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes
private const val RESPONSE_TIMEOUT_MS = 10_000L     // 10 seconds
```

#### 2. **SensorService** (Watch)
- **Location**: `wear/src/main/java/com/example/harbit/sensors/SensorService.kt`
- **Responsibilities**:
  - Listens for ping messages from mobile
  - Responds immediately with pong message
  - Continues normal sensor data collection

**Message Handling**:
```kotlin
override fun onMessageReceived(messageEvent: MessageEvent) {
    if (messageEvent.path == PING_PATH) {
        // Respond with pong
        msgClient.sendMessage(messageEvent.sourceNodeId, PONG_PATH, ByteArray(0))
    }
}
```

#### 3. **ActivityDistributionViewModel** (Mobile)
- **Location**: `mobile/src/main/java/com/example/harbit/ui/screens/dashboard/ActivityDistributionViewModel.kt`
- **Responsibilities**:
  - Injects WatchConnectionManager
  - Exposes connection state to UI: `val isWatchConnected: StateFlow<Boolean>`
  - Starts/stops monitoring based on screen lifecycle

**Integration**:
```kotlin
@HiltViewModel
class ActivityDistributionViewModel @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val sensorDataEvents: SensorDataEvents,
    private val watchConnectionManager: WatchConnectionManager
) : ViewModel() {
    
    val isWatchConnected: StateFlow<Boolean> = watchConnectionManager.isWatchConnected
    
    fun startListeningForDataUploads() {
        watchConnectionManager.startMonitoring()
        // ...
    }
    
    fun stopListeningForDataUploads() {
        watchConnectionManager.stopMonitoring()
        // ...
    }
}
```

#### 4. **DashboardScreen** (Mobile UI)
- **Location**: `mobile/src/main/java/com/example/harbit/ui/screens/dashboard/DashboardScreen.kt`
- **Responsibilities**:
  - Observes connection state
  - Displays appropriate icon based on connection status

**UI Logic**:
```kotlin
val isWatchConnected by viewModel.isWatchConnected.collectAsStateWithLifecycle()

// In LaunchedEffect
viewModel.checkWatchConnection() // Initial check

// Conditional icon rendering
if (isWatchConnected) {
    Icon(imageVector = Icons.Outlined.Watch, tint = Blue) // Connected
} else {
    Icon(imageVector = Icons.Outlined.WatchOff, tint = Red) // Disconnected
}
```

## Message Paths

| Path | Direction | Purpose | Payload |
|------|-----------|---------|---------|
| `/ping` | Mobile → Watch | Request connection confirmation | Empty (0 bytes) |
| `/pong` | Watch → Mobile | Confirm connection | Empty (0 bytes) |
| `/sensor_data` | Watch → Mobile | Send sensor batches | Binary sensor data |

## State Flow

### Initial State
- `isWatchConnected = false` (default)
- Icon shows: Red WatchOff ❌

### When Dashboard Opens
1. `DashboardScreen` calls `viewModel.startListeningForDataUploads()`
2. ViewModel calls `watchConnectionManager.startMonitoring()`
3. Manager sends immediate ping
4. Also starts 5-minute periodic ping timer

### When Ping is Sent
1. Manager sends `/ping` message to all connected nodes
2. Starts 10-second timeout timer
3. Waits for `/pong` response

### When Pong is Received
1. Manager receives `/pong` from watch
2. Sets `isWatchConnected = true`
3. Cancels timeout timer
4. UI updates to show blue Watch icon ✅

### When Timeout Occurs (No Pong)
1. 10 seconds pass without pong response
2. Manager sets `isWatchConnected = false`
3. UI updates to show red WatchOff icon ❌

### When Dashboard Closes
1. `DisposableEffect` triggers `viewModel.stopListeningForDataUploads()`
2. ViewModel calls `watchConnectionManager.stopMonitoring()`
3. Periodic pings stop
4. Message listener unregistered
5. Connection state resets to `false`

## Lifecycle Management

### ViewModel Lifecycle
```kotlin
override fun onCleared() {
    super.onCleared()
    stopListeningForDataUploads() // Ensures cleanup
}
```

### Screen Lifecycle
```kotlin
LaunchedEffect(Unit) {
    viewModel.startListeningForDataUploads()
    viewModel.checkWatchConnection() // Immediate check on load
}

DisposableEffect(Unit) {
    onDispose {
        viewModel.stopListeningForDataUploads() // Stop when leaving screen
    }
}
```

### WatchConnectionManager Lifecycle
- **Singleton**: Single instance shared across app
- **Cleanup**: Should be called in Application.onTerminate() if needed
- Currently managed by ViewModel lifecycle (starts/stops with Dashboard)

## Benefits

### 1. **Real-time Connection Status**
- Users know immediately if watch is connected
- No guessing about sync status

### 2. **Battery Efficient**
- Only pings every 5 minutes (not continuous polling)
- Empty payloads (0 bytes) minimize data transfer
- Stops monitoring when screen is not visible

### 3. **Automatic Recovery**
- If watch reconnects, next ping will detect it
- No manual refresh needed

### 4. **Reliable Detection**
- Timeout mechanism handles unresponsive watches
- Based on actual bidirectional communication (not just Bluetooth pairing)

### 5. **Reactive UI**
- StateFlow ensures UI updates automatically
- Compose recomposition triggered by state changes
- No manual UI updates needed

## Configuration

To adjust timing, modify constants in `WatchConnectionManager`:

```kotlin
// Increase ping frequency (e.g., every 2 minutes)
private const val PING_INTERVAL_MS = 2 * 60 * 1000L

// Increase timeout tolerance (e.g., 20 seconds)
private const val RESPONSE_TIMEOUT_MS = 20_000L
```

## Testing

### Manual Testing
1. **Connection Test**:
   - Open Dashboard screen
   - Verify blue Watch icon appears when watch is connected
   - Turn off watch or disable Bluetooth
   - Wait 10 seconds + next ping cycle
   - Verify icon changes to red WatchOff

2. **Recovery Test**:
   - Start with watch disconnected (red icon)
   - Turn on watch or enable Bluetooth
   - Wait up to 5 minutes for next ping
   - Verify icon changes to blue Watch

3. **Immediate Check**:
   - With watch connected, background the app
   - Disconnect watch
   - Re-open Dashboard
   - Should send immediate ping and detect disconnection within 10 seconds

### Logs
Monitor LogCat for:
```
WatchConnectionManager: Watch connection monitoring started
WatchConnectionManager: Sending ping to X node(s)
SensorService: Ping received from mobile, sending pong...
WatchConnectionManager: Pong received from watch - connection confirmed
```

Or for disconnection:
```
WatchConnectionManager: Ping timeout - no response from watch
WatchConnectionManager: No connected nodes found
```

## Future Enhancements

1. **Adaptive Ping Interval**: Ping more frequently when app is active, less when backgrounded
2. **Connection Quality**: Measure ping response time to indicate signal strength
3. **Notification on Disconnect**: Alert user if watch disconnects unexpectedly
4. **Last Seen Timestamp**: Show "Last connected: 2 minutes ago" when disconnected
5. **Manual Reconnect**: Add button to force immediate ping check
