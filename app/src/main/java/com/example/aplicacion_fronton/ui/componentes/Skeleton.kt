package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Placeholder "shimmer" con la silueta real del contenido, en vez del
 * `CircularProgressIndicator()` genérico que se repetía en 20 pantallas —
 * antes no había ninguna relación visual entre el estado de carga y lo que
 * aparece después.
 */
fun Modifier.shimmer(): Modifier = composed {
    val colorBase = MaterialTheme.colorScheme.surfaceContainerHighest
    val colorBrillo = MaterialTheme.colorScheme.surfaceContainerLowest
    val transicion = rememberInfiniteTransition(label = "shimmer")
    val avance by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerAvance",
    )
    this
        .background(colorBase)
        .drawWithCache {
            val anchoBarrido = size.width * 0.6f + 1f
            val x = -anchoBarrido + (size.width + anchoBarrido * 2) * avance
            val brush = Brush.linearGradient(
                colors = listOf(colorBase, colorBrillo, colorBase),
                start = Offset(x, 0f),
                end = Offset(x + anchoBarrido, size.height),
            )
            onDrawBehind { drawRect(brush) }
        }
}

@Composable
fun SkeletonBox(modifier: Modifier = Modifier, forma: Shape = RoundedCornerShape(8.dp)) {
    Box(modifier.clip(forma).shimmer())
}

@Composable
fun SkeletonCircle(tamaño: Dp, modifier: Modifier = Modifier) {
    Box(modifier.size(tamaño).clip(CircleShape).shimmer())
}

@Composable
fun SkeletonLine(ancho: Dp, alto: Dp = 14.dp, modifier: Modifier = Modifier) {
    Box(modifier.width(ancho).height(alto).clip(RoundedCornerShape(4.dp)).shimmer())
}

/**
 * Silueta de una "fila de card con avatar" — la forma que comparten
 * `TarjetaReto`/`TarjetaEnCurso` (Retos), `TarjetaCompromiso` (Apuestas) y
 * `TarjetaVersusPublico` (Buscar Versus): avatar circular + 2 líneas de
 * texto + una franja de acento a la izquierda.
 */
@Composable
fun SkeletonFilaCard(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp),
    ) {
        SkeletonCircle(48.dp)
        Spacer(Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonLine(140.dp)
            SkeletonLine(90.dp, alto = 10.dp)
        }
    }
}

/** Lista de `SkeletonFilaCard` con el mismo padding que ya usan las
 * `LazyColumn` de Retos/Apuestas/Buscar Versus — para las 3 pantallas que
 * comparten esa misma forma de card. */
@Composable
fun SkeletonListaFilas(padding: PaddingValues, cantidad: Int = 4, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(cantidad) { SkeletonFilaCard() }
    }
}
