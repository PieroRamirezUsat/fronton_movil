package com.example.aplicacion_fronton.ui.ranking

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MilitaryTech
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.ConfettiOverlay
import com.example.aplicacion_fronton.ui.componentes.ResplandorRadial
import com.example.aplicacion_fronton.ui.componentes.colorDivision
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Celebración a pantalla completa al ascender de división de Elo (Hierro →
 * Bronce, etc.) — mismo esqueleto de coreografía que `SubidaRankingScreen`
 * (fade + rebote + confeti + transición animada del valor protagonista), pero
 * puramente local: a diferencia de esa pantalla, acá no hace falta pedir el
 * perfil por red (nombre/foto) — la división nueva ya la trae `HomeViewModel`
 * de la misma comparación que dispara esta pantalla, así que es un Composable
 * sin ViewModel propio. El color de fondo es el de la división NUEVA
 * (`colorDivision`, compartido con `RankingScreen`), y el confeti mezcla los
 * colores de la división vieja y la nueva para que se sienta como una
 * transición, no solo un cambio de marca. */
@Composable
fun SubidaDivisionScreen(
    divisionAnterior: String,
    divisionNueva: String,
    onVerRanking: () -> Unit,
    onIrAHome: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val colorNueva = colorDivision(divisionNueva)
    val colorAnterior = colorDivision(divisionAnterior)

    val alfaFondo = remember { Animatable(0f) }
    val offsetIconoY = remember { Animatable(-140f) }
    val offsetTituloY = remember { Animatable(80f) }
    var mostrarParticulas by remember { mutableStateOf(false) }
    var divisionMostrada by remember { mutableStateOf(divisionAnterior) }
    var mostrarAcciones by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        alfaFondo.animateTo(1f, tween(300))
        launch {
            offsetIconoY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        }

        delay(200)
        offsetTituloY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))

        delay(300)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        mostrarParticulas = true
        divisionMostrada = divisionNueva

        delay(800)
        mostrarAcciones = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = alfaFondo.value }
            .background(Color(0xFF14171C))
            .safeDrawingPadding(),
    ) {
        if (mostrarParticulas) {
            ConfettiOverlay(
                disparar = true,
                modifier = Modifier.align(Alignment.Center),
                colores = listOf(colorNueva, colorAnterior, Color.White),
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.offset { IntOffset(0, offsetIconoY.value.roundToInt()) }) {
                ResplandorRadial(color = colorNueva, tamaño = 220.dp, alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(colorNueva.copy(alpha = 0.18f))
                        .border(3.dp, colorNueva, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.MilitaryTech, contentDescription = null, tint = colorNueva, modifier = Modifier.size(52.dp))
                }
            }

            Spacer(Modifier.height(28.dp))

            Text(
                "¡SUBISTE DE DIVISIÓN!",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset { IntOffset(0, offsetTituloY.value.roundToInt()) },
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "TU ELO YA TE PUSO EN OTRO NIVEL",
                style = CapsLabelTextStyle,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.offset { IntOffset(0, offsetTituloY.value.roundToInt()) },
            )

            Spacer(Modifier.height(28.dp))

            AnimatedContent(
                targetState = divisionMostrada,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 3 }) togetherWith
                        (fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it / 3 })
                },
                label = "division",
            ) { division ->
                Text(
                    division.uppercase(),
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 44.sp, fontWeight = FontWeight.Black),
                    color = colorDivision(division),
                )
            }

            AnimatedVisibility(
                visible = mostrarParticulas,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    Text(divisionAnterior.uppercase(), style = CapsLabelTextStyle.copy(fontSize = 11.sp), color = Color.White.copy(alpha = 0.5f))
                    Text(" → ", style = CapsLabelTextStyle.copy(fontSize = 11.sp), color = Color.White.copy(alpha = 0.5f))
                    Text(divisionNueva.uppercase(), style = CapsLabelTextStyle.copy(fontSize = 11.sp), color = colorNueva, fontWeight = FontWeight.Bold)
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
                    texto = "Ver ranking",
                    icono = Icons.Filled.Leaderboard,
                    saltoElastico = true,
                    onClick = onVerRanking,
                    colorContenedor = colorNueva,
                    colorTexto = Color(0xFF14171C),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = onIrAHome,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.1f)),
                ) {
                    Icon(Icons.Filled.Home, contentDescription = null, tint = Color.White.copy(alpha = 0.95f), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("IR AL INICIO", style = CapsLabelTextStyle, color = Color.White.copy(alpha = 0.95f), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
