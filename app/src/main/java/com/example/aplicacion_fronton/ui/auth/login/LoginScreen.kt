package com.example.aplicacion_fronton.ui.auth.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsTennis
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplicacion_fronton.model.dto.UsuarioDto
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle

@Composable
fun LoginScreen(
    onLoginExitoso: (UsuarioDto) -> Unit,
    onIrARegistro: () -> Unit,
    onOlvideContrasena: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    var correo by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(estado) {
        when (val actual = estado) {
            is LoginState.Exito -> onLoginExitoso(actual.usuario)
            is LoginState.NecesitaCompletarPerfilGoogle -> {
                viewModel.reiniciarEstado()
                onIrARegistro()
            }
            else -> Unit
        }
    }

    val cargando = estado is LoginState.Cargando

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 20.dp),
        ) {
            Icon(Icons.Filled.SportsTennis, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(
                "PALETA FRONTÓN",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 2.dp)

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(700, delayMillis = 80)) + slideInVertically(tween(700, delayMillis = 80)) { it / 4 },
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 32.dp)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 32.dp)
                        .size(width = 64.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary),
                )

                Text(
                    "INICIAR SESIÓN",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "Entra a la cancha y sigue tu progreso.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 32.dp),
                )

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

                    Spacer(Modifier.height(16.dp))
                    Text("CONTRASEÑA", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                                Icon(
                                    if (mostrarPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null,
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = camposSinCaja(),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        "¿OLVIDASTE TU CONTRASEÑA?",
                        style = CapsLabelTextStyle,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp)
                            .clickable(enabled = !cargando, onClick = onOlvideContrasena),
                    )

                    AnimatedVisibility(
                        visible = estado is LoginState.Error,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Text(
                            (estado as? LoginState.Error)?.mensaje.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }

                    BotonTactil(
                        texto = "Iniciar sesión",
                        icono = Icons.Filled.Bolt,
                        onClick = { viewModel.iniciarSesion(correo, password) },
                        enabled = !cargando,
                        cargando = cargando,
                        colorContenedor = MaterialTheme.colorScheme.primary,
                        colorPresionado = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 32.dp)) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        "  O ACCEDE CON  ",
                        style = CapsLabelTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                }

                BotonGoogle(
                    cargando = cargando,
                    onClick = { viewModel.continuarConGoogle(contexto) },
                    modifier = Modifier.padding(top = 16.dp),
                )

                Text(
                    "¿No tienes cuenta?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                )
                Text(
                    "Regístrate",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 24.dp)
                        .clickable(enabled = !cargando, onClick = onIrARegistro),
                )
            }
        }
    }
}

@Composable
private fun BotonGoogle(cargando: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val presionado by interaction.collectIsPressedAsState()
    val escala by animateFloatAsState(if (presionado) 0.97f else 1f, label = "escalaGoogle")

    OutlinedButton(
        onClick = onClick,
        enabled = !cargando,
        interactionSource = interaction,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(escala),
    ) {
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
        } else {
            LogoGoogle(modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(
                "Continuar con Google",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/** Aproximación liviana del logo de Google (4 cuadrantes de color) — evita
 * empaquetar un asset de marca solo para un botón, sin perder el reconocimiento
 * visual inmediato de "G multicolor". */
@Composable
private fun LogoGoogle(modifier: Modifier = Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(4.dp))) {
        Column(modifier = Modifier.matchParentSize()) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFF4285F4)))
                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFEA4335)))
            }
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFF34A853)))
                Box(Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFBBC05)))
            }
        }
    }
}

@Composable
internal fun camposSinCaja() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
)
