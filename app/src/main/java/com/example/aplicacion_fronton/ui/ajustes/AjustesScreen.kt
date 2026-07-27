package com.example.aplicacion_fronton.ui.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Wc
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplicacion_fronton.model.dto.CategoriaEdad
import com.example.aplicacion_fronton.model.dto.Genero
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.BotonVolver
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle

private val etiquetasCategoriaAjustes = mapOf(
    CategoriaEdad.MENORES to "Menores",
    CategoriaEdad.LIBRE to "Libre",
    CategoriaEdad.MAS_40 to "+40",
    CategoriaEdad.MAS_50 to "+50",
    CategoriaEdad.MAS_60 to "+60",
)

private val etiquetasGeneroAjustes = mapOf(
    Genero.MASCULINO to "Masculino",
    Genero.FEMENINO to "Femenino",
)

@Composable
fun AjustesScreen(
    onVolver: () -> Unit,
    onCuentaEliminada: () -> Unit,
    viewModel: AjustesViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val guardado by viewModel.guardado.collectAsStateWithLifecycle()
    val estadoEliminar by viewModel.eliminarCuenta.collectAsStateWithLifecycle()
    val estadoPassword by viewModel.cambiarPassword.collectAsStateWithLifecycle()
    var confirmandoEliminar by remember { mutableStateOf(false) }

    LaunchedEffect(estadoEliminar) {
        if (estadoEliminar is EliminarCuentaState.Eliminada) onCuentaEliminada()
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
                    Text("AJUSTES", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
            }
        },
    ) { padding ->
        when (val actual = estado) {
            is AjustesState.Cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is AjustesState.Error -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(actual.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            is AjustesState.Exito -> {
                if (confirmandoEliminar) {
                    DialogoConfirmarEliminar(
                        eliminando = estadoEliminar is EliminarCuentaState.Eliminando,
                        error = (estadoEliminar as? EliminarCuentaState.Error)?.mensaje,
                        onConfirmar = { viewModel.eliminarCuenta() },
                        onCancelar = { confirmandoEliminar = false },
                    )
                }

                var club by remember(actual.usuario.club) { mutableStateOf(actual.usuario.club.orEmpty()) }
                var esJugadorLibre by remember(actual.usuario.club) { mutableStateOf(actual.usuario.club == null) }
                var categoria by remember(actual.usuario.categoria_edad) { mutableStateOf(actual.usuario.categoria_edad) }
                var genero by remember(actual.usuario.genero) { mutableStateOf(actual.usuario.genero ?: Genero.MASCULINO) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                ) {
                    Text("CUENTA", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Apartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Jugador libre (sin club)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Switch(
                            checked = esJugadorLibre,
                            onCheckedChange = { esJugadorLibre = it; if (it) club = "" },
                        )
                    }
                    if (!esJugadorLibre) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = club,
                            onValueChange = { club = it },
                            placeholder = { Text("Nombre de tu club") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MilitaryTech, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("Categoría de edad", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        etiquetasCategoriaAjustes.forEach { (valor, etiqueta) ->
                            val activo = categoria == valor
                            Text(
                                etiqueta,
                                style = CapsLabelTextStyle,
                                fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
                                color = if (activo) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .clickable { categoria = valor }
                                    .padding(vertical = 10.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Wc, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("Género", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        etiquetasGeneroAjustes.forEach { (valor, etiqueta) ->
                            val activo = genero == valor
                            Text(
                                etiqueta,
                                style = CapsLabelTextStyle,
                                fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
                                color = if (activo) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .clickable { genero = valor }
                                    .padding(vertical = 10.dp),
                            )
                        }
                    }

                    if (guardado is GuardadoState.Error) {
                        Spacer(Modifier.height(12.dp))
                        Text((guardado as GuardadoState.Error).mensaje, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    if (guardado is GuardadoState.Guardado) {
                        Spacer(Modifier.height(12.dp))
                        Text("Cambios guardados.", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    }

                    BotonTactil(
                        texto = "Guardar cambios",
                        cargando = guardado is GuardadoState.Guardando,
                        onClick = {
                            viewModel.limpiarGuardado()
                            viewModel.guardarPerfil(if (esJugadorLibre) null else club.trim().ifBlank { null }, categoria, genero)
                        },
                        colorContenedor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    )

                    Spacer(Modifier.height(40.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(16.dp))
                    SeccionCambiarPassword(
                        estado = estadoPassword,
                        onCambiar = { actual, nueva -> viewModel.cambiarPassword(actual, nueva) },
                        onLimpiarEstado = { viewModel.limpiarCambiarPassword() },
                    )

                    Spacer(Modifier.height(40.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("ZONA DE RIESGO", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Eliminar tu cuenta es permanente: no podrás volver a iniciar sesión con este correo. Tus partidos pasados se conservan (con tu nombre oculto) para no romper el historial de tus rivales.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = { confirmandoEliminar = true }, modifier = Modifier.padding(top = 8.dp)) {
                        Text("Eliminar cuenta", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionCambiarPassword(
    estado: CambiarPasswordState,
    onCambiar: (actual: String, nueva: String) -> Unit,
    onLimpiarEstado: () -> Unit,
) {
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var mostrarPasswords by remember { mutableStateOf(false) }
    val guardando = estado is CambiarPasswordState.Guardando

    LaunchedEffect(estado) {
        if (estado is CambiarPasswordState.Guardado) {
            actual = ""
            nueva = ""
            confirmar = ""
        }
    }

    Text("CONTRASEÑA", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(12.dp))

    val visualTransformation = if (mostrarPasswords) VisualTransformation.None else PasswordVisualTransformation()
    val trailingIcon: @Composable () -> Unit = {
        IconButton(onClick = { mostrarPasswords = !mostrarPasswords }) {
            Icon(if (mostrarPasswords) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
        }
    }

    OutlinedTextField(
        value = actual,
        onValueChange = { actual = it; onLimpiarEstado() },
        label = { Text("Contraseña actual") },
        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        trailingIcon = trailingIcon,
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = nueva,
        onValueChange = { nueva = it; onLimpiarEstado() },
        label = { Text("Nueva contraseña") },
        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        trailingIcon = trailingIcon,
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    OutlinedTextField(
        value = confirmar,
        onValueChange = { confirmar = it; onLimpiarEstado() },
        label = { Text("Confirmar nueva contraseña") },
        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        trailingIcon = trailingIcon,
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
    )

    val noCoinciden = confirmar.isNotEmpty() && nueva != confirmar
    if (noCoinciden) {
        Spacer(Modifier.height(8.dp))
        Text("Las contraseñas nuevas no coinciden.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
    if (estado is CambiarPasswordState.Error) {
        Spacer(Modifier.height(8.dp))
        Text(estado.mensaje, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
    if (estado is CambiarPasswordState.Guardado) {
        Spacer(Modifier.height(8.dp))
        Text("Contraseña actualizada.", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
    }

    BotonTactil(
        texto = "Cambiar contraseña",
        cargando = guardando,
        enabled = !guardando && actual.isNotEmpty() && nueva.length >= 8 && nueva == confirmar,
        onClick = { onCambiar(actual, nueva) },
        colorContenedor = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
    )
}

@Composable
private fun DialogoConfirmarEliminar(
    eliminando: Boolean,
    error: String?,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!eliminando) onCancelar() },
        icon = { Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("¿Eliminar tu cuenta?") },
        text = {
            Column {
                Text("Esta acción no se puede deshacer. Perderás acceso a tu perfil, ranking y retos.")
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmar, enabled = !eliminando) {
                Text(if (eliminando) "Eliminando…" else "Sí, eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, enabled = !eliminando) { Text("Cancelar") }
        },
    )
}
