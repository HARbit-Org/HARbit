package com.example.harbit.ui.screens.details

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeartRateDetailScreen(
    onBackClick: () -> Unit
) {
    var selectedDate by remember { mutableStateOf("17/08/2025") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Mi Ritmo Cardíaco",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Subtitle
            Text(
                text = "Conoce cómo ha sido tu ritmo cardíaco en una fecha específica 💙",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Date Picker Card
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    Text(
                        text = "Fecha:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedDate,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { /* Open date picker */ }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Seleccionar fecha",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Heart Rate Chart Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Y-axis labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Column {
                            Text("150", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("95", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("56", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("24", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("0", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Heart Rate Chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            HeartRateChart()
                        }
                    }
                    
                    // X-axis labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("00:00", "06:00", "12:00", "18:00", "00:00").forEach { time ->
                            Text(
                                text = time,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Heart Rate Summary
            HeartRateSummary()
        }
    }
}

@Composable
private fun HeartRateChart() {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val path = Path()
        
        // Sample heart rate data points (normalized between 0 and 1)
        val heartRatePoints = listOf(
            0.1f to 0.6f, 0.2f to 0.4f, 0.3f to 0.7f, 0.4f to 0.5f,
            0.5f to 0.8f, 0.6f to 0.3f, 0.7f to 0.6f, 0.8f to 0.4f, 0.9f to 0.5f
        )
        
        heartRatePoints.forEachIndexed { index, (x, y) ->
            val xPos = x * size.width
            val yPos = (1 - y) * size.height
            
            if (index == 0) {
                path.moveTo(xPos, yPos)
            } else {
                path.lineTo(xPos, yPos)
            }
        }
        
        drawPath(
            path = path,
            color = Color.Red, // Heart rate should be red for semantic meaning
            style = Stroke(width = 3.dp.toPx())
        )
        
        // Draw grid lines
        for (i in 0..4) {
            val y = (i / 4f) * size.height
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = androidx.compose.ui.geometry.Offset(0f, y),
                end = androidx.compose.ui.geometry.Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}

@Composable
private fun HeartRateSummary() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Resumen",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HeartRateMetric(
                    label = "Máximo",
                    value = "101 BPM",
                    time = "11:58 AM",
                    color = MaterialTheme.colorScheme.error
                )
                
                HeartRateMetric(
                    label = "Mínimo",
                    value = "56 BPM",
                    time = "03:14 AM",
                    color = MaterialTheme.colorScheme.primary
                )
                
                HeartRateMetric(
                    label = "Promedio",
                    value = "78 BPM",
                    time = "",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun HeartRateMetric(
    label: String,
    value: String,
    time: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        if (time.isNotEmpty()) {
            Text(
                text = time,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
