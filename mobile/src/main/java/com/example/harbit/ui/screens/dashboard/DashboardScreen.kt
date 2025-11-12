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
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.harbit.ui.components.Header
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.icons.outlined.WatchOff
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import com.example.harbit.data.local.dao.ActivityDistribution
import com.example.harbit.domain.Alert
import com.example.harbit.ui.components.ActivityDistributionCard
import com.example.harbit.ui.components.AlertCard
import com.example.harbit.ui.components.InfoPopup
import java.time.LocalDate

import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onActivityDetailClick: () -> Unit,
    onHeartRateClick: () -> Unit,
    onStepsClick: () -> Unit,
    viewModel: ActivityDistributionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isWatchConnected by viewModel.isWatchConnected.collectAsStateWithLifecycle()
    var alerts: List<Alert> = emptyList()
    
    // Load today's activity distribution on first composition AND when returning to screen
    LaunchedEffect(Unit) {
        val today = LocalDate.now()
        viewModel.loadActivityDistribution(today, today)
        // Start listening for sensor data upload events and watch connection monitoring
        viewModel.startListeningForDataUploads()
    }
    
    // Stop listening when leaving screen
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            viewModel.stopListeningForDataUploads()
        }
    }
    
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
            if (isWatchConnected) {
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
        when (state) {
            is ActivityDistributionState.Loading -> {
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(350.dp),
//                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
//                ) {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator()
//                    }
//                }
                alerts = (state as ActivityDistributionState.Loading).alerts
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onActivityDetailClick() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Activity Chart - Empty circle in light gray
                    Box(
                        modifier = Modifier.size(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Draw empty gray circle
                        Canvas(
                            modifier = Modifier.size(160.dp)
                        ) {
                            val strokeWidth = 35.dp.toPx()
                            drawArc(
                                color = Color.LightGray,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )
                        }

                        // Center text
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "0.0 hs",
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Realiza actividad utilizando HARbit para obtener la distribución del día.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            is ActivityDistributionState.Success -> {
                alerts = (state as ActivityDistributionState.Success).alerts
                ActivityDistributionCard(
                    activities = (state as ActivityDistributionState.Success).activities,
                    totalHours = (state as ActivityDistributionState.Success).totalHours,
                    onCardClick = onActivityDetailClick
                )


            }
            is ActivityDistributionState.Error -> {
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(350.dp),
//                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
//                ) {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = (state as ActivityDistributionState.Error).message,
//                            color = MaterialTheme.colorScheme.error,
//                            textAlign = TextAlign.Center,
//                            modifier = Modifier.padding(16.dp)
//                        )
//                    }
//                }
                alerts = (state as ActivityDistributionState.Error).alerts
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onActivityDetailClick() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Activity Chart - Empty circle in light gray
                    Box(
                        modifier = Modifier.size(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Draw empty gray circle
                        Canvas(
                            modifier = Modifier.size(160.dp)
                        ) {
                            val strokeWidth = 35.dp.toPx()
                            drawArc(
                                color = Color.LightGray,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )
                        }

                        // Center text
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "0.0 hs",
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Realiza actividad utilizando HARbit para obtener la distribución del día.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            is ActivityDistributionState.Empty -> {
                alerts = (state as ActivityDistributionState.Empty).alerts
                // Show empty chart with 0.0 hours in gray
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onActivityDetailClick() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Activity Chart - Empty circle in light gray
                    Box(
                        modifier = Modifier.size(250.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Draw empty gray circle
                        Canvas(
                            modifier = Modifier.size(160.dp)
                        ) {
                            val strokeWidth = 35.dp.toPx()
                            drawArc(
                                color = Color.LightGray,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth)
                            )
                        }

                        // Center text
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "0.0 hs",
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Realiza actividad utilizando HARbit para obtener la distribución del día.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            InfoPopup(
                infoText = """
                    • Pausas activas: Se recomiendan las pausas de 5 minutos cada media hora en función de un estudio de Harvard Health, el cual demuestra la reducción de glucosa en sangre y la presión arterial sistólica.
                    
                    • Actividad física semanal: La OMS recomienda por lo menos de 150 a 300 minutos por semana (21 a 42 minutos diarios, aproximadamente) de actividad física de intensidad moderada o vigorosa para todos los adultos, con el objetivo de prevenir y ayudar a manejar las cardiopatías, la diabetes de tipo 2 y el cáncer, así como para reducir los síntomas de la depresión y la ansiedad, disminuir el deterioro cognitivo, mejorar la memoria y potenciar la salud cerebral.
                """.trimIndent(),
                title = "¿En qué se basan las alertas de HARbit?",
                icon = Icons.Default.HelpOutline
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        alerts.forEach { alert ->
            AlertCard(message = alert.message)
            Spacer(modifier = Modifier.height(18.dp))
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
