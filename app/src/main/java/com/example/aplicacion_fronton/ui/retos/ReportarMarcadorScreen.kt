package com.example.aplicacion_fronton.ui.retos

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.BotonVolver
import com.example.aplicacion_fronton.ui.componentes.ConfettiOverlay
import com.example.aplicacion_fronton.ui.componentes.ResplandorRadial
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle

@Composable
fun ReportarMarcadorScreen(
    versusId: Int,
    onVolver: () -> Unit,
    onVictoria: (eloAntes: Int, eloDespues: Int) -> Unit,
    viewModel: ReportarMarcadorViewModel = viewModel(),
) {
    val carga by viewModel.carga.collectAsState()
    val envio by viewModel.envio.collectAsState()

    LaunchedEffect(versusId) { viewModel.cargar(versusId) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    BotonVolver(onClick = onVolver)
                    Text("REPORTAR MARCADOR", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
            }
        },
    ) { padding ->
        when (val estado = carga) {
            is CargaPartidoState.Cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is CargaPartidoState.Error -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(estado.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            is CargaPartidoState.Exito -> ContenidoReportar(
                estado = estado,
                envio = envio,
                padding = padding,
                onEnviar = { propios, rival -> viewModel.enviarMarcador(versusId, propios, rival, estado.miId) },
                onFinalizar = onVolver,
                onVictoria = onVictoria,
                onReintentar = viewModel::reintentar,
            )
        }
    }
}

@Composable
private fun ContenidoReportar(
    estado: CargaPartidoState.Exito,
    envio: EnvioMarcadorState,
    padding: PaddingValues,
    onEnviar: (Int, Int) -> Unit,
    onFinalizar: () -> Unit,
    onVictoria: (eloAntes: Int, eloDespues: Int) -> Unit,
    onReintentar: () -> Unit,
) {
    var setsPropios by remember { mutableIntStateOf(0) }
    var setsRival by remember { mutableIntStateOf(0) }
    val enviando = envio is EnvioMarcadorState.Enviando
    val bloqueado = envio !is EnvioMarcadorState.Formulario && envio !is EnvioMarcadorState.Error

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        // Tarjeta resumen del partido
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(2.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(12.dp))
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                JugadorMini(estado.miNombre, estado.miFotoUrl, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                Text(
                    "VS",
                    style = MaterialTheme.typography.titleLarge.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                )
                JugadorMini(estado.rivalNombre, estado.rivalFotoUrl, MaterialTheme.colorScheme.outline, Modifier.weight(1f))
            }
            HorizontalDivider(modifier = Modifier.padding(top = 12.dp, bottom = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(estado.versus.cancha ?: "Sin cancha definida", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(24.dp))

        // Marcador / estados de envío
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 28.dp, horizontal = 16.dp),
        ) {
            AnimatedContent(
                targetState = envio,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                label = "estadoMarcador",
            ) { estadoEnvio ->
                when (estadoEnvio) {
                    is EnvioMarcadorState.Formulario, is EnvioMarcadorState.Error, is EnvioMarcadorState.Enviando -> {
                        Column {
                            Text(
                                "PUNTUACIÓN FINAL (SETS)",
                                style = CapsLabelTextStyle,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(Modifier.height(20.dp))
                            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                                SelectorSet(
                                    valor = setsPropios,
                                    onIncrementar = { if (setsPropios < 3) setsPropios++ },
                                    onDecrementar = { if (setsPropios > 0) setsPropios-- },
                                    habilitado = !enviando,
                                )
                                Box(modifier = Modifier.width(2.dp).height(90.dp).background(Color.White.copy(alpha = 0.2f)))
                                SelectorSet(
                                    valor = setsRival,
                                    onIncrementar = { if (setsRival < 3) setsRival++ },
                                    onDecrementar = { if (setsRival > 0) setsRival-- },
                                    habilitado = !enviando,
                                )
                            }
                        }
                    }
                    is EnvioMarcadorState.Esperando -> EstadoEsperando(estadoEnvio.mensaje)
                    is EnvioMarcadorState.Disputa -> EstadoDisputa(estadoEnvio.mensaje)
                    is EnvioMarcadorState.Confirmado -> EstadoConfirmado(estadoEnvio)
                }
            }
        }

        if (envio is EnvioMarcadorState.Error) {
            Spacer(Modifier.height(12.dp))
            Text(envio.mensaje, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        if (!bloqueado) {
            Spacer(Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .padding(14.dp),
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 2.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "Anota los sets a favor de cada uno. Tu rival debe reportar el MISMO marcador por su cuenta — si ambos coinciden, se confirma solo. Si no coinciden, queda en disputa y cualquiera puede corregirlo y reintentar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        when {
            envio is EnvioMarcadorState.Confirmado -> BotonTactil(
                texto = "Finalizar",
                icono = Icons.Filled.CheckCircle,
                onClick = {
                    if (envio.setsPropios > envio.setsRival) {
                        onVictoria(envio.eloAntes, envio.eloDespues)
                    } else {
                        onFinalizar()
                    }
                },
                colorContenedor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
            )
            envio is EnvioMarcadorState.Disputa -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BotonTactil(
                    texto = "Corregir marcador",
                    icono = Icons.Filled.Info,
                    onClick = onReintentar,
                    colorContenedor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth(),
                )
                BotonTactil(
                    texto = "Volver al detalle",
                    onClick = onFinalizar,
                    colorContenedor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            envio is EnvioMarcadorState.Esperando -> BotonTactil(
                texto = "Volver al detalle",
                onClick = onFinalizar,
                colorContenedor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
            )
            else -> BotonTactil(
                texto = "Enviar marcador",
                icono = Icons.AutoMirrored.Filled.Send,
                saltoElastico = true,
                onClick = { onEnviar(setsPropios, setsRival) },
                colorContenedor = MaterialTheme.colorScheme.tertiary,
                cargando = enviando,
                enabled = setsPropios != setsRival,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (setsPropios == setsRival && envio is EnvioMarcadorState.Formulario) {
            Spacer(Modifier.height(8.dp))
            Text(
                "El marcador no puede terminar en empate.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun JugadorMini(nombre: String, fotoUrl: String?, colorBorde: Color, modifier: Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(2.dp, colorBorde, CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            val foto = fotoUrl.urlCompletaFoto()
            if (foto != null) {
                AsyncImage(model = foto, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(CircleShape))
            } else {
                Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(nombre.uppercase(), style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 2)
    }
}

@Composable
private fun SelectorSet(valor: Int, onIncrementar: () -> Unit, onDecrementar: () -> Unit, habilitado: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onIncrementar, enabled = habilitado, modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f))) {
            Icon(Icons.Filled.Add, contentDescription = "Sumar set", tint = Color.White)
        }
        Text(valor.toString(), style = NumericTextStyle.copy(fontSize = 56.sp, fontWeight = FontWeight.Bold), color = Color.White)
        IconButton(onClick = onDecrementar, enabled = habilitado, modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f))) {
            Icon(Icons.Filled.Remove, contentDescription = "Restar set", tint = Color.White)
        }
    }
}

@Composable
private fun EstadoEsperando(mensaje: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        val transicion = rememberInfiniteTransicion()
        Icon(
            Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(56.dp).alpha(transicion),
        )
        Spacer(Modifier.height(16.dp))
        Text("ESPERANDO RIVAL...", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(mensaje, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun rememberInfiniteTransicion(): Float {
    val infinite = androidx.compose.animation.core.rememberInfiniteTransition(label = "pulso")
    val alpha by infinite.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(750, easing = LinearOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "pulsoAlpha",
    )
    return alpha
}

@Composable
private fun EstadoDisputa(mensaje: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(16.dp))
        Text("EN DISPUTA", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(mensaje, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun EstadoConfirmado(estado: EnvioMarcadorState.Confirmado) {
    val delta = estado.eloDespues - estado.eloAntes
    var mostrarElo by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(500)
        mostrarElo = true
    }
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            // Resplandor detrás de la medalla — antes este era el momento de
            // logro más frecuente de la app (cada partido reportado) y el
            // único con fondo completamente plano, sin nada del vocabulario
            // de "resplandor" que ya existe en Splash/Duelo.
            Box(contentAlignment = Alignment.Center) {
                ResplandorRadial(color = MaterialTheme.colorScheme.secondaryContainer, tamaño = 180.dp, alpha = 0.45f)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("¡MARCADOR CONFIRMADO!", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(estado.setsPropios.toString(), style = NumericTextStyle.copy(fontSize = 44.sp, fontWeight = FontWeight.Bold), color = Color.White)
                Text("-", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.secondary)
                Text(estado.setsRival.toString(), style = NumericTextStyle.copy(fontSize = 44.sp, fontWeight = FontWeight.Bold), color = Color.White)
            }
            if (mostrarElo) {
                Spacer(Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.1f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(50))
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
                Text("NUEVO ELO: ${estado.eloDespues}", style = CapsLabelTextStyle.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.6f))
            }
        }
        ConfettiOverlay(disparar = mostrarElo)
    }
}
