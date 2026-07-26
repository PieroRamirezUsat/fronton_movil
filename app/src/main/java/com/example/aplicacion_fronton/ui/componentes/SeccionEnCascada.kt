package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable

/**
 * Cascada de aparición por sección (fade+slide con delay escalonado) — antes
 * vivía duplicada como composable privado en `HomeScreen.kt` y
 * `ReporteApuestasScreen.kt`. Uso: envolver cada sección de una pantalla con
 * `retraso` creciente (0, 100, 200...) sobre un `visible` disparado una sola
 * vez con `LaunchedEffect(Unit)` al entrar a la pantalla.
 */
@Composable
fun SeccionEnCascada(visible: Boolean, retraso: Int, contenido: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400, delayMillis = retraso)) +
            slideInVertically(tween(400, delayMillis = retraso)) { it / 5 },
    ) {
        contenido()
    }
}
