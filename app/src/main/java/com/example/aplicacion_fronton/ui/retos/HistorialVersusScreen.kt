package com.example.aplicacion_fronton.ui.retos

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.model.dto.Modalidad
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.BotonVolver
import com.example.aplicacion_fronton.ui.componentes.SeccionEnCascada
import com.example.aplicacion_fronton.ui.componentes.SkeletonListaFilas
import com.example.aplicacion_fronton.ui.componentes.TarjetaPartidoHistorial
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle

@Composable
fun HistorialVersusScreen(
    onVolver: () -> Unit,
    onVerDetalle: (Int) -> Unit,
    onBuscarRivales: () -> Unit,
    viewModel: HistorialVersusViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val filtro by viewModel.filtro.collectAsStateWithLifecycle()

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
                    Text("HISTORIAL DE RETOS", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
            }
        },
    ) { padding ->
        when (val actual = estado) {
            is HistorialState.Cargando -> SkeletonListaFilas(padding)
            is HistorialState.Error -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(actual.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            is HistorialState.Exito -> {
                if (actual.partidos.isEmpty()) {
                    EstadoVacioHistorial(padding, onBuscarRivales)
                } else {
                    ContenidoHistorial(actual, filtro, padding, onCambiarFiltro = viewModel::cambiarFiltro, onVerDetalle = onVerDetalle)
                }
            }
        }
    }
}

@Composable
private fun ContenidoHistorial(
    estado: HistorialState.Exito,
    filtro: String,
    padding: PaddingValues,
    onCambiarFiltro: (String) -> Unit,
    onVerDetalle: (Int) -> Unit,
) {
    val filtrados = estado.partidos.filter { it.rivalNombre.contains(filtro, ignoreCase = true) }
    val victorias = filtrados.count { it.gane }
    val derrotas = filtrados.size - victorias

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

  SeccionEnCascada(visible, retraso = 0) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ELO INDIVIDUAL", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(estado.eloActual.toString(), style = NumericTextStyle.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(6.dp))
                            Text("PTS", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
                        }
                    }
                    if (estado.tendencia30Dias != null) {
                        val subio = estado.tendencia30Dias >= 0
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (subio) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (subio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    if (subio) "+${estado.tendencia30Dias}" else "${estado.tendencia30Dias}",
                                    style = NumericTextStyle.copy(fontWeight = FontWeight.Bold),
                                    color = if (subio) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                )
                            }
                            Text("ÚLTIMOS 30 DÍAS", style = CapsLabelTextStyle.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                if (estado.serieElo.size >= 2) {
                    GraficoElo(estado.serieElo, MaterialTheme.colorScheme.primary)
                } else {
                    Text(
                        "Todavía no hay suficientes partidos individuales confirmados para graficar la evolución.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = filtro,
                onValueChange = onCambiarFiltro,
                placeholder = { Text("Filtrar por rival...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (filtro.isBlank()) "TODOS LOS RETOS" else "HEAD TO HEAD",
                        style = CapsLabelTextStyle,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Row {
                    Text("${victorias}G", style = NumericTextStyle.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(" / ", style = NumericTextStyle, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f))
                    Text("${derrotas}P", style = NumericTextStyle.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        item {
            Text(
                "PARTIDOS",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        if (filtrados.isEmpty()) {
            item {
                Text(
                    "No hay partidos contra un rival con ese nombre.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            items(filtrados, key = { it.versusId }) { partido ->
                TarjetaPartidoHistorial(partido, onClick = { onVerDetalle(partido.versusId) })
            }
        }
    }
  }
}

/** Gráfico de evolución de Elo dibujado a mano con Canvas — no hay librería de
 * gráficos en el proyecto, y una serie de puntos reales (no fake) tampoco la
 * necesita: es una polilínea simple normalizada al alto disponible, con un
 * punto final pulsante para que se note dónde estás parado hoy. */
@Composable
private fun GraficoElo(serie: List<Int>, colorLinea: Color) {
    val minVal = serie.min()
    val maxVal = serie.max()
    val rango = (maxVal - minVal).coerceAtLeast(1)

    val transicionPulso = rememberInfiniteTransition(label = "pulsoElo")
    val radioPulsoDp by transicionPulso.animateFloat(
        initialValue = 4f,
        targetValue = 9f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
        label = "radioPulsoElo",
    )

    Canvas(modifier = Modifier.fillMaxWidth().height(90.dp).padding(top = 16.dp)) {
        val anchoPaso = if (serie.size > 1) size.width / (serie.size - 1) else 0f
        val altoUtil = size.height * 0.8f
        val margenVertical = size.height * 0.1f
        val puntos = serie.mapIndexed { indice, valor ->
            val x = indice * anchoPaso
            val normalizado = (valor - minVal).toFloat() / rango
            val y = size.height - margenVertical - (normalizado * altoUtil)
            Offset(x, y)
        }

        val path = Path().apply {
            moveTo(puntos.first().x, puntos.first().y)
            puntos.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, color = colorLinea, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        val ultimo = puntos.last()
        drawCircle(color = colorLinea.copy(alpha = 0.25f), radius = radioPulsoDp.dp.toPx(), center = ultimo)
        drawCircle(color = colorLinea, radius = 4.dp.toPx(), center = ultimo)
    }
}

@Composable
private fun EstadoVacioHistorial(padding: PaddingValues, onBuscarRivales: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.SportsTennis, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "AÚN NO TIENES RETOS JUGADOS",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "¡Es hora de entrar a la cancha y buscar un rival!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
        BotonTactil(
            texto = "Buscar rival",
            icono = Icons.Filled.SportsTennis,
            onClick = onBuscarRivales,
            colorContenedor = MaterialTheme.colorScheme.tertiary,
        )
    }
}
