package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Botón "tactile" del sistema de diseño Stitch: una franja inferior sólida (más
 * oscura que el color del botón) que se cubre por completo al presionar, dando
 * la sensación de que la superficie se hunde en su propia sombra — no es un
 * simple scale, es el patrón real de los mockups (`box-shadow: inset` +
 * `translateY`).
 *
 * La altura TOTAL del componente (`altura` + `alturaSombra`) es siempre fija,
 * independiente del estado de presión — necesario para poder alinear este
 * botón junto a otro (ej. `OutlinedButton`) sin que cambien de tamaño entre sí.
 *
 * `saltoElastico` agrega el "levantamiento" con overshoot elástico que piden
 * los mockups para los CTA principales (enviar reto, guardar compromiso,
 * confirmar) — no es el comportamiento por defecto porque en botones chicos o
 * repetidos (filas de lista) se sentiría exagerado, solo se activa a propósito
 * en los CTA de mayor peso de cada pantalla.
 */
@Composable
fun BotonTactil(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
    enabled: Boolean = true,
    cargando: Boolean = false,
    colorContenedor: Color = MaterialTheme.colorScheme.tertiary,
    colorPresionado: Color? = null,
    colorTexto: Color = Color.White,
    forma: Shape = RoundedCornerShape(12.dp),
    altura: Dp = 52.dp,
    alturaSombra: Dp = 4.dp,
    saltoElastico: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val presionado by interaction.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(presionado) {
        if (presionado) haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
    }
    val huecoActual by animateDpAsState(
        targetValue = if (presionado) alturaSombra else 0.dp,
        animationSpec = tween(100),
        label = "huecoTactil",
    )
    val colorBase = if (enabled) colorContenedor else colorContenedor.copy(alpha = 0.5f)
    val colorActivo by animateColorAsState(
        targetValue = if (presionado && colorPresionado != null) colorPresionado else colorBase,
        animationSpec = tween(180),
        label = "colorTactil",
    )
    val colorSombra = remember(colorActivo) { colorActivo.oscurecer() }
    val elevacion by animateDpAsState(
        targetValue = if (saltoElastico && presionado) (-4).dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "saltoElasticoBoton",
    )

    Box(
        modifier = modifier
            .offset(y = elevacion)
            .height(altura + alturaSombra)
            .clip(forma)
            .background(colorSombra),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(altura + huecoActual)
                .clip(forma)
                .background(colorActivo)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled && !cargando,
                    onClick = onClick,
                ),
        ) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.width(20.dp), strokeWidth = 2.dp, color = colorTexto)
            } else {
                Text(texto.uppercase(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colorTexto)
                if (icono != null) {
                    Spacer(Modifier.width(8.dp))
                    Icon(icono, contentDescription = null, tint = colorTexto)
                }
            }
        }
    }
}

private fun Color.oscurecer(factor: Float = 0.32f): Color = Color(
    red = red * (1 - factor),
    green = green * (1 - factor),
    blue = blue * (1 - factor),
    alpha = alpha,
)
