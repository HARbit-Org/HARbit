package com.example.harbit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.*

/**
 * Material3 DateRangePicker with custom theming using @OptIn.
 * This component uses your Material Design 3 color palette automatically.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDateRangePicker(
    startDate: String,
    endDate: String,
    onDateRangeChange: (startDate: String, endDate: String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Rango de fechas",
    placeholder: String = "DD/MM/AAAA - DD/MM/AAAA"
) {
    val focus = LocalFocusManager.current
    val interaction = remember { MutableInteractionSource() }
    var showDialog by remember { mutableStateOf(false) }

    // Parse current dates or use null
    val initialStartDateMillis = remember(startDate) {
        if (startDate.isNotBlank()) {
            runCatching {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                sdf.parse(startDate)?.time
            }.getOrNull()
        } else {
            null
        }
    }

    val initialEndDateMillis = remember(endDate) {
        if (endDate.isNotBlank()) {
            runCatching {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                sdf.parse(endDate)?.time
            }.getOrNull()
        } else {
            null
        }
    }

    // Date range picker state
    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartDateMillis,
        initialSelectedEndDateMillis = initialEndDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Only allow dates up to today
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    // Format the display value
    val displayValue = remember(startDate, endDate) {
        when {
            startDate.isNotBlank() && endDate.isNotBlank() -> "$startDate - $endDate"
            startDate.isNotBlank() -> "$startDate - ..."
            else -> ""
        }
    }

    // TextField (read-only) that opens the Material3 picker
    OutlinedTextField(
        value = displayValue,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interaction,
                indication = null
            ) {
                focus.clearFocus()
                showDialog = true
            },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        ),
        trailingIcon = {
            IconButton(onClick = {
                focus.clearFocus()
                showDialog = true
            }) {
                Icon(
                    Icons.Outlined.CalendarMonth,
                    contentDescription = "Elegir rango de fechas",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )

    // Custom Dialog with DateRangePicker following Material Design pattern
    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.6f),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
                    // Top row with Close button and Save button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { showDialog = false }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        TextButton(
                            onClick = {
                                val startMillis = dateRangePickerState.selectedStartDateMillis
                                val endMillis = dateRangePickerState.selectedEndDateMillis
                                
                                if (startMillis != null && endMillis != null) {
                                    // Format start date with UTC timezone
                                    val startCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                        timeInMillis = startMillis
                                    }
                                    val formattedStart = String.format(
                                        Locale.getDefault(),
                                        "%02d/%02d/%04d",
                                        startCalendar.get(Calendar.DAY_OF_MONTH),
                                        startCalendar.get(Calendar.MONTH) + 1,
                                        startCalendar.get(Calendar.YEAR)
                                    )
                                    
                                    // Format end date with UTC timezone
                                    val endCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                        timeInMillis = endMillis
                                    }
                                    val formattedEnd = String.format(
                                        Locale.getDefault(),
                                        "%02d/%02d/%04d",
                                        endCalendar.get(Calendar.DAY_OF_MONTH),
                                        endCalendar.get(Calendar.MONTH) + 1,
                                        endCalendar.get(Calendar.YEAR)
                                    )
                                    
                                    onDateRangeChange(formattedStart, formattedEnd)
                                    showDialog = false
                                }
                            },
                            enabled = dateRangePickerState.selectedStartDateMillis != null && 
                                      dateRangePickerState.selectedEndDateMillis != null
                        ) {
                            Text(text = "Guardar")
                        }
                    }
                    
                    // DateRangePicker takes remaining space
                    DateRangePicker(
                        state = dateRangePickerState,
                        modifier = Modifier.weight(1f),
                        colors = DatePickerDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }
    }
}
