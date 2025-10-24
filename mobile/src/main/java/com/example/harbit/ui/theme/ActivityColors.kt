package com.example.harbit.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Activity color and display name mapping.
 * Maps database activity labels (English) to their display colors and Spanish names.
 */
data class ActivityInfo(
    val color: Color,
    val displayName: String
)

/**
 * Central mapping for all activity types.
 * Keys are the actual database values (case-insensitive).
 */
val activityInfoMap = mapOf(
    "sit" to ActivityInfo(sedentarismColor, "Sentado"),
    "walk" to ActivityInfo(walkColor, "Caminar"),
    "type" to ActivityInfo(typeColor, "Tipear"),
    "eat" to ActivityInfo(eatColor, "Comer"),
    "workouts" to ActivityInfo(exerciseColor, "Ejercicio"),
    "write" to ActivityInfo(writeColor, "Escribir"),
    "others" to ActivityInfo(otherColor, "Otros")
)

/**
 * Get activity info for a given label (case-insensitive).
 * Returns default "Otro" info if not found.
 */
fun getActivityInfo(label: String): ActivityInfo {
    return activityInfoMap[label.lowercase()] 
        ?: ActivityInfo(otherColor, label.capitalize())
}
