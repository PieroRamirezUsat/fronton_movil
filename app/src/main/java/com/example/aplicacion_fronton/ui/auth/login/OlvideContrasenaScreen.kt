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
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.BotonVolver
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle

/** Paso 1 de "olvidé mi contraseña": pide el correo y dispara el envío del
 * código de 6 dígitos — sin link/deep-link, la app no tiene web funcional
 * detrás, así que el segundo paso se completa acá mismo. */
@Composable
fun OlvideContrasenaScreen(
    onVolver: () -> Unit,
    onCodigoEnviado: (correo: String) -> Unit,
    viewModel: OlvideContrasenaViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var correo by remember { mutableStateOf("") }
    val cargando = estado is OlvideContrasenaState.Cargando

    LaunchedEffect(estado) {
        if (estado is OlvideContrasenaState.Enviado) onCodigoEnviado(correo.trim())
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
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    BotonVolver(onClick = onVolver)
                    Text("RECUPERAR CONTRASEÑA", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
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
                "Ingresa el correo con el que te registraste — te vamos a mandar un código para crear una contraseña nueva.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(8.dp))
                    .padding(16.dp),
            ) {
                Text("CORREO ELECTRÓNICO", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextField(
                    value = correo,
                    onValueChange = { correo = it },
                    placeholder = { Text("Ej: correo@ejemplo.com") },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = camposSinCaja(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            AnimatedVisibility(
                visible = estado is OlvideContrasenaState.Error,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Text(
                    (estado as? OlvideContrasenaState.Error)?.mensaje.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
            }

            BotonTactil(
                texto = "Enviar código",
                icono = Icons.Filled.MailOutline,
                onClick = { viewModel.solicitar(correo) },
                enabled = !cargando,
                cargando = cargando,
                colorContenedor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            )
        }
    }
}
