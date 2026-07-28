package com.example.aplicacion_fronton.ui.perfil

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.Stadium
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.model.dto.CategoriaEdad
import com.example.aplicacion_fronton.model.dto.CumplimientoDto
import com.example.aplicacion_fronton.model.dto.NivelCumplimiento
import com.example.aplicacion_fronton.model.dto.rivalIdPara
import com.example.aplicacion_fronton.model.dto.soyEquipoJugador1
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.BotonVolver
import com.example.aplicacion_fronton.ui.componentes.CargandoPelotita
import com.example.aplicacion_fronton.ui.componentes.TarjetaPartidoHistorial
import com.example.aplicacion_fronton.ui.retos.PartidoHistorialUi
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle

private val etiquetasCategoriaJugador = mapOf(
    CategoriaEdad.MENORES to "Menores",
    CategoriaEdad.LIBRE to "Libre",
    CategoriaEdad.MAS_40 to "+40",
    CategoriaEdad.MAS_50 to "+50",
    CategoriaEdad.MAS_60 to "+60",
)

/**
 * Perfil público de OTRO jugador (no el propio) — de solo lectura, sin editar
 * ni cerrar sesión. Se llega acá tocando una fila del Ranking. Réplica de
 * `perfil_de_jugador_historial_de_cumplimiento`: card de Elo con textura de
 * puntos ("pared de cancha"), chips de categoría/club, y la card de
 * cumplimiento — el propósito central es que el usuario vea el historial real
 * de esa persona antes de retar o comprometerse con ella.
 */
@Composable
fun PerfilJugadorScreen(
    datos: PerfilJugadorHolder.Datos,
    onVolver: () -> Unit,
    onRetar: () -> Unit = {},
    onVerVersus: (Int) -> Unit = {},
) {
    var cumplimiento by remember { mutableStateOf<CumplimientoDto?>(null) }
    var cargandoCumplimiento by remember { mutableStateOf(true) }
    var errorCumplimiento by remember { mutableStateOf<String?>(null) }
    var ultimosVersus by remember { mutableStateOf<List<PartidoHistorialUi>>(emptyList()) }
    var cargandoVersus by remember { mutableStateOf(true) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(datos.usuarioId) {
        cargandoCumplimiento = true
        val resultado = safeApiCall { RetrofitClient.usuariosService.obtenerCumplimiento(datos.usuarioId) }
        when (resultado) {
            is ApiResult.Exito -> cumplimiento = resultado.datos
            is ApiResult.Error -> errorCumplimiento = resultado.mensaje
        }
        cargandoCumplimiento = false
    }

    LaunchedEffect(datos.usuarioId) {
        cargandoVersus = true
        val versusResultado = safeApiCall { RetrofitClient.versusService.listarVersusDeJugador(datos.usuarioId) }
        val versusLista = (versusResultado as? ApiResult.Exito)?.datos.orEmpty()
        if (versusLista.isNotEmpty()) {
            val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val jugadores = (ranking as? ApiResult.Exito)?.datos.orEmpty()
            ultimosVersus = versusLista.mapNotNull { v ->
                val soyJugador1 = v.soyEquipoJugador1(datos.usuarioId)
                val misSets = if (soyJugador1) v.sets_jugador1 else v.sets_jugador2
                val susSets = if (soyJugador1) v.sets_jugador2 else v.sets_jugador1
                if (misSets == null || susSets == null) return@mapNotNull null
                val rivalId = v.rivalIdPara(datos.usuarioId)
                val rival = jugadores.firstOrNull { it.id == rivalId }
                PartidoHistorialUi(
                    versusId = v.id,
                    rivalNombre = rival?.nombre ?: "Jugador #$rivalId",
                    rivalFotoUrl = rival?.foto_url,
                    fechaHora = v.fecha_hora,
                    modalidad = v.modalidad,
                    misSets = misSets,
                    susSets = susSets,
                    gane = misSets > susSets,
                )
            }
        }
        cargandoVersus = false
    }

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
                    Text(
                        "PERFIL DEL JUGADOR",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
            }
        },
    ) { paddingInterno ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 6 },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingInterno)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .clip(CircleShape)
                            .border(3.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (datos.fotoUrl != null) {
                            AsyncImage(
                                model = datos.fotoUrl.urlCompletaFoto(),
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize().clip(CircleShape),
                            )
                        } else {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        datos.nombre.uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "CATEGORÍA ${etiquetasCategoriaJugador[datos.categoriaEdad]?.uppercase() ?: ""}",
                            style = CapsLabelTextStyle,
                            color = Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            if (datos.club != null) {
                                Icon(Icons.Filled.Stadium, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(
                                datos.club?.uppercase() ?: "JUGADOR LIBRE",
                                style = CapsLabelTextStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                TarjetaEloConTextura(datos)

                Spacer(Modifier.height(24.dp))
                Text(
                    "CUMPLIMIENTO DE APUESTAS",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                when {
                    cargandoCumplimiento -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CargandoPelotita()
                    }
                    errorCumplimiento != null -> Text(errorCumplimiento.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    cumplimiento != null -> TarjetaCumplimientoJugador(cumplimiento!!)
                }

                Spacer(Modifier.height(24.dp))
                Text("ÚLTIMOS RETOS", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                when {
                    cargandoVersus -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CargandoPelotita()
                    }
                    ultimosVersus.isEmpty() -> EstadoVacioVersus("Aún no hay partidos registrados para mostrar aquí.")
                    else -> {
                        // Antes se mostraban los hasta 10 partidos que trae el
                        // backend siempre — con historial real, la pantalla
                        // quedaba tapada de tarjetas. Mismo patrón ya usado en
                        // Notificaciones: 3 por defecto, botón para ver el resto
                        // de lo que ya se cargó (sin pedido extra al backend).
                        var verTodoElHistorial by remember { mutableStateOf(false) }
                        val limiteHistorial = 3
                        val versusVisibles = if (verTodoElHistorial) ultimosVersus else ultimosVersus.take(limiteHistorial)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            versusVisibles.forEach { partido ->
                                TarjetaPartidoHistorial(partido, onClick = { onVerVersus(partido.versusId) })
                            }
                            if (!verTodoElHistorial && ultimosVersus.size > limiteHistorial) {
                                TextButton(onClick = { verTodoElHistorial = true }, modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        "VER ${ultimosVersus.size - limiteHistorial} MÁS DEL HISTORIAL",
                                        style = CapsLabelTextStyle,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }

                BotonTactil(
                    texto = "Retar a un duelo",
                    icono = Icons.Filled.SportsTennis,
                    onClick = onRetar,
                    colorContenedor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                )
            }
        }
    }
}

@Composable
private fun TarjetaEloConTextura(datos: PerfilJugadorHolder.Datos) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
    ) {
        // Textura de "pared de cancha" — grilla de puntos al 5% de opacidad,
        // igual que el mockup (bg-[radial-gradient(#000_1px,transparent_1px)]).
        Canvas(modifier = Modifier.matchParentSize()) {
            val paso = 16.dp.toPx()
            var x = paso / 2
            while (x < size.width) {
                var y = paso / 2
                while (y < size.height) {
                    drawCircle(Color.Black.copy(alpha = 0.05f), radius = 1.dp.toPx(), center = Offset(x, y))
                    y += paso
                }
                x += paso
            }
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "RATING ELO ${if (datos.modalidad == "dobles") "DOBLES" else "INDIVIDUAL"}",
                    style = CapsLabelTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    datos.elo.toString(),
                    style = NumericTextStyle.copy(fontSize = 48.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("RANKING NACIONAL", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("#${datos.posicionRanking}", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun TarjetaCumplimientoJugador(cumplimiento: CumplimientoDto) {
    val (colorNivel, textoNivel) = when (cumplimiento.nivel) {
        NivelCumplimiento.CUMPLE_SIEMPRE -> MaterialTheme.colorScheme.primary to "CUMPLE SIEMPRE"
        NivelCumplimiento.CUMPLE_CASI_SIEMPRE -> MaterialTheme.colorScheme.secondary to "CUMPLE CASI SIEMPRE"
        NivelCumplimiento.HISTORIAL_INCUMPLIMIENTOS -> MaterialTheme.colorScheme.tertiary to "HISTORIAL DE INCUMPLIMIENTOS"
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Text(
            textoNivel,
            style = CapsLabelTextStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(colorNivel)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text("APUESTAS SALDADAS", style = CapsLabelTextStyle.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Text(cumplimiento.saldados.toString(), style = NumericTextStyle.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            }
            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.height(48.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text("APUESTAS SIN SALDAR", style = CapsLabelTextStyle.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Text(cumplimiento.incumplidos.toString(), style = NumericTextStyle.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

/** Misma franja "court-line" usada en Home — coherencia visual entre estados
 * vacíos de toda la app, en vez de una caja plana genérica. */
@Composable
private fun EstadoVacioVersus(texto: String) {
    val colorFranja = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .drawBehind {
                drawRect(color = colorFranja, size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height))
            }
            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.EventBusy, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
