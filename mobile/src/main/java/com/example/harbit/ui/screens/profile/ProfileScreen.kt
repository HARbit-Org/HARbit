package com.example.harbit.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.harbit.ui.components.Header
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogoutSuccess: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val updateState by viewModel.updateState.collectAsState()
    val logoutState by viewModel.logoutState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Collect form state from ViewModel
    val userName by viewModel.userName.collectAsState()
    val email by viewModel.email.collectAsState()
    val phone by viewModel.phone.collectAsState()
    val height by viewModel.height.collectAsState()
    val weight by viewModel.weight.collectAsState()
    val pictureUrl by viewModel.pictureUrl.collectAsState()

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Validation state
    var isUserNameValid by remember { mutableStateOf(true) }
    var isEmailValid by remember { mutableStateOf(true) }
    var isPhoneValid by remember { mutableStateOf(true) }
    var isWeightValid by remember { mutableStateOf(true) }
    var isHeightValid by remember { mutableStateOf(true) }

    var userNameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var weightError by remember { mutableStateOf("") }
    var heightError by remember { mutableStateOf("") }
    
    var hasTriedSubmit by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    // Handle update state
    LaunchedEffect(updateState) {
        when (updateState) {
            is ProfileUpdateState.Success -> {
                // Show success message
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Perfil actualizado correctamente",
                        duration = SnackbarDuration.Short
                    )
                }
                viewModel.resetUpdateState()
            }
            is ProfileUpdateState.Error -> {
                // Show error message
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = (updateState as ProfileUpdateState.Error).message,
                        duration = SnackbarDuration.Long
                    )
                }
                viewModel.resetUpdateState()
            }
            else -> { /* No action needed */ }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadUserData()
    }
    
    // Handle logout state
    LaunchedEffect(logoutState) {
        when (logoutState) {
            is LogoutState.Success -> {
                showLogoutDialog = false
                viewModel.resetLogoutState()
                onLogoutSuccess()
            }
            else -> {}
        }
    }
    
    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (logoutState !is LogoutState.Loading) {
                    showLogoutDialog = false
                    viewModel.resetLogoutState()
                }
            },
            title = { Text("Cerrar sesión") },
            text = { 
                if (logoutState is LogoutState.Error) {
                    Text((logoutState as LogoutState.Error).message)
                } else {
                    Text("¿Estás seguro de que deseas cerrar sesión?")
                }
            },
            confirmButton = {
                if (logoutState !is LogoutState.Error) {
                    TextButton(
                        onClick = {
                            viewModel.logout()
                        },
                        enabled = logoutState !is LogoutState.Loading
                    ) {
                        if (logoutState is LogoutState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Sí, cerrar sesión")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showLogoutDialog = false
                        viewModel.resetLogoutState()
                    },
                    enabled = logoutState !is LogoutState.Loading
                ) {
                    Text(if (logoutState is LogoutState.Error) "Cerrar" else "Cancelar")
                }
            }
        )
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
//                .padding(paddingValues)
                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 0.dp)
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Header(
            title = "Mi Perfil",
            subtitle = "¡Queremos conocerte!"
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Profile picture
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (pictureUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pictureUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                // Fallback icon if no picture
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Foto de perfil",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }
        }
        
        // Profile Form
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = {
                viewModel.updateUserName(it)
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

        OutlinedTextField(
            value = email,
            onValueChange = {
                viewModel.updateEmail(it)
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

        OutlinedTextField(
            value = phone,
            onValueChange = {
                viewModel.updatePhone(it)
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = {
                        viewModel.updateWeight(it)
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
                        viewModel.updateHeight(it)
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

        Spacer(modifier = Modifier.height(16.dp))

        // Save Button
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
                validateWeight(weight) { isValid, error ->
                    isWeightValid = isValid
                    weightError = error
                }
                validateHeight(height) { isValid, error ->
                    isHeightValid = isValid
                    heightError = error
                }

                // If all valid, check for changes and update profile
                if (isUserNameValid && isEmailValid && isPhoneValid && isWeightValid && isHeightValid) {
                    if (!viewModel.hasUserDataChanged()) {
                        // No changes detected, show snackbar
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "No hay cambios para guardar",
                                duration = SnackbarDuration.Short
                            )
                        }
                    } else {
                        // Changes detected, proceed with update
                        viewModel.updateProfile()
                    }
                }
            },
            modifier = Modifier
                .height(48.dp),
            enabled = updateState !is ProfileUpdateState.Loading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            if (updateState is ProfileUpdateState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = "Guardar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "Cerrar sesión",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(18.dp)) // Space for bottom navigation
        }
    }
}

// Validation helper functions
private fun isEmailValid(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun isPhoneValid(phone: String): Boolean {
    // Accepts phone numbers starting with +, followed by 10 to 15 digits (international format)
    val regex = Regex("^\\+[1-9]\\d{9,14}\$")
    return regex.matches(phone)
}

private fun validateEmail(email: String, callback: (Boolean, String) -> Unit) {
    when {
        email.isBlank() -> callback(false, "El correo electrónico es obligatorio")
        !isEmailValid(email) -> callback(false, "Ingresa un correo electrónico válido")
        else -> callback(true, "")
    }
}

private fun validatePhone(phone: String, callback: (Boolean, String) -> Unit) {
    when {
        phone.isBlank() -> callback(false, "El número de teléfono es obligatorio")
        !isPhoneValid(phone) -> callback(false, "Ingresa un número de teléfono válido en formato internacional (ejemplo: +521234567890)")
        else -> callback(true, "")
    }
}

private fun validateWeight(weight: String, callback: (Boolean, String) -> Unit) {
    when {
        weight.isBlank() -> callback(false, "El peso es obligatorio")
        weight.toFloatOrNull() == null -> callback(false, "Ingresa un peso válido")
        weight.toFloat() <= 0 -> callback(false, "El peso debe ser mayor a 0")
        weight.toFloat() > 300 -> callback(false, "Ingresa un peso realista")
        else -> callback(true, "")
    }
}

private fun validateHeight(height: String, callback: (Boolean, String) -> Unit) {
    when {
        height.isBlank() -> callback(false, "La altura es obligatoria")
        height.toFloatOrNull() == null -> callback(false, "Ingresa una altura válida")
        height.toFloat() <= 0 -> callback(false, "La altura debe ser mayor a 0")
        height.toFloat() < 50 -> callback(false, "Ingresa una altura realista")
        height.toFloat() > 300 -> callback(false, "Ingresa una altura realista")
        else -> callback(true, "")
    }
}

fun validateUserName(userName: String, callback: (Boolean, String) -> Unit) {
    when {
        userName.isBlank() -> callback(false, "El nombre de usuario es obligatorio")
        userName.length < 3 -> callback(false, "El nombre debe tener al menos 3 caracteres")
        else -> callback(true, "")
    }
}
