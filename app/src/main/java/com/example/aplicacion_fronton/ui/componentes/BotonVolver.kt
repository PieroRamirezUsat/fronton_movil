package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale

/**
 * `IconButton` de "volver" (flecha) con feedback táctil — antes era el mismo
 * `IconButton(onClick = onVolver) { Icon(ArrowBack, ...) }` repetido sin
 * ningún feedback en ~15 topBars casi idénticas de la app, mientras el resto
 * de botones ya tenían scale+haptic (`BotonTactil`/`rememberInteraccionPresionable`).
 */
@Composable
fun BotonVolver(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (interaccion, escala) = rememberInteraccionPresionable()
    IconButton(onClick = onClick, interactionSource = interaccion, modifier = modifier.scale(escala)) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.primary)
    }
}

/** Mismo feedback para el `IconButton` de "cerrar" (X) de las pantallas tipo
 * modal (Verificar comprobante, Invitación/Detalle de compromiso). */
@Composable
fun BotonCerrar(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val (interaccion, escala) = rememberInteraccionPresionable()
    IconButton(onClick = onClick, interactionSource = interaccion, modifier = modifier.scale(escala)) {
        Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
