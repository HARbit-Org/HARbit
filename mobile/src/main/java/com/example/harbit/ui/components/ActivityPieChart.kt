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
import com.example.harbit.ui.theme.eatColor
import com.example.harbit.ui.theme.exerciseColor
import com.example.harbit.ui.theme.otherColor
import com.example.harbit.ui.theme.sedentarismColor
import com.example.harbit.ui.theme.standColor
import com.example.harbit.ui.theme.walkColor

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
            Triple(sedentarismColor, 30f, "Sedentario"),
            Triple(walkColor, 20f, "Caminar"),
            Triple(standColor, 10f, "De pie"),
            Triple(eatColor, 10f, "Comer"),
            Triple(otherColor, 15f, "Otro"),
            Triple(exerciseColor, 15f, "Ejercicio"),
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