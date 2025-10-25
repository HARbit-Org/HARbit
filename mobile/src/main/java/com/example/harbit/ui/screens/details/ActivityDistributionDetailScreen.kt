package com.example.harbit.ui.screens.details

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.harbit.ui.components.Header
import com.example.harbit.ui.components.MaterialDateRangePicker
import com.example.harbit.ui.components.ActivityDistributionCard
import com.example.harbit.ui.screens.dashboard.ActivityDistributionState
import com.example.harbit.ui.screens.dashboard.ActivityDistributionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Activity pie chart will be embedded here

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDistributionDetailScreen(
    onBackClick: () -> Unit,
    viewModel: ActivityDistributionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    
    var startDateStr by remember { mutableStateOf(today.format(dateFormatter)) }
    var endDateStr by remember { mutableStateOf(today.format(dateFormatter)) }
    
    // Load today's activity distribution on first composition
    LaunchedEffect(Unit) {
        viewModel.loadActivityDistribution(today, today)
    }
    
    // Load activity distribution when date range changes
    LaunchedEffect(startDateStr, endDateStr) {
        if (startDateStr.isNotBlank() && endDateStr.isNotBlank()) {
            try {
                val startDate = LocalDate.parse(startDateStr, dateFormatter)
                val endDate = LocalDate.parse(endDateStr, dateFormatter)
                viewModel.loadActivityDistribution(startDate, endDate)
            } catch (e: Exception) {
                // Invalid date format, ignore
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "",
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 0.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header(
                title = "Mi Distribución de Actividades",
                subtitle = "Conoce el detalle de tus actividades diarias a lo largo del tiempo que HARbit te ha acompañado"
            )

            Spacer(modifier = Modifier.height(18.dp))

            MaterialDateRangePicker(
                startDate = startDateStr,
                endDate = endDateStr,
                onDateRangeChange = { start, end ->
                    startDateStr = start
                    endDateStr = end
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {

            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Activity Chart Card based on state
            when (state) {
                is ActivityDistributionState.Loading -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is ActivityDistributionState.Success -> {
                    val successState = state as ActivityDistributionState.Success
                    ActivityDistributionCard(
                        activities = successState.activities,
                        totalHours = successState.totalHours,
                        onCardClick = { } // No navigation needed in detail screen
                    )
                }
                is ActivityDistributionState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (state as ActivityDistributionState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                is ActivityDistributionState.Empty -> {
                    // Empty state - show gray circle with 0.0 hours
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(250.dp),
                            contentAlignment = Alignment.Center
                        ) {
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
                    }
                }
            }
        }
    }
}
