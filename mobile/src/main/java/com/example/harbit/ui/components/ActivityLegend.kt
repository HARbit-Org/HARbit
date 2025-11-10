package com.example.harbit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.harbit.data.local.dao.ActivityDistribution
import com.example.harbit.ui.theme.getActivityInfo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityLegend(activities: List<ActivityDistribution>) {
    val displayActivities = activities.map { activity ->
        val activityInfo = getActivityInfo(activity.activityLabel)
        Triple(
            activityInfo.color,
            activityInfo.displayName,
            String.format("%.2f%%", activity.percentage)
        )
    }

    val SEDENTARY_ACTIVITIES = setOf("Sit", "Write", "Eat", "Type")
    val ACTIVE_ACTIVITIES = setOf("Walk", "Workouts")

    var sedentaryPct = 0.0
    var activePct = 0.0
    var otherPct = 0.0

    for (activity in activities) {
        when (activity.activityLabel) {
            in SEDENTARY_ACTIVITIES -> {
                sedentaryPct += activity.percentage
            }
            in ACTIVE_ACTIVITIES -> {
                activePct += activity.percentage
            }
            else -> {
                otherPct += activity.percentage
            }
        }
    }

    val sedentaryPctFormatted = String.format("%.2f%%", sedentaryPct)
    val activePctFormatted = String.format("%.2f%%", activePct)
    val otherPctFormatted = String.format("%.2f%%", otherPct)


    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        displayActivities.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { (color, name, percentage) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$name ($percentage)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    RoundedCornerShape(8.dp)
                )
                .padding(2.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // ✅ Info popup positioned in top right corner
                InfoPopup(
                    infoText = """
                    • Sedentario: Actividades como sentarse, escribir, comer y escribir en un teclado
                    
                    • Activo: Caminar y ejercicios (fútbol, básquet, entre otros)
                    
                    • Otros: Actividades que no se clasifican en las categorías anteriores
                """.trimIndent(),
                    title = "Categorías de Actividad"
                )
            }

            FlowRow (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp, start = 14.dp, end = 14.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                Text(
                    text = "Sedentario ($sedentaryPctFormatted)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Activo ($activePctFormatted)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Otros ($otherPctFormatted)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}