package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * Resplandor radial detrás de un elemento central — el mismo efecto que ya
 * existía a mano en `SplashScreen.kt` (detrás del logo) y `DueloRetoScreen.kt`
 * (alrededor de los avatares), extraído acá para los momentos de logro que
 * hasta ahora se quedaban con fondo plano (Reportar Marcador confirmado,
 * éxito de Registro, la dona del Reporte de Apuestas).
 */
@Composable
fun ResplandorRadial(color: Color, tamaño: Dp, modifier: Modifier = Modifier, alpha: Float = 0.35f) {
    Box(
        modifier = modifier
            .size(tamaño)
            .background(
                Brush.radialGradient(colors = listOf(color.copy(alpha = alpha), Color.Transparent)),
                CircleShape,
            ),
    )
}
