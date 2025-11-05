package com.example.harbit.ui.screens.dashboard

import com.example.harbit.data.local.dao.ActivityDistribution
import com.example.harbit.data.remote.dto.ActivityDistributionItemDto
import com.example.harbit.domain.Alert
import com.example.harbit.domain.model.AlertType

object AlertGenerator {
    // Thresholds (in minutes)
    private const val SEDENTARY_THRESHOLD = 60 // 60 min continuous sedentary
    private const val ACTIVE_THRESHOLD = 40 // 40 min active is good
    private const val ALMOST_ACTIVE_THRESHOLD = 25 // 25 min active is close to the goal
    private const val MIN_DATA_THRESHOLD = 15 // At least 15 min of data to generate insights

    // Activity categories
    private val SEDENTARY_ACTIVITIES = setOf("Sit", "Write", "Eat", "Type")
    private val ACTIVE_ACTIVITIES = setOf("Walk", "Workouts")

    // Motivational messages pool
    private val MOTIVATIONAL_MESSAGES = listOf(
        "Construye tu bienestar paso a paso 💙",
        "Una vida en movimiento es una vida saludable 🌟",
        "Cada pequeño movimiento cuenta 💪",
        "Tu salud es tu mayor riqueza 🌱",
        "Hoy es un buen día para moverte 🚶",
        "El movimiento es medicina 💊",
        "Pequeños cambios, grandes resultados ✨",
        "Mantén el ritmo, vas muy bien 🎯",
        "La constancia es la clave del éxito 🔑",
        "Celebra cada logro, por pequeño que sea 🎉",
        "Tu bienestar importa 💝",
        "El ejercicio es un regalo para ti mismo 🎁",
        "Muévete por ti, por tu salud 🌈",
        "Cada día es una nueva oportunidad 🌅"
    )

    fun generateAlerts(activities: List<ActivityDistribution>, totalHours: Double): List<Alert> {
        val alerts = mutableListOf<Alert>()

        // Calculate metrics
        val totalMinutes = totalHours * 60
        val sedentaryMinutes = activities
            .filter { it.activityLabel in SEDENTARY_ACTIVITIES }
            .sumOf { it.totalMinutes }
        val activeMinutes = activities
            .filter { it.activityLabel in ACTIVE_ACTIVITIES }
            .sumOf { it.totalMinutes }

        // Check if we have enough data
        if (totalMinutes < MIN_DATA_THRESHOLD) {
            // Not enough data - show motivational messages only
            alerts.addAll(getMotivationalAlerts(3))
            return alerts
        }

        // 1. Check for excessive sedentary time (HIGH PRIORITY)
        if (sedentaryMinutes >= SEDENTARY_THRESHOLD) {
            alerts.add(
                Alert(
                    type = AlertType.Sedentary(sedentaryMinutes.toInt()),
                    message = "⚠️ Has estado inactivo por ${sedentaryMinutes.toInt()} minutos hoy. No olvides tomar pausas activas de 5 minutos cada media hora.",
                    priority = 2
                )
            )
        }

        // 2. Check for good active time (MEDIUM PRIORITY)
        if (activeMinutes >= ACTIVE_THRESHOLD) {
            alerts.add(
                Alert(
                    type = AlertType.Active(activeMinutes.toInt()),
                    message = "🎉 ¡Excelente! Has realizado más de $ACTIVE_THRESHOLD minutos de actividad hoy. Sigue así y alcanzarás los 300 semanales recomendados por la OMS.",
                    priority = 0
                )
            )
        } else if (activeMinutes >= ALMOST_ACTIVE_THRESHOLD) {
            alerts.add(
                Alert(
                    type = AlertType.Active(activeMinutes.toInt()),
                    message = "Te falta poco para alcanzar la meta de los $ACTIVE_THRESHOLD minutos activo, ¡no te rindas!",
                    priority = 0
                )
            )
        } else {
            alerts.add(
                Alert(
                    type = AlertType.Active(activeMinutes.toInt()),
                    message = "La OMS recomienda alrededor de $ACTIVE_THRESHOLD minutos de actividad diaria. El progreso está en tus manos, no olvides moverte.",
                    priority = 2
                )
            )
        }

        // 4. Check for balanced activity (LOW PRIORITY)
        val sedentaryPct = ((sedentaryMinutes / totalMinutes) * 100).toInt()
        val activePct = ((activeMinutes / totalMinutes) * 100).toInt()

        if (activePct in 20..40 && sedentaryPct < 70) {
            alerts.add(
                Alert(
                    type = AlertType.Balanced(sedentaryPct, activePct),
                    message = "Tienes un buen balance entre actividad y descanso, ¡sigue así!",
                    priority = 3
                )
            )
        }

        // 5. Add motivational messages to fill gaps
        val motivationalCount = when {
            alerts.isEmpty() -> 3 // No insights - show 3 motivational
            alerts.size == 1 -> 2 // 1 insight - add 2 motivational
            else -> 1 // 2+ insights - add 1 motivational
        }

        alerts.addAll(getMotivationalAlerts(motivationalCount))

        // Sort by priority and return top 3
        return alerts.sortedBy { it.priority }.take(5)
    }

    fun getMotivationalAlerts(count: Int): List<Alert> {
        val shuffled = MOTIVATIONAL_MESSAGES.shuffled()
        return shuffled.take(count).map { message ->
            Alert(
                type = AlertType.Motivational(message),
                message = message,
                priority = 10 // Lowest priority
            )
        }
    }
}