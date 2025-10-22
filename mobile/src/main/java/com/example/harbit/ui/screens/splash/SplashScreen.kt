package com.example.harbit.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.harbit.R

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToWelcome: () -> Unit,
    onNavigateToProfileCompletion: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val destination by viewModel.destination.collectAsState()

    // Navigate when destination is determined
    LaunchedEffect(destination) {
        when (destination) {
            is SplashDestination.Welcome -> onNavigateToWelcome()
            is SplashDestination.ProfileCompletion -> onNavigateToProfileCompletion()
            is SplashDestination.Main -> onNavigateToMain()
            null -> { /* Still loading */ }
        }
    }

    // Show splash UI while checking auth status
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "HARbit Logo",
                modifier = Modifier.width(120.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
