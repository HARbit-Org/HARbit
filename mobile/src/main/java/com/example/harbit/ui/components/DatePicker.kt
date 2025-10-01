@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DatePicker(
    date: String,
    onDateChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    val initialMillis = remember(date) {
        runCatching {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .parse(date)?.time
        }.getOrNull()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : SelectableDates {
            private val today = System.currentTimeMillis()
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= today
            override fun isSelectableYear(year: Int) = year in 1900..Calendar.getInstance().get(Calendar.YEAR)
        }
    )

    OutlinedTextField(
        value = date,
        onValueChange = { },               // read-only; changes come from the picker
        readOnly = true,
        label = { Text("Fecha de nacimiento") },
        placeholder = { Text("DD/MM/AAAA") },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showPicker = true },
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = "Elegir fecha")
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        )
    )

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showPicker = false
                    datePickerState.selectedDateMillis?.let { millis ->
                        val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        onDateChange(fmt.format(Date(millis)))
                    }
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState, title = { Text("Fecha de nacimiento") })
        }
    }
}
