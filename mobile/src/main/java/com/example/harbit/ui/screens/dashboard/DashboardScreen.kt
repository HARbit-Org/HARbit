package com.example.harbit.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.harbit.ui.components.Header
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.icons.outlined.WatchOff
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import com.example.harbit.ui.components.ActivityDistributionCard
import com.example.harbit.ui.components.AlertCard

import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onActivityDetailClick: () -> Unit,
    onHeartRateClick: () -> Unit,
    onStepsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 0.dp)
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Header(
            title = "Mi Actividad",
            subtitle = "Así es tu día hasta ahora 💙"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (true) {
                Icon(
                    imageVector = Icons.Outlined.Watch,
                    contentDescription = "Watch Connected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .padding(4.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.WatchOff,
                    contentDescription = "Watch Not connected",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .padding(4.dp)
                )
            }
        }

        // Activity Distribution Card
        ActivityDistributionCard(
            onCardClick = onActivityDetailClick
        )

        Spacer(modifier = Modifier.height(18.dp))

        AlertCard(
            message = "Has estado inactivo por más de 60 min, intenta una pausa activa"
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Health Metrics Row
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize(0.5f),
                horizontalArrangement = Arrangement
                    .spacedBy(12.dp),
            ) {
                StepsCard(
                    modifier = Modifier.weight(1f),
                    onClick = onStepsClick
                )

//            HeartRateCard(
//                modifier = Modifier.weight(1f),
//                onClick = onHeartRateClick
//            )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        AlertCard(
            message = "Construye tu bienestar paso a paso"
        )

        Spacer(modifier = Modifier.height(18.dp))
    }
}


@Composable
private fun StepsCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = "5010",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Row (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier
                        .padding(0.dp),
                    text = "pasos",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        lineHeightStyle = LineHeightStyle(
                            alignment = LineHeightStyle.Alignment.Center,
                            trim = LineHeightStyle.Trim.Both
                        )
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(18.dp))
            
            Text(
                text = "Hoy superaste tu meta 💛",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HeartRateCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Heart rate chart placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Simplified heart rate line chart
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val path = androidx.compose.ui.graphics.Path()
                    val points = listOf(0.2f, 0.8f, 0.3f, 0.6f, 0.7f, 0.4f, 0.9f, 0.5f)
                    
                    for (i in points.indices) {
                        val x = (i / (points.size - 1).toFloat()) * size.width
                        val y = (1 - points[i]) * size.height
                        
                        if (i == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }
                    
                    drawPath(
                        path = path,
                        color = Color.Red, // Using Red for heart rate as it's semantically correct
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }
    }
}
