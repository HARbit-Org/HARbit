package com.example.harbit.domain.model

sealed class AlertType {
    data class Sedentary(val minutes: Int) : AlertType()
    data class Active(val minutes: Int) : AlertType()
    data class LowActivity(val totalMinutes: Int) : AlertType()
    data class Balanced(val sedentaryPct: Int, val activePct: Int) : AlertType()
    data class Motivational(val message: String) : AlertType()
    data class NoActivity(val totalMinutes: Int) : AlertType()
    data class WorkoutDetected(val minutes: Int) : AlertType()
    data class ErgonomicBreak(val deskMinutes: Int) : AlertType()
    data class HighSedentaryRatio(val sedentaryPct: Int) : AlertType()
}