package com.example.aplicacion_fronton.ui.auth.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.BotonVolver
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle

/** Paso 2 de "olvidé mi contraseña": código de 6 dígitos + nueva contraseña. */
@Composable
fun ConfirmarResetScreen(
    correo: String,
    onVolver: () -> Unit,
    onListo: () -> Unit,
    viewModel: ConfirmarResetViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var codigo by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }
    val cargando = estado is ConfirmarResetState.Cargando

    LaunchedEffect(estado) {
        if (estado is ConfirmarResetState.Exito) onListo()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    BotonVolver(onClick = onVolver)
                    Text("INGRESA EL CÓDIGO", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Text(
                "Si $correo está registrado, le mandamos un código de 6 dígitos. Vence en 15 minutos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            val visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp))
                    .padding(16.dp),
            ) {
                Text("CÓDIGO DE 6 DÍGITOS", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextField(
                    value = codigo,
                    onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) codigo = it },
                    placeholder = { Text("123456") },
                    leadingIcon = { Icon(Icons.Filled.Password, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = camposSinCaja(),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(16.dp))
                Text("NUEVA CONTRASEÑA", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextField(
                    value = nueva,
                    onValueChange = { nueva = it },
                    placeholder = { Text("••••••••") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                            Icon(if (mostrarPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
                        }
                    },
                    singleLine = true,
                    visualTransformation = visualTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = camposSinCaja(),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(16.dp))
                Text("CONFIRMAR CONTRASEÑA", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextField(
                    value = confirmar,
                    onValueChange = { confirmar = it },
                    placeholder = { Text("••••••••") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    singleLine = true,
                    visualTransformation = visualTransformation,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = camposSinCaja(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            val noCoinciden = confirmar.isNotEmpty() && nueva != confirmar
            if (noCoinciden) {
                Text(
                    "Las contraseñas no coinciden.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            AnimatedVisibility(
                visible = estado is ConfirmarResetState.Error,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Text(
                    (estado as? ConfirmarResetState.Error)?.mensaje.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
            }

            BotonTactil(
                texto = "Cambiar contraseña",
                icono = Icons.Filled.CheckCircle,
                onClick = { viewModel.confirmar(correo, codigo, nueva) },
                enabled = !cargando && codigo.length == 6 && nueva.length >= 8 && nueva == confirmar,
                cargando = cargando,
                colorContenedor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            )
        }
    }
}
