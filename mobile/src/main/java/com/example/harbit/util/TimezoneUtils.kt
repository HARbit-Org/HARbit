package com.example.harbit.util

import java.time.Instant
import java.time.ZoneId

/**
 * Get the current timezone offset in minutes from UTC.
 * 
 * Examples:
 * - UTC-5 (Peru): returns -300
 * - UTC+0 (London): returns 0
 * - UTC+1 (Berlin): returns 60
 * 
 * Automatically handles daylight saving time.
 */
fun getCurrentTimezoneOffsetMinutes(): Int {
    return ZoneId.systemDefault()
        .rules
        .getOffset(Instant.now())
        .totalSeconds / 60
}
