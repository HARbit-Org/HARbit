package com.example.harbit.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Material3 DatePicker with custom theming using @OptIn.
 * This component uses your Material Design 3 color palette automatically.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialDatePicker(
    date: String,
    onDateChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Fecha de nacimiento",
    placeholder: String = "DD/MM/AAAA"
) {
    val focus = LocalFocusManager.current
    val interaction = remember { MutableInteractionSource() }
    var showDialog by remember { mutableStateOf(false) }

    // Parse current date or use today
    val initialDateMillis = remember(date) {
        if (date.isNotBlank()) {
            runCatching {
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(date)?.time
            }.getOrNull()
        } else {
            null
        }
    }

    // Date picker state with date validator
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Only allow dates up to today (no future dates for birth date)
                return utcTimeMillis <= System.currentTimeMillis()
            }
        }
    )

    // TextField (read-only) that opens the Material3 picker
    OutlinedTextField(
        value = date,
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
                    contentDescription = "Elegir fecha",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    )

    // Material3 DatePickerDialog with custom colors
    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = millis
                            }
                            val formatted = String.format(
                                Locale.getDefault(),
                                "%02d/%02d/%04d",
                                calendar.get(Calendar.DAY_OF_MONTH),
                                calendar.get(Calendar.MONTH) + 1,
                                calendar.get(Calendar.YEAR)
                            )
                            onDateChange(formatted)
                        }
                        showDialog = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}
