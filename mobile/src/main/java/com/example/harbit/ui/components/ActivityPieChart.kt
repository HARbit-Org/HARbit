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
import com.example.harbit.data.local.dao.ActivityDistribution
import com.example.harbit.ui.theme.getActivityInfo

@Composable
fun ActivityPieChart(activities: List<ActivityDistribution>) {
    val density = LocalDensity.current
    val strokeWidth = with(density) { 35.dp.toPx() }

    Canvas(
        modifier = Modifier
            .size(160.dp)
    ) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val radius = (size.width / 2f) - strokeWidth / 2f

        var startAngle = -90f

        activities.forEach { activity ->
            val activityInfo = getActivityInfo(activity.activityLabel)
            val sweepAngle = (activity.percentage / 100f) * 360f

            drawArc(
                color = activityInfo.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle.toFloat(),
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )

            startAngle += sweepAngle.toFloat()
        }
    }
}