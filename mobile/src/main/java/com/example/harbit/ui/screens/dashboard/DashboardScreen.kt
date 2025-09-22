package com.example.harbit.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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

import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DashboardScreen(
    onActivityDetailClick: () -> Unit,
    onHeartRateClick: () -> Unit,
    onStepsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        DashboardHeader()
        
        // Activity Distribution Card
        ActivityDistributionCard(
            onCardClick = onActivityDetailClick
        )
        
        // Health Metrics Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StepsCard(
                modifier = Modifier.weight(1f),
                onClick = onStepsClick
            )
            
            HeartRateCard(
                modifier = Modifier.weight(1f),
                onClick = onHeartRateClick
            )
        }
        
        // Inactivity Alert
        InactivityAlertCard()
        
        Spacer(modifier = Modifier.height(100.dp)) // Space for bottom navigation
    }
}

@Composable
private fun DashboardHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Mi Actividad",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Así es tu día hasta ahora 💙",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Watch connection indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

@Composable
private fun ActivityDistributionCard(
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Activity Chart
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                ActivityPieChart()
                
                // Center text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "14 hs",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "de actividad",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Activity Legend
            ActivityLegend()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Has estado inactivo por más de 60 min, intenta una pausa activa",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun ActivityPieChart() {
    val density = LocalDensity.current
    val strokeWidth = with(density) { 20.dp.toPx() }
    
    Canvas(
        modifier = Modifier.size(200.dp)
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

@Composable
private fun ActivityLegend() {
    val activities = listOf(
        Triple(Color(0xFFE57373), "Sedentario", "31%"), // Light red for sedentary
        Triple(Color(0xFF81C784), "Caminar", "16%"), // Light green for walking
        Triple(Color(0xFF64B5F6), "De pie", "16%"), // Light blue for standing
        Triple(Color(0xFFFFB74D), "Ejercicio", "21%"), // Orange for exercise
        Triple(Color(0xFFBA68C8), "Otro", "16%") // Purple for other
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
                                .background(color, CircleShape)
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

@Composable
private fun StepsCard(
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsWalk,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "5010",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "pasos",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "101 BPM",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Hoy superaste tu meta 💛",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Continúa tu bienestar paso a paso",
                fontSize = 8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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

@Composable
private fun InactivityAlertCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💡",
                fontSize = 24.sp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = "Consejo del día",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Intenta realizar pausas activas cada 30 minutos para mantener tu cuerpo en movimiento y mejorar tu productividad.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
