package com.example.aplicacion_fronton.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsTennis
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacion_fronton.ui.theme.MonoFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Colores propios de esta pantalla (fondo oscuro atmosférico) — no vienen del
// ColorScheme claro de la app, son los del mockup splash_screen_front_n_din_mico_2.
private val Petroleo = Color(0xFF146259)
private val PetroleoOscuro = Color(0xFF0F1E1B)
private val Dorado = Color(0xFFFEC564)
private val TealClaro = Color(0xFFA9F0E4)

/**
 * Réplica de `splash_screen_front_n_din_mico_2` (la variante elegida sobre `_1`
 * — ver memoria de catálogo de pantallas): el bloque de marca completo cae y
 * rebota junto, seguido de una onda de impacto expansiva. Usa "PALETA FRONTÓN"
 * en vez de "Frontón Dinámico" para ser consistente con el resto de la app.
 */
@Composable
fun SplashScreen(onTerminado: () -> Unit) {
    val offsetY = remember { Animatable(-260f) }
    val opacidad = remember { Animatable(0f) }
    val onda = remember { Animatable(0f) }
    val escalaX = remember { Animatable(1f) }
    val escalaY = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch { opacidad.animateTo(1f, tween(500, easing = LinearEasing)) }
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = keyframes {
                durationMillis = 1500
                -260f at 0
                0f at 600 using CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)
                -32f at 825
                0f at 1050
                -10f at 1200
                0f at 1500
            },
        )
    }

    // Squash/stretch real en el aterrizaje (600ms) — antes el "rebote" era solo
    // un desplazamiento vertical, sin la deformación de peso que sí describe
    // el mockup. Se aplana horizontalmente y se estira al achatarse, como
    // cualquier objeto real que golpea una superficie.
    LaunchedEffect(Unit) {
        delay(600)
        launch {
            escalaX.animateTo(1.15f, tween(90, easing = LinearOutSlowInEasing))
            escalaX.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        escalaY.animateTo(0.85f, tween(90, easing = LinearOutSlowInEasing))
        escalaY.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    LaunchedEffect(Unit) {
        delay(600)
        onda.animateTo(1f, tween(800, easing = LinearEasing))
    }

    LaunchedEffect(Unit) {
        delay(1900)
        onTerminado()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(colors = listOf(Petroleo, PetroleoOscuro))),
        contentAlignment = Alignment.Center,
    ) {
        // Grilla sutil tipo "líneas de cancha" — 3% opacidad, igual que el mockup.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val paso = 80.dp.toPx()
            val grosor = 2.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(Color.White.copy(alpha = 0.03f), Offset(x, 0f), Offset(x, size.height), strokeWidth = grosor)
                x += paso
            }
            var y = 0f
            while (y < size.height) {
                drawLine(Color.White.copy(alpha = 0.03f), Offset(0f, y), Offset(size.width, y), strokeWidth = grosor)
                y += paso
            }
        }

        // Resplandor atmosférico de fondo, centrado.
        Box(
            modifier = Modifier
                .size(320.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Petroleo.copy(alpha = 0.35f), Color.Transparent),
                    ),
                    CircleShape,
                ),
        )

        // Onda de impacto — dispara 600ms después de cargar, crece y se desvanece.
        Box(
            modifier = Modifier
                .size((onda.value * 260).dp)
                .border((2.dp), TealClaro.copy(alpha = (1f - onda.value) * 0.25f), CircleShape),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = offsetY.value.dp)
                .graphicsLayer {
                    alpha = opacidad.value
                    scaleX = escalaX.value
                    scaleY = escalaY.value
                }
                .padding(horizontal = 20.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(4.dp, Dorado, CircleShape)
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.SportsTennis, contentDescription = null, tint = Dorado, modifier = Modifier.size(32.dp))
            }

            Box(modifier = Modifier.padding(top = 32.dp)) {
                Text(
                    "PALETA FRONTÓN",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(96.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Dorado),
            )
            Text(
                "Poder • Precisión • Prestigio",
                style = MaterialTheme.typography.bodyMedium,
                color = TealClaro.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
        ) {
            Text(
                "INICIANDO SESIÓN",
                fontFamily = MonoFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                color = Color.White.copy(alpha = 0.4f),
            )
            Box(modifier = Modifier.padding(top = 16.dp)) {
                BarraDeCargaIndeterminada()
            }
        }
    }
}

@Composable
private fun BarraDeCargaIndeterminada() {
    val anchoContenedor = 192.dp
    val transicion = rememberInfiniteTransition(label = "cargaInfinita")
    val fraccionAncho by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0f at 0
                0.4f at 1000
                0f at 2000
            },
        ),
        label = "anchoBarra",
    )
    val fraccionPosicion by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0f at 0
                0.3f at 1000
                1f at 2000
            },
        ),
        label = "posicionBarra",
    )

    Box(
        modifier = Modifier
            .width(anchoContenedor)
            .height(2.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.1f)),
    ) {
        Box(
            modifier = Modifier
                .offset(x = anchoContenedor * fraccionPosicion)
                .width(anchoContenedor * fraccionAncho)
                .height(2.dp)
                .background(Dorado),
        )
    }
}
