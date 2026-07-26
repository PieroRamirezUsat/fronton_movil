package com.example.aplicacion_fronton.ui.compromisos

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonVolver
import com.example.aplicacion_fronton.ui.componentes.EstadoVacio
import com.example.aplicacion_fronton.ui.componentes.ResplandorRadial
import com.example.aplicacion_fronton.ui.componentes.SeccionEnCascada
import com.example.aplicacion_fronton.ui.componentes.SkeletonBox
import com.example.aplicacion_fronton.ui.componentes.SkeletonFilaCard
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun ReporteApuestasScreen(
    onVolver: () -> Unit,
    viewModel: ReporteApuestasViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.cargar() }

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
                    Text("REPORTE DE APUESTAS", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
            }
        },
    ) { padding ->
        when (val actual = estado) {
            is ReporteApuestasState.Cargando -> SkeletonReporte(padding)
            is ReporteApuestasState.Error -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(actual.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            is ReporteApuestasState.Exito -> if (!actual.hayDatos) {
                EstadoVacio(
                    icono = Icons.Filled.Handshake,
                    titulo = "TODAVÍA NO HAY NADA QUE REPORTAR",
                    subtitulo = "Cuando registres o saldes tu primera apuesta, tu reporte va a aparecer acá.",
                    modifier = Modifier.padding(padding),
                    colorCirculo = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                    colorIcono = MaterialTheme.colorScheme.tertiary,
                )
            } else {
                ContenidoReporte(actual, padding)
            }
        }
    }
}

@Composable
private fun ContenidoReporte(actual: ReporteApuestasState.Exito, padding: PaddingValues) {
    // Antes esta pantalla no tenía ninguna animación de entrada — la misma
    // cascada de fade+slide escalonado que ya usa Home por sección, para que
    // el reporte se sienta parte del mismo sistema, no una pantalla aparte.
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        if (actual.tasaVictoria != null) {
            SeccionEnCascada(visible, retraso = 0) {
                TarjetaRecordApuestas(actual.tasaVictoria, actual.victorias, actual.derrotas)
            }
        }

        SeccionEnCascada(visible, retraso = 100) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                EstadisticaReporte("SALDADAS", actual.saldadas.toString(), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                EstadisticaReporte("PENDIENTES", actual.pendientes.toString(), MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                EstadisticaRacha(actual.racha, Modifier.weight(1f))
            }
        }

        if (actual.evolucion.size >= 2) {
            SeccionEnCascada(visible, retraso = 200) {
                Column {
                    TituloSeccionReporte("EVOLUCIÓN DE APUESTAS")
                    Spacer(Modifier.height(8.dp))
                    GraficoEvolucionApuestas(actual.evolucion)
                }
            }
        }

        if (actual.rivalesFrecuentes.isNotEmpty()) {
            SeccionEnCascada(visible, retraso = 300) {
                Column {
                    TituloSeccionReporte("CON QUIÉN MÁS APUESTAS")
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        actual.rivalesFrecuentes.forEach { FilaRivalFrecuente(it) }
                    }
                }
            }
        }

        if (actual.ultimasApuestas.isNotEmpty()) {
            SeccionEnCascada(visible, retraso = 400) {
                Column {
                    TituloSeccionReporte("ÚLTIMAS APUESTAS")
                    Spacer(Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        actual.ultimasApuestas.forEach { FilaApuesta(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TituloSeccionReporte(texto: String) {
    val colorFranja = MaterialTheme.colorScheme.primary
    Text(
        texto,
        style = CapsLabelTextStyle,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .drawBehind { drawRect(color = colorFranja, size = Size(2.dp.toPx(), size.height)) }
            .padding(start = 8.dp),
    )
}

@Composable
private fun TarjetaRecordApuestas(tasaVictoria: Int, victorias: Int, derrotas: Int) {
    val colorTasa = if (tasaVictoria >= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(20.dp),
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("TASA DE VICTORIA", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("RÉCORD", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            DonutTasaVictoria(tasaVictoria, colorTasa)
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("${victorias}V", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text(" - ", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${derrotas}D", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                }
                Text("Temporada actual", style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DonutTasaVictoria(tasa: Int, color: Color) {
    val colorFondo = MaterialTheme.colorScheme.surfaceContainerHighest
    // Antes la dona aparecía ya "llena" de golpe, sin ninguna animación — el
    // arco crece desde 0 y el porcentaje cuenta hacia arriba, como un
    // scoreboard real.
    val progreso = remember { Animatable(0f) }
    LaunchedEffect(tasa) { progreso.animateTo(tasa / 100f, animationSpec = tween(900, easing = FastOutSlowInEasing)) }
    Box(modifier = Modifier.size(128.dp), contentAlignment = Alignment.Center) {
        // Resplandor solo cuando la tasa va ganadora (≥50%) — para que
        // coincida con "vas ganando" y no aparezca vacío en un 0%.
        if (tasa >= 50) {
            ResplandorRadial(color = color, tamaño = 150.dp, alpha = 0.3f)
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val grosor = size.width * 0.16f
            drawArc(color = colorFondo, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(grosor))
            drawArc(color = color, startAngle = -90f, sweepAngle = 360f * progreso.value, useCenter = false, style = Stroke(grosor, cap = StrokeCap.Round))
        }
        Text("${(progreso.value * 100).roundToInt()}%", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = color)
    }
}

/** Mismo grid de la plantilla: fondo neutro + un borde inferior sólido del
 * color de la estadística (en vez de un borde parejo alrededor). */
@Composable
private fun EstadisticaReporte(etiqueta: String, valor: String, colorValor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .drawBehind {
                drawRect(
                    color = colorValor,
                    topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - 3.dp.toPx()),
                    size = Size(size.width, 3.dp.toPx()),
                )
            }
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(etiqueta, style = CapsLabelTextStyle.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(valor, style = NumericTextStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = colorValor, textAlign = TextAlign.Center)
    }
}

/**
 * Tile de racha aparte de `EstadisticaReporte`: antes el número y la palabra
 * "GANANDO"/"PERDIENDO" iban en un solo `Text` con salto de línea manual y
 * tipografía numérica de 20sp — la palabra no entraba en el ancho del tile y
 * se cortaba en dos líneas más ("PERDIEND" / "O"). Acá van en dos `Text`
 * separados, el número grande y la palabra en una etiqueta caps chica que sí
 * entra. El color también dejó de estar fijo en `tertiary` — ahora sigue el
 * signo real de la racha (primary si se está ganando).
 */
@Composable
private fun EstadisticaRacha(racha: RachaApuestas?, modifier: Modifier = Modifier) {
    val color = when (racha?.ganando) {
        true -> MaterialTheme.colorScheme.primary
        false -> MaterialTheme.colorScheme.tertiary
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .drawBehind {
                drawRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(0f, size.height - 3.dp.toPx()),
                    size = Size(size.width, 3.dp.toPx()),
                )
            }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("RACHA", style = CapsLabelTextStyle.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(racha?.cantidad?.toString() ?: "—", style = NumericTextStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = color, textAlign = TextAlign.Center)
        if (racha != null) {
            Text(
                if (racha.ganando) "GANANDO" else "PERDIENDO",
                style = CapsLabelTextStyle.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                color = color,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }
    }
}

/** Racha acumulada neta (+1 gané/-1 perdí, corrida) de las últimas apuestas
 * derivables — un dato real, no inventado, ya que sin fichas no hay ningún
 * "monto" que graficar. El color de cada barra es el resultado de ESA
 * apuesta puntual (se deriva de la diferencia con la barra anterior). */
@Composable
private fun GraficoEvolucionApuestas(valores: List<Int>, modifier: Modifier = Modifier) {
    val colorGanancia = MaterialTheme.colorScheme.primary
    val colorPerdida = MaterialTheme.colorScheme.tertiary
    val maxAbs = valores.maxOf { abs(it) }.coerceAtLeast(1)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        valores.forEachIndexed { i, v ->
            val anterior = if (i == 0) 0 else valores[i - 1]
            val gane = v > anterior
            val alturaObjetivo = (abs(v).toFloat() / maxAbs).coerceIn(0.15f, 1f)
            // Cada barra crece desde 0 con un pequeño retraso escalonado por
            // índice — antes aparecían todas ya crecidas de golpe.
            val alturaAnimada = remember(v, i) { Animatable(0f) }
            LaunchedEffect(v, i) {
                delay(i * 70L)
                alturaAnimada.animateTo(alturaObjetivo, animationSpec = tween(500, easing = FastOutSlowInEasing))
            }
            Box(
                modifier = Modifier
                    .width(20.dp)
                    .fillMaxHeight(alturaAnimada.value.coerceAtLeast(0.02f))
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(if (gane) colorGanancia else colorPerdida),
            )
        }
    }
}

@Composable
private fun FilaRivalFrecuente(rival: RivalFrecuenteUi) {
    val colorFranja = MaterialTheme.colorScheme.primary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .drawBehind { drawRect(color = colorFranja, size = Size(4.dp.toPx(), size.height)) }
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 16.dp),
    ) {
        Avatar(rival.fotoUrl, tamaño = 64.dp)
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(rival.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(
                if (rival.totalApuestas == 1) "1 apuesta total" else "${rival.totalApuestas} apuestas totales",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (rival.resultado != null) {
            val (victorias, derrotas) = rival.resultado
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${victorias}V", style = NumericTextStyle.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                Text(" / ", style = NumericTextStyle.copy(fontSize = 15.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${derrotas}D", style = NumericTextStyle.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

private data class EstiloPillApuesta(val colorFranja: Color, val colorFondoPill: Color, val texto: String, val colorTexto: Color)

@Composable
private fun estiloDe(estado: EstadoApuestaUi): EstiloPillApuesta = when (estado) {
    EstadoApuestaUi.GANASTE -> EstiloPillApuesta(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary, "GANASTE", MaterialTheme.colorScheme.onPrimary)
    EstadoApuestaUi.PERDISTE -> EstiloPillApuesta(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiary, "PERDISTE", MaterialTheme.colorScheme.onTertiary)
    EstadoApuestaUi.SALDADO -> EstiloPillApuesta(MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.surfaceContainerHighest, "SALDADO", MaterialTheme.colorScheme.onSurfaceVariant)
    EstadoApuestaUi.PENDIENTE -> EstiloPillApuesta(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondaryContainer, "PENDIENTE", MaterialTheme.colorScheme.onSecondaryContainer)
}

@Composable
private fun FilaApuesta(apuesta: ApuestaUi) {
    val estilo = estiloDe(apuesta.estado)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .drawBehind { drawRect(color = estilo.colorFranja, size = Size(4.dp.toPx(), size.height)) }
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(apuesta.otroNombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(" · ${apuesta.fecha}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
            Spacer(Modifier.height(2.dp))
            DescripcionApuesta(apuesta.descripcion)
        }
        Text(
            estilo.texto,
            style = CapsLabelTextStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = estilo.colorTexto,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(estilo.colorFondoPill)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

private val patronMonto = Regex("^\\s*[Ss]/\\.?\\s*\\d")

/** Si la descripción es un monto ("S/ 50.00") se muestra en tipografía
 * numérica destacada, igual que la plantilla — cualquier otra cosa (comida,
 * bebida, favores) se muestra con un ícono genérico + cursiva. */
@Composable
private fun DescripcionApuesta(descripcion: String) {
    if (patronMonto.containsMatchIn(descripcion)) {
        Text(descripcion, style = NumericTextStyle.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Handshake, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                "\"$descripcion\"",
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun Avatar(fotoUrl: String?, tamaño: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier.size(tamaño).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        val foto = fotoUrl.urlCompletaFoto()
        if (foto != null) {
            AsyncImage(model = foto, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(CircleShape))
        } else {
            Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(tamaño * 0.45f))
        }
    }
}

/** Silueta de la tarjeta récord (dona + récord), los 3 stat-tiles y el
 * gráfico de barras — los elementos más "gráficos" del reporte, donde un
 * spinner genérico se nota más que en una pantalla de solo texto. */
@Composable
private fun SkeletonReporte(padding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        SkeletonBox(Modifier.fillMaxWidth().height(160.dp), forma = RoundedCornerShape(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(3) { SkeletonBox(Modifier.weight(1f).height(64.dp), forma = RoundedCornerShape(12.dp)) }
        }
        SkeletonBox(Modifier.fillMaxWidth().height(140.dp), forma = RoundedCornerShape(16.dp))
        SkeletonFilaCard()
    }
}
