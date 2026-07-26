package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.random.Random

private val ColoresConfettiPorDefecto = listOf(Color(0xFFFEC564), Color(0xFFAB472B), Color(0xFFA9F0E4), Color.White)

private data class ParticulaConfetti(val color: Color, val dx: Float, val dy: Float, val duracionMs: Int)

/**
 * Estallido de confeti radial — antes vivía privado y duplicado en
 * `ReportarMarcadorScreen`; se extrae acá para reusarlo también en el momento
 * de éxito de Registro (y cualquier otro "logro" futuro) sin copiar el código.
 */
@Composable
fun ConfettiOverlay(disparar: Boolean, modifier: Modifier = Modifier, colores: List<Color> = ColoresConfettiPorDefecto, alturaCaja: Dp = 220.dp) {
    if (!disparar) return
    val particulas = remember {
        List(24) {
            ParticulaConfetti(
                color = colores.random(),
                dx = (Random.nextFloat() * 2 - 1) * 150f,
                dy = (Random.nextFloat() * 2 - 1) * 150f,
                duracionMs = 700 + Random.nextInt(500),
            )
        }
    }
    Box(modifier = modifier.fillMaxWidth().height(alturaCaja), contentAlignment = Alignment.Center) {
        particulas.forEach { particula ->
            val progreso = remember(particula) { Animatable(0f) }
            LaunchedEffect(particula) {
                progreso.animateTo(1f, animationSpec = tween(particula.duracionMs, easing = LinearOutSlowInEasing))
            }
            Box(
                modifier = Modifier
                    .offset { IntOffset((particula.dx * progreso.value).roundToInt(), (particula.dy * progreso.value).roundToInt()) }
                    .size(7.dp)
                    .alpha(1f - progreso.value)
                    .background(particula.color, RoundedCornerShape(1.dp)),
            )
        }
    }
}
