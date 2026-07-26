package com.example.aplicacion_fronton.ui.retos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
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
import com.example.aplicacion_fronton.ui.componentes.ConfettiOverlay
import com.example.aplicacion_fronton.ui.componentes.ResplandorRadial
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Celebración a pantalla completa al ganar un partido — mismo esqueleto de
 * coreografía que `DueloRetoScreen` (fade + rebote + confeti + resplandor +
 * botones diferidos), con identidad propia: fondo primary/teal de marca y
 * acentos dorados (trofeo), en vez de la partición roja/azul del duelo. */
@Composable
fun VictoriaPartidoScreen(
    versusId: Int,
    eloAntes: Int,
    eloDespues: Int,
    onVerDetalle: () -> Unit,
    onIrAHome: () -> Unit,
    viewModel: VictoriaPartidoViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(versusId) { viewModel.cargar(versusId) }

    when (val actual = estado) {
        is VictoriaPartidoState.Cargando -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary).safeDrawingPadding(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        is VictoriaPartidoState.Error -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary).safeDrawingPadding().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(actual.mensaje, color = Color.White, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onIrAHome) { Text("CONTINUAR", color = Color.White) }
            }
        }
        is VictoriaPartidoState.Exito -> ContenidoVictoriaPartido(
            datos = actual.datos,
            eloAntes = eloAntes,
            eloDespues = eloDespues,
            onVerDetalle = onVerDetalle,
            onIrAHome = onIrAHome,
        )
    }
}

@Composable
private fun ContenidoVictoriaPartido(
    datos: VictoriaPartidoUi,
    eloAntes: Int,
    eloDespues: Int,
    onVerDetalle: () -> Unit,
    onIrAHome: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val delta = eloDespues - eloAntes

    val alfaFondo = remember { Animatable(0f) }
    val offsetTrofeoY = remember { Animatable(-140f) }
    val offsetTituloY = remember { Animatable(80f) }
    var mostrarParticulas by remember { mutableStateOf(false) }
    var mostrarElo by remember { mutableStateOf(false) }
    var mostrarAcciones by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        alfaFondo.animateTo(1f, tween(300))
        launch {
            offsetTrofeoY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        }

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        mostrarParticulas = true

        delay(200)
        offsetTituloY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))

        delay(400)
        mostrarElo = true

        delay(800)
        mostrarAcciones = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = alfaFondo.value }
            .background(MaterialTheme.colorScheme.primary)
            .safeDrawingPadding(),
    ) {
        if (mostrarParticulas) {
            ConfettiOverlay(
                disparar = true,
                modifier = Modifier.align(Alignment.Center),
                colores = listOf(Color(0xFFFEC564), Color.White, MaterialTheme.colorScheme.secondaryContainer),
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.offset { IntOffset(0, offsetTrofeoY.value.roundToInt()) }) {
                ResplandorRadial(color = MaterialTheme.colorScheme.secondaryContainer, tamaño = 220.dp, alpha = 0.4f)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(52.dp))
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "¡GANASTE EL PARTIDO!",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset { IntOffset(0, offsetTituloY.value.roundToInt()) },
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "vs. ${datos.rivalNombre}".uppercase(),
                style = CapsLabelTextStyle,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.offset { IntOffset(0, offsetTituloY.value.roundToInt()) },
            )

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(datos.misSets.toString(), style = NumericTextStyle.copy(fontSize = 56.sp, fontWeight = FontWeight.Bold), color = Color.White)
                Text("-", style = MaterialTheme.typography.headlineMedium, color = Color.White.copy(alpha = 0.6f))
                Text(datos.susSets.toString(), style = NumericTextStyle.copy(fontSize = 56.sp, fontWeight = FontWeight.Bold), color = Color.White)
            }

            AnimatedVisibility(
                visible = mostrarElo,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(50))
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                    ) {
                        Text(
                            if (delta >= 0) "+$delta PTS" else "$delta PTS",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("NUEVO ELO: $eloDespues", style = CapsLabelTextStyle.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.6f))
                }
            }
        }

        AnimatedVisibility(
            visible = mostrarAcciones,
            enter = fadeIn(tween(300)) + expandVertically(tween(300)),
            exit = fadeOut(tween(150)) + shrinkVertically(tween(150)),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp, start = 24.dp, end = 24.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                BotonTactil(
                    texto = "Ver detalle",
                    icono = Icons.Filled.Person,
                    saltoElastico = true,
                    onClick = onVerDetalle,
                    colorContenedor = MaterialTheme.colorScheme.secondaryContainer,
                    colorTexto = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = onIrAHome,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.15f)),
                ) {
                    Icon(Icons.Filled.Home, contentDescription = null, tint = Color.White.copy(alpha = 0.95f), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("IR AL INICIO", style = CapsLabelTextStyle, color = Color.White.copy(alpha = 0.95f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
