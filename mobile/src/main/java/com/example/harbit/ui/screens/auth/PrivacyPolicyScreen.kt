package com.example.harbit.ui.screens.auth

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.harbit.ui.components.Header


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "",
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 0.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Header(
                title = "Política de Privacidad\ny Protección de Datos",
                subtitle = ""
            )

            Text(
                text = "Al aceptar esta política, usted autoriza el tratamiento de sus datos personales de acuerdo con las siguientes condiciones:",
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                PolicySection(
                    number = "1.",
                    title = "Finalidad del tratamiento",
                    content = "Sus datos serán utilizados exclusivamente para el funcionamiento de la aplicación HARbit, incluyendo el monitoreo de sus signos vitales, el análisis de sus hábitos y la generación de reportes personalizados sobre su bienestar físico."
                )
                
                PolicySection(
                    number = "2.",
                    title = "Confidencialidad y seguridad",
                    content = "Toda la información recopilada será tratada de manera confidencial y almacenada con medidas de seguridad técnicas y organizativas adecuadas para proteger los datos contra el acceso no autorizado, pérdidas o alteraciones."
                )
                
                PolicySection(
                    number = "3.",
                    title = "No cesión a terceros",
                    content = "Sus datos no serán compartidos con terceros sin su consentimiento expreso, salvo obligación legal o requerimientos judiciales, y únicamente para los fines expresamente autorizados."
                )
                
                PolicySection(
                    number = "4.",
                    title = "Derechos del usuario",
                    content = "Usted podrá solicitar en cualquier momento el acceso, rectificación, cancelación y oposición del tratamiento de sus datos personales, así como retirar su consentimiento para su uso."
                )
                
                PolicySection(
                    number = "5.",
                    title = "Contacto",
                    content = "Para ejercer sus derechos o realizar consultas sobre esta política, puede contactarnos al correo: contacto@harbit.com."
                )
            }
        }
    }
}

@Composable
private fun PolicySection(
    number: String,
    title: String,
    content: String
) {
    Column(
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Row {
            Text(
                text = number,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.width(24.dp)
            )
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = content,
            fontSize = 12.sp,
            color = Color.Black,
            lineHeight = 16.sp,
            modifier = Modifier.padding(start = 24.dp)
        )
    }
}
