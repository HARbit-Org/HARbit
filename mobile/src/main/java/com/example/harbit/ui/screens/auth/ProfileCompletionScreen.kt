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
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf("Masculino") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var privacyAccepted by remember { mutableStateOf(false) }

    var isUserNameValid by remember { mutableStateOf(true) }
    var isEmailValid by remember { mutableStateOf(true) }
    var isPhoneValid by remember { mutableStateOf(true) }
    var isBirthDateValid by remember { mutableStateOf(true) }
    var isWeightValid by remember { mutableStateOf(true) }
    var isHeightValid by remember { mutableStateOf(true) }
    
    var userNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var birthDateError by remember { mutableStateOf("") }
    var weightError by remember { mutableStateOf("") }
    var heightError by remember { mutableStateOf("") }
    
    var hasTriedSubmit by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with logo
        Image(
            painter = painterResource(id = com.example.harbit.R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier
                .width(80.dp)
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
            onValueChange = { 
                userName = it
                if (hasTriedSubmit) {
                    validateUserName(it) { isValid, error ->
                        isUserNameValid = isValid
                        userNameError = error
                    }
                }
            },
            label = { Text("Nombre de usuario") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isUserNameValid,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = userNameError,
            color = if (!isUserNameValid && userNameError.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(start = 16.dp, top = 4.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))
        
        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                if (hasTriedSubmit) {
                    validateEmail(it) { isValid, error ->
                        isEmailValid = isValid
                        emailError = error
                    }
                }
            },
            label = { Text("Correo electrónico preferido") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isEmailValid,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = emailError,
            color = if (!isEmailValid && emailError.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(start = 16.dp, top = 4.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))
        
        // Phone Field
        OutlinedTextField(
            value = phone,
            onValueChange = { 
                phone = it
                if (hasTriedSubmit) {
                    validatePhone(it) { isValid, error ->
                        isPhoneValid = isValid
                        phoneError = error
                    }
                }
            },
            label = { Text("Número de teléfono") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = !isPhoneValid,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            text = phoneError,
            color = if (!isPhoneValid && phoneError.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(start = 16.dp, top = 4.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))
        
        // Birth Date Field
        MaterialDatePicker(
            date = birthDate,
            onDateChange = { 
                birthDate = it
                if (hasTriedSubmit) {
                    validateBirthDate(it) { isValid, error ->
                        isBirthDateValid = isValid
                        birthDateError = error
                    }
                }
            }
        )
        Text(
            text = birthDateError,
            color = if (!isBirthDateValid && birthDateError.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(start = 16.dp, top = 4.dp)
                .fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(4.dp))

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
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { 
                        weight = it
                        if (hasTriedSubmit) {
                            validateWeight(it) { isValid, error ->
                                isWeightValid = isValid
                                weightError = error
                            }
                        }
                    },
                    label = { Text("Peso (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !isWeightValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = weightError,
                    color = if (!isWeightValid && weightError.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 4.dp)
                        .fillMaxWidth()
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = height,
                    onValueChange = { 
                        height = it
                        if (hasTriedSubmit) {
                            validateHeight(it) { isValid, error ->
                                isHeightValid = isValid
                                heightError = error
                            }
                        }
                    },
                    label = { Text("Altura (cm)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !isHeightValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = heightError,
                    color = if (!isHeightValid && heightError.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surface,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 4.dp)
                        .fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Complete Profile Button
        Button(
            onClick = {
                hasTriedSubmit = true
                
                // Validate all fields
                validateUserName(userName) { isValid, error ->
                    isUserNameValid = isValid
                    userNameError = error
                }
                validateEmail(email) { isValid, error ->
                    isEmailValid = isValid
                    emailError = error
                }
                validatePhone(phone) { isValid, error ->
                    isPhoneValid = isValid
                    phoneError = error
                }
                validateBirthDate(birthDate) { isValid, error ->
                    isBirthDateValid = isValid
                    birthDateError = error
                }
                validateWeight(weight) { isValid, error ->
                    isWeightValid = isValid
                    weightError = error
                }
                validateHeight(height) { isValid, error ->
                    isHeightValid = isValid
                    heightError = error
                }
                
                // Only proceed if all validations pass
                if (isUserNameValid && isEmailValid && isPhoneValid && 
                    isBirthDateValid && isWeightValid && isHeightValid && privacyAccepted) {
                    onProfileComplete()
                }
            },
            modifier = Modifier
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp),
            enabled = privacyAccepted
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

fun isEmailValid(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun isPhoneValid(phone: String): Boolean {
    // Accepts phone numbers starting with +, followed by 10 to 15 digits (international format)
    val regex = Regex("^\\+[1-9]\\d{9,14}\$")
    return regex.matches(phone)
}

// Validation functions with Spanish error messages
fun validateUserName(userName: String, callback: (Boolean, String) -> Unit) {
    when {
        userName.isBlank() -> callback(false, "El nombre de usuario es obligatorio")
        userName.length < 3 -> callback(false, "El nombre debe tener al menos 3 caracteres")
        else -> callback(true, "")
    }
}

fun validateEmail(email: String, callback: (Boolean, String) -> Unit) {
    when {
        email.isBlank() -> callback(false, "El correo electrónico es obligatorio")
        !isEmailValid(email) -> callback(false, "Ingresa un correo electrónico válido")
        else -> callback(true, "")
    }
}

fun validatePhone(phone: String, callback: (Boolean, String) -> Unit) {
    when {
        phone.isBlank() -> callback(false, "El número de teléfono es obligatorio")
        !isPhoneValid(phone) -> callback(false, "Ingresa un número de teléfono válido en formato internacional (ejemplo: +521234567890)")
        else -> callback(true, "")
    }
}

fun validateBirthDate(birthDate: String, callback: (Boolean, String) -> Unit) {
    when {
        birthDate.isBlank() -> callback(false, "La fecha de nacimiento es obligatoria")
        else -> callback(true, "")
    }
}

fun validateWeight(weight: String, callback: (Boolean, String) -> Unit) {
    when {
        weight.isBlank() -> callback(false, "El peso es obligatorio")
        weight.toFloatOrNull() == null -> callback(false, "Ingresa un peso válido")
        weight.toFloat() <= 0 -> callback(false, "El peso debe ser mayor a 0")
        weight.toFloat() > 300 -> callback(false, "Ingresa un peso realista")
        else -> callback(true, "")
    }
}

fun validateHeight(height: String, callback: (Boolean, String) -> Unit) {
    when {
        height.isBlank() -> callback(false, "La altura es obligatoria")
        height.toFloatOrNull() == null -> callback(false, "Ingresa una altura válida")
        height.toFloat() <= 0 -> callback(false, "La altura debe ser mayor a 0")
        height.toFloat() < 50 -> callback(false, "Ingresa una altura realista")
        height.toFloat() > 300 -> callback(false, "Ingresa una altura realista")
        else -> callback(true, "")
    }
}
