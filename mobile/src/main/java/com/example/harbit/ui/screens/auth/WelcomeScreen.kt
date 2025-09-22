package com.example.harbit.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Material Design 3 theme colors will be used

@Composable
fun WelcomeScreen(
    onGoogleSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        // HARbit Logo and Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(60.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Heart icon with person inside (simplified representation)
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "HARbit Logo",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // App Title
        Text(
            text = "HARbit",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Subtitle
        Text(
            text = "Transforma tus hábitos,\nmejora tu bienestar",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Google Sign In Button
        Button(
            onClick = onGoogleSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Google icon would go here if we had the asset
                Text(
                    text = "🔍 Accede con Google",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}