package com.example.harbit.ui.screens.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.harbit.data.remote.dto.ProgressInsightDto
import com.example.harbit.ui.components.Header
import com.example.harbit.ui.theme.writeColor
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun ProgressScreen(
    viewModel: ProgressInsightsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadProgressInsights()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 0.dp)
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Header(
            title = "Mi Progreso",
            subtitle = "HARbit analiza tu actividad y te muestra cómo vas avanzando semana a semana"
        )

        Spacer(modifier = Modifier.height(18.dp))

        when (state) {
            is ProgressInsightsState.Loading -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is ProgressInsightsState.Success -> {
                val successState = state as ProgressInsightsState.Success

                successState.insights.forEach { insightDto ->
                    ProgressListItem(
                        insight = insightDto,
                    )
                }
            }
            is ProgressInsightsState.Error -> {
                Text(
                    text = "Todavía no tenemos información sobre tu progreso. ¡Sigue utilizando HARbit para mejorar tu bienestar!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            is ProgressInsightsState.Empty -> {
                Text(
                    text = "Todavía no tenemos información sobre tu progreso. ¡Sigue utilizando HARbit para mejorar tu bienestar!",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // Space for bottom navigation
    }
}

@Composable
private fun ProgressListItem(
    insight: ProgressInsightDto,
) {
    val icon: ImageVector
    val color: Color
    val overlineText: String

    val formattedDate = try {
        val dateTime = LocalDateTime.parse(insight.createdAtStr, DateTimeFormatter.ISO_DATE_TIME)
        dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    } catch (e: Exception) {
        insight.createdAtStr // Fallback al formato original si hay error
    }

    when (insight.type) {
        "progress" -> {
            icon = Icons.Default.FavoriteBorder
            color = MaterialTheme.colorScheme.primary
            overlineText = "Progreso"
        }
        "improvement_opportunity" -> {
            icon = Icons.Default.HeartBroken
            color = MaterialTheme.colorScheme.error
            overlineText = "Aspecto de mejora"
        }
        else -> {
            icon = Icons.Default.Error
            color = writeColor
            overlineText = "Informativo"
        }
    }


    ListItem(
        headlineContent = {
            Text(
                text = insight.title
            )
        },
        supportingContent = {
            Text(
                text = insight.body
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color
            )
        },
        overlineContent = {
            Text(
                text = overlineText,
                color = color,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        },
        trailingContent = {
            Text(
                text = formattedDate
            )
        }
    )
    HorizontalDivider()
}
