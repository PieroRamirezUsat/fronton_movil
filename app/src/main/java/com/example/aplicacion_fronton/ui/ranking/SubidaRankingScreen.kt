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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.CargandoPelotita
import com.example.aplicacion_fronton.ui.componentes.ConfettiOverlay
import com.example.aplicacion_fronton.ui.componentes.ResplandorRadial
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

sealed class SubidaRankingState {
    data object Cargando : SubidaRankingState()
    data class Exito(val nombre: String, val fotoUrl: String?) : SubidaRankingState()
    data class Error(val mensaje: String) : SubidaRankingState()
}

class SubidaRankingViewModel : ViewModel() {
    private val _estado = MutableStateFlow<SubidaRankingState>(SubidaRankingState.Cargando)
    val estado: StateFlow<SubidaRankingState> = _estado.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _estado.value = SubidaRankingState.Cargando
            val resultado = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            _estado.value = when (resultado) {
                is ApiResult.Exito -> SubidaRankingState.Exito(resultado.datos.nombre, resultado.datos.foto_url)
                is ApiResult.Error -> SubidaRankingState.Error(resultado.mensaje)
            }
        }
    }
}

/** Celebración a pantalla completa al mejorar de posición en el ranking —
 * mismo esqueleto de coreografía que `DueloRetoScreen`/`VictoriaPartidoScreen`,
 * con identidad dorada (misma que ya usa el podio de Ranking) y la posición
 * como protagonista: transición animada de la posición anterior a la nueva. */
@Composable
fun SubidaRankingScreen(
    posicionAnterior: Int,
    posicionNueva: Int,
    onVerRanking: () -> Unit,
    onIrAHome: () -> Unit,
    viewModel: SubidaRankingViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.cargar() }

    when (val actual = estado) {
        is SubidaRankingState.Cargando -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer).safeDrawingPadding(), contentAlignment = Alignment.Center) {
            CargandoPelotita(color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        is SubidaRankingState.Error -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.secondaryContainer).safeDrawingPadding().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(actual.mensaje, color = MaterialTheme.colorScheme.onSecondaryContainer, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onIrAHome) { Text("CONTINUAR", color = MaterialTheme.colorScheme.onSecondaryContainer) }
            }
        }
        is SubidaRankingState.Exito -> ContenidoSubidaRanking(
            nombre = actual.nombre,
            fotoUrl = actual.fotoUrl,
            posicionAnterior = posicionAnterior,
            posicionNueva = posicionNueva,
            onVerRanking = onVerRanking,
            onIrAHome = onIrAHome,
        )
    }
}

@Composable
private fun ContenidoSubidaRanking(
    nombre: String,
    fotoUrl: String?,
    posicionAnterior: Int,
    posicionNueva: Int,
    onVerRanking: () -> Unit,
    onIrAHome: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val entroAlPodio = posicionNueva <= 3

    val alfaFondo = remember { Animatable(0f) }
    val offsetFotoY = remember { Animatable(-140f) }
    val offsetTituloY = remember { Animatable(80f) }
    var mostrarParticulas by remember { mutableStateOf(false) }
    var posicionMostrada by remember { mutableStateOf(posicionAnterior) }
    var mostrarPodio by remember { mutableStateOf(false) }
    var mostrarAcciones by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        alfaFondo.animateTo(1f, tween(300))
        launch {
            offsetFotoY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        }

        delay(200)
        offsetTituloY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))

        delay(300)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        mostrarParticulas = true
        posicionMostrada = posicionNueva

        delay(500)
        if (entroAlPodio) mostrarPodio = true

        delay(if (entroAlPodio) 500L else 300L)
        mostrarAcciones = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = alfaFondo.value }
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .safeDrawingPadding(),
    ) {
        if (mostrarParticulas) {
            ConfettiOverlay(
                disparar = true,
                modifier = Modifier.align(Alignment.Center),
                colores = listOf(MaterialTheme.colorScheme.onSecondaryContainer, Color.White, Color(0xFF146259)),
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.offset { IntOffset(0, offsetFotoY.value.roundToInt()) }) {
                ResplandorRadial(color = Color.White, tamaño = 200.dp, alpha = 0.5f)
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                        .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    val foto = fotoUrl.urlCompletaFoto()
                    if (foto != null) {
                        AsyncImage(model = foto, contentDescription = nombre, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(CircleShape))
                    } else {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(40.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "¡SUBISTE EN EL RANKING!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset { IntOffset(0, offsetTituloY.value.roundToInt()) },
            )
            Spacer(Modifier.height(4.dp))
            Text(
                nombre.uppercase(),
                style = CapsLabelTextStyle,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                textAlign = TextAlign.Center,
                modifier = Modifier.offset { IntOffset(0, offsetTituloY.value.roundToInt()) },
            )

            Spacer(Modifier.height(20.dp))

            AnimatedContent(
                targetState = posicionMostrada,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 3 }) togetherWith
                        (fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it / 3 })
                },
                label = "posicion",
            ) { posicion ->
                Text(
                    "#$posicion",
                    style = NumericTextStyle.copy(fontSize = 72.sp, fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }

            AnimatedVisibility(
                visible = mostrarPodio,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.3f))
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("¡ENTRASTE AL PODIO!", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
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
                    colorContenedor = Color(0xFF146259),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = onIrAHome,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.12f)),
                ) {
                    Icon(Icons.Filled.Home, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("IR AL INICIO", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
