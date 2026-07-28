package com.example.aplicacion_fronton.ui.retos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.CargandoPelotita
import com.example.aplicacion_fronton.ui.componentes.ConfettiOverlay
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val RojoDuelo = Color(0xFFD32F2F)
private val RojoDueloOscuro = Color(0xFF7A0000)
private val AzulDuelo = Color(0xFF1565C0)
private val AzulDueloOscuro = Color(0xFF002F6C)

/**
 * Animación de "duelo" al recibir un reto — sin plantilla Stitch que la
 * cubra (diseño propio). Pantalla completa: dos mitades chocan al centro
 * (rojo = quien retó, azul = quien recibe el reto — vos), con ráfaga de
 * partículas + rayos en el impacto. Soporta individual (1 jugador por lado)
 * y dobles (2, apilados).
 */
@Composable
fun DueloRetoScreen(
    versusId: Int,
    onVerReto: (Int) -> Unit,
    onContinuar: () -> Unit,
    viewModel: DueloRetoViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(versusId) { viewModel.cargar(versusId) }

    when (val actual = estado) {
        is DueloState.Cargando -> Box(Modifier.fillMaxSize().background(Color.Black).safeDrawingPadding(), contentAlignment = Alignment.Center) {
            CargandoPelotita(color = Color.White)
        }
        is DueloState.Error -> Box(Modifier.fillMaxSize().background(Color.Black).safeDrawingPadding().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(actual.mensaje, color = Color.White, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onContinuar) { Text("CONTINUAR", color = Color.White) }
            }
        }
        is DueloState.Exito -> ContenidoDuelo(actual.duelo, onVerReto = { onVerReto(versusId) }, onContinuar = onContinuar)
    }
}

@Composable
private fun ContenidoDuelo(duelo: DueloUi, onVerReto: () -> Unit, onContinuar: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val densidad = LocalDensity.current

    val alfaFondo = remember { Animatable(0f) }
    val fraccionRojo = remember { Animatable(-1f) } // -1 = fuera de pantalla a la izquierda
    val fraccionAzul = remember { Animatable(1f) } // 1 = fuera de pantalla a la derecha
    val alfaFlash = remember { Animatable(0f) }
    val offsetTituloY = remember { Animatable(-120f) }
    var mostrarParticulas by remember { mutableStateOf(false) }
    // A diferencia del flash inicial (un solo golpe), esto queda prendido todo
    // lo que dure la pantalla — rayos y llamas recurrentes, no un efecto que
    // se apaga a los 350ms y no vuelve.
    var mostrarEfectosConstantes by remember { mutableStateOf(false) }
    var mostrarAcciones by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        alfaFondo.animateTo(1f, tween(300))
        launch {
            fraccionRojo.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        }
        fraccionAzul.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))

        // Impacto: flash + haptic fuerte + partículas, y a partir de acá quedan
        // prendidos los rayos/llamas en loop.
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        mostrarParticulas = true
        mostrarEfectosConstantes = true
        launch {
            alfaFlash.animateTo(0.85f, tween(60))
            alfaFlash.animateTo(0f, tween(220))
        }

        delay(150)
        offsetTituloY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))

        delay(1400)
        mostrarAcciones = true
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val anchoPx = with(densidad) { maxWidth.toPx() }
        val mitadPx = anchoPx / 2

        Row(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = alfaFondo.value }) {
            MitadDuelo(
                jugadores = duelo.equipoRojo,
                colorClaro = RojoDuelo,
                colorOscuro = RojoDueloOscuro,
                alineacion = Alignment.CenterEnd,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .offset { IntOffset((fraccionRojo.value * mitadPx).roundToInt(), 0) },
            )
            MitadDuelo(
                jugadores = duelo.equipoAzul,
                colorClaro = AzulDuelo,
                colorOscuro = AzulDueloOscuro,
                alineacion = Alignment.CenterStart,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .offset { IntOffset((fraccionAzul.value * mitadPx).roundToInt(), 0) },
            )
        }

        // Costura central en zigzag tipo grieta/rayo — antes era solo el borde
        // recto entre los dos colores, "medio delineado" según pidió el
        // usuario: ahora la división en sí se ve como un impacto, no una
        // simple línea vertical.
        if (mostrarEfectosConstantes) CosturaRelampago(modifier = Modifier.fillMaxSize())

        // Ícono de choque sobre la costura.
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(3.dp, Color(0xFFFEC564), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.SportsTennis, contentDescription = null, tint = Color(0xFF1C1C15))
        }

        if (mostrarEfectosConstantes) RayosImpacto(modifier = Modifier.fillMaxSize())
        if (mostrarParticulas) {
            ConfettiOverlay(
                disparar = true,
                modifier = Modifier.align(Alignment.Center),
                colores = listOf(Color(0xFFFFC107), Color(0xFFFF7043), RojoDuelo, Color.White),
            )
        }

        // Flash blanco del impacto.
        Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = alfaFlash.value)))

        Column(
            modifier = Modifier.fillMaxSize().padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "¡TE HAN RETADO!",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset { IntOffset(0, offsetTituloY.value.roundToInt()) }
                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }

        AnimatedVisibility(
            visible = mostrarAcciones,
            enter = fadeIn(tween(300)) + expandVertically(tween(300)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(150)),
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 40.dp, start = 24.dp, end = 24.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                BotonTactil(
                    texto = "Ver reto",
                    icono = Icons.Filled.SportsTennis,
                    saltoElastico = true,
                    onClick = onVerReto,
                    colorContenedor = Color(0xFFFEC564),
                    colorTexto = Color(0xFF1C1C15),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                // Antes era texto blanco al 70% de opacidad directo sobre el
                // fondo rojo/azul — se leía mal. Un fondo oscuro translúcido
                // detrás sube el contraste real sin que el botón compita en
                // peso visual con "VER RETO".
                TextButton(
                    onClick = onContinuar,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.3f)),
                ) {
                    Text("AHORA NO", style = CapsLabelTextStyle, color = Color.White.copy(alpha = 0.95f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MitadDuelo(
    jugadores: List<JugadorDuelo>,
    colorClaro: Color,
    colorOscuro: Color,
    alineacion: Alignment,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.background(Brush.verticalGradient(listOf(colorClaro, colorOscuro))),
        contentAlignment = alineacion,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            jugadores.forEach { jugador -> AvatarDuelo(jugador) }
        }
    }
}

@Composable
private fun AvatarDuelo(jugador: JugadorDuelo) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            // Anillo de llamas detrás de la foto, en loop mientras la pantalla
            // esté abierta — antes no había nada "constante" alrededor de las
            // fotos, solo el estallido de confeti que se apaga a los pocos
            // segundos.
            LlamasAlrededorDeFoto(modifier = Modifier.size(150.dp))
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                val foto = jugador.fotoUrl.urlCompletaFoto()
                if (foto != null) {
                    AsyncImage(model = foto, contentDescription = jugador.nombre, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(CircleShape))
                } else {
                    Text(jugador.nombre.trim().take(2).uppercase(), style = MaterialTheme.typography.headlineSmall, color = Color.White)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            jugador.nombre.uppercase(),
            style = CapsLabelTextStyle.copy(fontSize = 12.sp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

/** Anillo de ~8 brasas que suben y se desvanecen en loop alrededor de una
 * foto — el efecto "llamas alrededor de las fotos, constante" que pidió el
 * usuario, no un estallido único. */
@Composable
private fun LlamasAlrededorDeFoto(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "llamas")
    val colores = listOf(
        Color(0xFFFFC107), Color(0xFFFF7043), Color(0xFFFF5722),
        Color(0xFFFFEB3B), Color(0xFFFF7043), Color(0xFFFFC107),
        Color(0xFFFF5722), Color(0xFFFFEB3B),
    )

    @Composable
    fun animarProgreso(indice: Int): Float {
        val progreso by transicion.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1300, easing = LinearEasing),
                initialStartOffset = StartOffset(indice * 1300 / colores.size),
            ),
            label = "llama$indice",
        )
        return progreso
    }

    val p0 = animarProgreso(0)
    val p1 = animarProgreso(1)
    val p2 = animarProgreso(2)
    val p3 = animarProgreso(3)
    val p4 = animarProgreso(4)
    val p5 = animarProgreso(5)
    val p6 = animarProgreso(6)
    val p7 = animarProgreso(7)
    val progresos = listOf(p0, p1, p2, p3, p4, p5, p6, p7)

    Canvas(modifier = modifier) {
        val radio = size.minDimension / 2f
        progresos.forEachIndexed { i, progreso ->
            val angulo = (i * (360f / progresos.size)) * (Math.PI / 180f)
            val distancia = radio * (0.78f + progreso * 0.3f)
            val x = (size.width / 2 + distancia * kotlin.math.cos(angulo)).toFloat()
            val y = (size.height / 2 + distancia * kotlin.math.sin(angulo) - progreso * 16f).toFloat()
            val alfa = (1f - progreso).coerceIn(0f, 1f)
            drawCircle(colores[i].copy(alpha = alfa * 0.9f), radius = 5f + (1f - progreso) * 3f, center = Offset(x, y))
        }
    }
}

/** Grieta en zigzag, con brillo pulsante, marcando la costura roja/azul — y
 * un par de rayos que destellan cerca del centro en loop (no un flash único
 * que se apaga a los 350ms). */
@Composable
private fun CosturaRelampago(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "costura")
    val brillo by transicion.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(650, easing = LinearOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "brilloCostura",
    )
    Canvas(modifier = modifier) {
        val centroX = size.width / 2
        val puntos = listOf(
            Offset(centroX, 0f),
            Offset(centroX - 16f, size.height * 0.12f),
            Offset(centroX + 12f, size.height * 0.22f),
            Offset(centroX - 20f, size.height * 0.35f),
            Offset(centroX + 14f, size.height * 0.5f),
            Offset(centroX - 16f, size.height * 0.65f),
            Offset(centroX + 12f, size.height * 0.8f),
            Offset(centroX, size.height),
        )
        for (i in 0 until puntos.size - 1) {
            // Glow ancho detrás, línea fina brillante encima.
            drawLine(Color(0xFFFEC564).copy(alpha = brillo * 0.5f), puntos[i], puntos[i + 1], strokeWidth = 14f, cap = StrokeCap.Round)
            drawLine(Color.White.copy(alpha = brillo), puntos[i], puntos[i + 1], strokeWidth = 4f, cap = StrokeCap.Round)
        }
    }
}

/** Rayos en zigzag que destellan cerca del centro, en loop. */
@Composable
private fun RayosImpacto(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "rayos")
    val alfa by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1600
                0f at 0
                1f at 80
                0.15f at 220
                0f at 420
                0f at 1600
            },
        ),
        label = "alfaRayos",
    )
    Canvas(modifier = modifier) {
        val centroX = size.width / 2
        val colorRayo = Color(0xFFFFC107).copy(alpha = alfa)
        listOf(-40f, 40f).forEach { desplazo ->
            val puntos = listOf(
                Offset(centroX + desplazo, size.height * 0.15f),
                Offset(centroX + desplazo * 0.4f, size.height * 0.32f),
                Offset(centroX + desplazo * 1.3f, size.height * 0.42f),
                Offset(centroX + desplazo * 0.2f, size.height * 0.6f),
                Offset(centroX + desplazo, size.height * 0.78f),
            )
            for (i in 0 until puntos.size - 1) {
                drawLine(colorRayo, puntos[i], puntos[i + 1], strokeWidth = 5f, cap = StrokeCap.Round)
            }
        }
    }
}
