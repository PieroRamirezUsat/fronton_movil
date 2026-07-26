package com.example.aplicacion_fronton.ui.compromisos

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.ConfettiOverlay
import com.example.aplicacion_fronton.ui.componentes.ResplandorRadial
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Celebración a pantalla completa al ganar una apuesta — mismo esqueleto de
 * coreografía que `DueloRetoScreen`/`VictoriaPartidoScreen`, con identidad
 * terracota/tertiary (misma que ya usa Apuestas en el resto de la app) y la
 * descripción de lo apostado como protagonista, mismo estilo "entre comillas
 * en cursiva" ya usado en el Reporte de Apuestas. */
@Composable
fun VictoriaApuestaScreen(
    compromisoId: Int,
    onVerDetalle: () -> Unit,
    onIrAHome: () -> Unit,
    viewModel: VictoriaApuestaViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(compromisoId) { viewModel.cargar(compromisoId) }

    when (val actual = estado) {
        is VictoriaApuestaState.Cargando -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.tertiary).safeDrawingPadding(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        is VictoriaApuestaState.Error -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.tertiary).safeDrawingPadding().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(actual.mensaje, color = Color.White, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onIrAHome) { Text("CONTINUAR", color = Color.White) }
            }
        }
        is VictoriaApuestaState.Exito -> ContenidoVictoriaApuesta(
            datos = actual.datos,
            onVerDetalle = onVerDetalle,
            onIrAHome = onIrAHome,
        )
    }
}

@Composable
private fun ContenidoVictoriaApuesta(
    datos: VictoriaApuestaUi,
    onVerDetalle: () -> Unit,
    onIrAHome: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    val alfaFondo = remember { Animatable(0f) }
    val offsetIconoY = remember { Animatable(-140f) }
    val offsetTituloY = remember { Animatable(80f) }
    var mostrarParticulas by remember { mutableStateOf(false) }
    var mostrarDescripcion by remember { mutableStateOf(false) }
    var mostrarAcciones by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        alfaFondo.animateTo(1f, tween(300))
        launch {
            offsetIconoY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        }

        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        mostrarParticulas = true

        delay(200)
        offsetTituloY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))

        delay(400)
        mostrarDescripcion = true

        delay(800)
        mostrarAcciones = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = alfaFondo.value }
            .background(MaterialTheme.colorScheme.tertiary)
            .safeDrawingPadding(),
    ) {
        if (mostrarParticulas) {
            ConfettiOverlay(
                disparar = true,
                modifier = Modifier.align(Alignment.Center),
                colores = listOf(Color(0xFFFEC564), Color.White, MaterialTheme.colorScheme.tertiaryContainer),
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.offset { IntOffset(0, offsetIconoY.value.roundToInt()) }) {
                ResplandorRadial(color = Color(0xFFFEC564), tamaño = 220.dp, alpha = 0.4f)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEC564)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Paid, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(48.dp))
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "¡GANASTE LA APUESTA!",
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

            AnimatedVisibility(
                visible = mostrarDescripcion,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
            ) {
                Text(
                    "\"${datos.descripcion}\"",
                    style = MaterialTheme.typography.titleLarge.copy(fontStyle = FontStyle.Italic),
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.15f))
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                )
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
                    icono = Icons.Filled.Visibility,
                    saltoElastico = true,
                    onClick = onVerDetalle,
                    colorContenedor = Color(0xFFFEC564),
                    colorTexto = MaterialTheme.colorScheme.tertiary,
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
