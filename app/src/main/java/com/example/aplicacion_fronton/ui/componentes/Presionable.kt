package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Feedback táctil mínimo (scale-on-press + haptic) para botones M3 estándar
 * (`OutlinedButton`/`TextButton`/`IconButton`) que no pasan por `BotonTactil`
 * — antes esos quedaban con el ripple genérico de Material mientras el resto
 * de la app ya tenía micro-interacciones propias, una inconsistencia real
 * entre CTAs "cuidados" y el resto. Uso: pasar el `MutableInteractionSource`
 * devuelto al parámetro `interactionSource` del botón (todos los M3 lo
 * aceptan), y `Modifier.scale(escala)` al propio botón.
 */
@Composable
fun rememberInteraccionPresionable(): Pair<MutableInteractionSource, Float> {
    val interaction = remember { MutableInteractionSource() }
    val presionado by interaction.collectIsPressedAsState()
    val escala by animateFloatAsState(targetValue = if (presionado) 0.94f else 1f, label = "escalaPresionable")
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(presionado) {
        if (presionado) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
    }
    return interaction to escala
}
