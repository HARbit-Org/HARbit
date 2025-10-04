package com.example.harbit.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun ActivityPieChart() {
    val density = LocalDensity.current
    val strokeWidth = with(density) { 35.dp.toPx() }

    Canvas(
        modifier = Modifier
            .size(160.dp)
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = (size.width / 2f) - strokeWidth / 2f

        // Activity data (percentages)
        val activities = listOf(
            Triple(Color(0xFFE57373), 31f, "Sedentario"), // Light red for sedentary
            Triple(Color(0xFF81C784), 16f, "Caminar"), // Light green for walking
            Triple(Color(0xFF64B5F6), 16f, "De pie"), // Light blue for standing
            Triple(Color(0xFFFFB74D), 21f, "Ejercicio"), // Orange for exercise
            Triple(Color(0xFFBA68C8), 16f, "Otro") // Purple for other
        )

        var startAngle = -90f

        activities.forEach { (color, percentage, _) ->
            val sweepAngle = (percentage / 100f) * 360f

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )

            startAngle += sweepAngle
        }
    }
}