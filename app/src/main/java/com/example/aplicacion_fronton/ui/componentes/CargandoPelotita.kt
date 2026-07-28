package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.StartOffsetType
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reemplazo "de caricatura" del `CircularProgressIndicator()` plano de
 * Material — 3 pelotitas que rebotan en cascada (mismo espíritu que el
 * squash/rebote del Splash, pero en loop continuo para estados de carga
 * indeterminados). Drop-in: mismo uso que un spinner, `Box(contentAlignment
 * = Alignment.Center) { CargandoPelotita() }`.
 */
@Composable
fun CargandoPelotita(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    tamano: Dp = 12.dp,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Pelotita(color, tamano, retrasoMs = 0)
        Spacer(Modifier.width(tamano / 2))
        Pelotita(color, tamano, retrasoMs = 120)
        Spacer(Modifier.width(tamano / 2))
        Pelotita(color, tamano, retrasoMs = 240)
    }
}

@Composable
private fun Pelotita(color: Color, tamano: Dp, retrasoMs: Int) {
    val transicion = rememberInfiniteTransition(label = "pelotita")
    val alturaRebote by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(retrasoMs, StartOffsetType.Delay),
        ),
        label = "alturaRebote",
    )
    Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.height(tamano * 2)) {
        Box(
            modifier = Modifier
                .size(tamano)
                .clip(CircleShape)
                .background(color)
                .offset(y = -(tamano * 1.4f * alturaRebote)),
        )
    }
}
