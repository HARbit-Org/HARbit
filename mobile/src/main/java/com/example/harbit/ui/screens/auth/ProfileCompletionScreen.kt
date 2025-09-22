package com.example.harbit.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Using Material Design 3 theme colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCompletionScreen(
    onProfileComplete: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("john.doe@gmail.com") }
    var phone by remember { mutableStateOf("+51987654321") }
    var birthDate by remember { mutableStateOf("17/08/2002") }
    var selectedGender by remember { mutableStateOf("Masculino") }
    var height by remember { mutableStateOf("72.3") }
    var weight by remember { mutableStateOf("176") }
    var privacyAccepted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(40.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "HARbit Logo",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Completar mi Perfil",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // User Name Field
        OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("Nombre de usuario") },
            placeholder = { Text("John") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico preferido") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = Color.Gray,
                disabledLabelColor = Color.Gray
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Phone Field
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Número de teléfono") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Birth Date Field
        OutlinedTextField(
            value = birthDate,
            onValueChange = { birthDate = it },
            label = { Text("Fecha de nacimiento") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Gender Selection
        Text(
            text = "OCUPACIÓN",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Masculino", "Femenino", "Otro").forEach { gender ->
                FilterChip(
                    onClick = { selectedGender = gender },
                    label = { Text(gender) },
                    selected = selectedGender == gender,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Height and Weight Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Peso (kg)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Altura (cm)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Privacy Policy Checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = privacyAccepted,
                onCheckedChange = { privacyAccepted = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = "He leído y acepto la política de privacidad y protección",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                TextButton(
                    onClick = onPrivacyPolicyClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "de datos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Complete Profile Button
        Button(
            onClick = onProfileComplete,
            enabled = privacyAccepted && userName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "¡Listo!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
