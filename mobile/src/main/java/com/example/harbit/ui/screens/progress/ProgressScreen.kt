package com.example.harbit.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ProgressScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        ProgressHeader()
        
        // Achievement Cards
        AchievementSection()
        
        // Improvement Suggestions
        ImprovementSection()
        
        Spacer(modifier = Modifier.height(100.dp)) // Space for bottom navigation
    }
}

@Composable
private fun ProgressHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Mi Progreso",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "HARbit analiza tu actividad y te muestra cómo vas avanzando semana a semana.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun AchievementSection() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Logros",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AchievementCard(
                icon = Icons.Default.TrendingDown,
                title = "¡Lograste reducir tu sedentarismo!",
                description = "Redujiste 15% tu tiempo sentado respecto a la semana pasada.",
                date = "12/08/2025",
                color = MaterialTheme.colorScheme.primary,
                isPositive = true
            )
            
            AchievementCard(
                icon = Icons.Default.DirectionsWalk,
                title = "¡Superaste tu promedio de pasos!",
                description = "Promediaste 11,200 pasos por día esta semana, 32% más que la anterior. Mantener este hábito mejora tu resistencia y energía.",
                date = "05/08/2025",
                color = MaterialTheme.colorScheme.primary,
                isPositive = true
            )
        }
    }
}

@Composable
private fun ImprovementSection() {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Aspectos de mejora",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        AchievementCard(
            icon = Icons.Default.AccessTime,
            title = "Aumenta tu tiempo sentado",
            description = "+18% vs semana pasada",
            date = "04/08/2025",
            color = MaterialTheme.colorScheme.error,
            isPositive = false
        )
    }
}

@Composable
private fun AchievementCard(
    icon: ImageVector,
    title: String,
    description: String,
    date: String,
    color: Color,
    isPositive: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            } else {
                MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                modifier = Modifier.weight(1f)
            ) {
                // Icon with background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 18.sp
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = date,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (isPositive) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Logro completado",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Aspecto a mejorar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
