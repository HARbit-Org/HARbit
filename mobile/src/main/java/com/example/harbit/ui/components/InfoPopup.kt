package com.example.harbit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun InfoPopup(
    infoText: AnnotatedString,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Info,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
    title: String = "Información",
    onTextClick: ((Int) -> Unit)? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    
    // Info icon button
    Icon(
        imageVector = icon,
        contentDescription = "Información",
        modifier = modifier
            .size(iconSize)
            .clickable { showDialog = true },
        tint = MaterialTheme.colorScheme.onSurface
    )
    
    // Popup dialog
    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Info text with clickable portions
                    ClickableText(
                        text = infoText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        onClick = { offset ->
                            // Check for URL annotations and open them
                            infoText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                .firstOrNull()?.let { annotation ->
                                    uriHandler.openUri(annotation.item)
                                }
                            // Also call custom click handler if provided
                            onTextClick?.invoke(offset)
                        }
                    )
                    
                    // Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showDialog = false }
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }
    }
}

// Overload for backward compatibility with plain String
@Composable
fun InfoPopup(
    infoText: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.Info,
    iconSize: androidx.compose.ui.unit.Dp = 20.dp,
    title: String = "Información"
) {
    InfoPopup(
        infoText = AnnotatedString(infoText),
        modifier = modifier,
        icon = icon,
        iconSize = iconSize,
        title = title,
        onTextClick = null
    )
}