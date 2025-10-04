package com.example.harbit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AlertCard(
    message: String
) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Black,
        textAlign = TextAlign.Center,
        fontStyle = FontStyle.Italic,
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.tertiaryContainer,
                RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
            .fillMaxWidth()
    )
}