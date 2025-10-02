package com.example.harbit.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.harbit.ui.components.ButtonGroup
import com.example.harbit.ui.components.MaterialDatePicker

// Using Material Design 3 theme colors

@Composable
fun ProfileCompletionScreen(
    onProfileComplete: () -> Unit,
    onPrivacyPolicyClick: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("john.doe@gmail.com") }
    var phone by remember { mutableStateOf("+51987654321") }
    var birthDate by remember { mutableStateOf("") }
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
        Image(
            painter = painterResource(id = com.example.harbit.R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier
                .width(128.dp)
        )
        
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
//            enabled = false,
//            colors = OutlinedTextFieldDefaults.colors(
//                disabledBorderColor = Color.Gray,
//                disabledLabelColor = Color.Gray
//            )
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
        MaterialDatePicker(
            date = birthDate,
            onDateChange = { birthDate = it }
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        ButtonGroup(
            options = listOf("Masculino", "Femenino", "Otro"),
            selectedOption = selectedGender,
            onSelected = { selectedGender = it }
        )

        
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
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )

            Spacer(Modifier.width(8.dp))

            val annotated = buildAnnotatedString {
                append("He leído y acepto la ")
                pushStringAnnotation(tag = "policy", annotation = "policy")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.None)) {
                    append("política de privacidad y protección")
                }
                pop()
            }

            ClickableText(
                text = annotated,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                onClick = { offset ->
                    annotated.getStringAnnotations("policy", offset, offset)
                        .firstOrNull()?.let { onPrivacyPolicyClick() }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Complete Profile Button
        Button(
            onClick = onProfileComplete,
            enabled = privacyAccepted && userName.isNotBlank(),
            modifier = Modifier
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "¡Listo!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
