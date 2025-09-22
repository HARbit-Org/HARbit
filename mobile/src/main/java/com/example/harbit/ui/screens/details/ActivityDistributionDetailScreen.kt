package com.example.harbit.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Activity pie chart will be embedded here

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDistributionDetailScreen(
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
                    text = "Mi Distribución de\nActividades",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 20.sp
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
                text = "Conoce cómo distribuite tus actividades diarias a lo largo del tiempo que HARbit te ha acompañado",
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
            
            // Activity Chart Card
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        // Simplified pie chart placeholder
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    androidx.compose.foundation.shape.CircleShape
                                )
                        )
                        
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
                    
                    // Detailed Activity Legend
                    DetailedActivityLegend()
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Activity Summary Cards
            ActivitySummarySection()
        }
    }
}

@Composable
private fun DetailedActivityLegend() {
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
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivitySummarySection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ActivitySummaryItem(
            icon = Icons.Default.EventSeat,
            title = "Sedentario",
            percentage = "31%",
            duration = "Durante 4h 20 min",
            color = Color(0xFFE57373)
        )
        
        ActivitySummaryItem(
            icon = Icons.Default.DirectionsWalk,
            title = "Caminar",
            percentage = "16%",
            duration = "Durante 2h 56 min",
            color = Color(0xFF81C784)
        )
        
        ActivitySummaryItem(
            icon = Icons.Default.Accessibility,
            title = "Comidas",
            percentage = "16%",
            duration = "Durante 2h 14 min",
            color = Color(0xFF64B5F6)
        )
    }
}

@Composable
private fun ActivitySummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    percentage: String,
    duration: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = duration,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = percentage,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
