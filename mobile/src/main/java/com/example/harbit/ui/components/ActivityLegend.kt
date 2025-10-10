package com.example.harbit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.harbit.ui.theme.eatColor
import com.example.harbit.ui.theme.exerciseColor
import com.example.harbit.ui.theme.otherColor
import com.example.harbit.ui.theme.sedentarismColor
import com.example.harbit.ui.theme.standColor
import com.example.harbit.ui.theme.walkColor

@Composable
fun ActivityLegend() {
    val activities = listOf(
        Triple(sedentarismColor, "Sedentario", "30%"),
        Triple(walkColor, "Caminar", "20%"),
        Triple(standColor, "De pie", "10%"),
        Triple(eatColor, "Comer", "10%"),
        Triple(otherColor, "Otro", "15%"),
        Triple(exerciseColor, "Ejercicio", "15%"),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        activities.chunked(3).forEach { row ->
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
                            text = name,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}