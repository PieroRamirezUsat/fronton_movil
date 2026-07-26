package com.example.aplicacion_fronton.ui.retos

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.model.dto.EstadoVersus
import com.example.aplicacion_fronton.model.dto.yaComenzoElVersus
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.BotonVolver
import com.example.aplicacion_fronton.ui.componentes.EstadoVacio
import com.example.aplicacion_fronton.ui.componentes.SkeletonListaFilas
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle
import java.util.Calendar

private val mesesAbrevBuscar = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")

private fun formatearFechaBuscar(iso: String): String = try {
    val fecha = iso.substringBefore("T")
    val hora = iso.substringAfter("T").take(5)
    val partes = fecha.split("-")
    "${partes[2]} ${mesesAbrevBuscar[partes[1].toInt() - 1]}, $hora"
} catch (e: Exception) {
    iso
}

private fun formatearHoraBuscar(iso: String): String = try {
    iso.substringAfter("T").take(5)
} catch (e: Exception) {
    iso
}

/** "HOY, 19:00" / "MAÑANA, 08:30" / fecha corta completa si está más lejos —
 * antes solo se mostraba la hora sola, sin decir qué día era. */
private fun formatearFechaRelativaBuscar(iso: String): String = when {
    esFecha(iso, 0) -> "HOY, ${formatearHoraBuscar(iso)}"
    esFecha(iso, 1) -> "MAÑANA, ${formatearHoraBuscar(iso)}"
    else -> formatearFechaBuscar(iso)
}

/** true si `iso` cae en el mismo día del año/mes/día que `offsetDias` respecto
 * a hoy (0 = hoy, 1 = mañana) — comparación de calendario, no de 24h exactas. */
private fun esFecha(iso: String, offsetDias: Int): Boolean = try {
    val partes = iso.substringBefore("T").split("-")
    val objetivo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offsetDias) }
    partes[0].toInt() == objetivo.get(Calendar.YEAR) &&
        partes[1].toInt() == objetivo.get(Calendar.MONTH) + 1 &&
        partes[2].toInt() == objetivo.get(Calendar.DAY_OF_MONTH)
} catch (e: Exception) {
    false
}

private enum class ChipVersus { HOY, MANANA, EN_VIVO }

@Composable
fun BuscarVersusScreen(
    onVolver: () -> Unit,
    onVerDetalle: (Int) -> Unit,
    onRegistrarCompromiso: (Int) -> Unit,
    viewModel: BuscarVersusViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var busqueda by remember { mutableStateOf("") }
    var chipActivo by remember { mutableStateOf<ChipVersus?>(null) }

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
                    Text("BUSCAR RETOS", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    TextField(
                        value = busqueda,
                        onValueChange = { busqueda = it },
                        placeholder = { Text("Buscar un reto...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                    ) {
                        ChipFiltroVersus("HOY", chipActivo == ChipVersus.HOY) {
                            chipActivo = if (chipActivo == ChipVersus.HOY) null else ChipVersus.HOY
                        }
                        ChipFiltroVersus("MAÑANA", chipActivo == ChipVersus.MANANA) {
                            chipActivo = if (chipActivo == ChipVersus.MANANA) null else ChipVersus.MANANA
                        }
                        ChipFiltroVersus("EN VIVO", chipActivo == ChipVersus.EN_VIVO, mostrarPulso = true) {
                            chipActivo = if (chipActivo == ChipVersus.EN_VIVO) null else ChipVersus.EN_VIVO
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
            }
        },
    ) { padding ->
        when (val actual = estado) {
            is BuscarVersusState.Cargando -> SkeletonListaFilas(padding)
            is BuscarVersusState.Error -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(actual.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            is BuscarVersusState.Exito -> {
                val filtrados = remember(actual.versus, busqueda, chipActivo) {
                    actual.versus.filter { item ->
                        val coincideTexto = busqueda.isBlank() ||
                            item.jugador1Nombre.contains(busqueda, ignoreCase = true) ||
                            item.jugador2Nombre.contains(busqueda, ignoreCase = true)
                        val coincideChip = when (chipActivo) {
                            null -> true
                            ChipVersus.EN_VIVO -> item.versus.estado == EstadoVersus.JUGADO
                            ChipVersus.HOY -> esFecha(item.versus.fecha_hora, 0)
                            ChipVersus.MANANA -> esFecha(item.versus.fecha_hora, 1)
                        }
                        coincideTexto && coincideChip
                    }
                }
                if (filtrados.isEmpty()) {
                    val hayFiltrosActivos = busqueda.isNotBlank() || chipActivo != null
                    // Antes esta pantalla no tenía NINGÚN botón en su estado
                    // vacío — si el usuario había filtrado por error, quedaba
                    // sin forma de saber que el filtro (no la falta de datos)
                    // era la causa. Cuando de verdad no hay ningún versus
                    // público, no hay ninguna acción útil que ofrecer acá.
                    EstadoVacio(
                        icono = Icons.Filled.Visibility,
                        titulo = if (hayFiltrosActivos) "SIN RESULTADOS" else "SIN RETOS PARA VER",
                        subtitulo = if (hayFiltrosActivos) {
                            "Ningún reto coincide con tu búsqueda o filtro. Prueba con otro criterio."
                        } else {
                            "Todavía no hay partidos de otros jugadores programados o en curso."
                        },
                        modifier = Modifier.padding(padding),
                        colorCirculo = MaterialTheme.colorScheme.surfaceContainerHighest,
                        colorIcono = MaterialTheme.colorScheme.onSurfaceVariant,
                        textoBoton = if (hayFiltrosActivos) "Quitar filtros" else null,
                        iconoBoton = Icons.Filled.FilterAltOff,
                        onBoton = { busqueda = ""; chipActivo = null },
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(filtrados, key = { it.versus.id }) { item ->
                            TarjetaVersusPublico(
                                item = item,
                                onClick = { onVerDetalle(item.versus.id) },
                                onRegistrarCompromiso = { onRegistrarCompromiso(item.versus.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipFiltroVersus(texto: String, activo: Boolean, mostrarPulso: Boolean = false, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (activo) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        if (mostrarPulso) {
            val transicion = rememberInfiniteTransition(label = "pulsoEnVivo")
            val alfa by transicion.animateFloat(
                initialValue = 1f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(tween(700, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
                label = "alfaPulsoEnVivo",
            )
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = alfa)),
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            texto,
            style = CapsLabelTextStyle,
            color = if (activo) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TarjetaVersusPublico(item: VersusPublicoUi, onClick: () -> Unit, onRegistrarCompromiso: () -> Unit) {
    val v = item.versus
    val enCurso = v.estado == EstadoVersus.JUGADO
    val colorFranja = if (enCurso) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .drawBehind { drawRect(color = colorFranja, size = Size(4.dp.toPx(), size.height)) }
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(start = 20.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            // La cancha va justo debajo de la etiqueta de estado, no al fondo
            // de la tarjeta junto a la fecha — es lo primero que identifica el
            // partido, igual que en la plantilla.
            Column(modifier = Modifier.weight(1f)) {
                if (enCurso) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val transicion = rememberInfiniteTransition(label = "pulsoEnCurso")
                        val alfa by transicion.animateFloat(
                            initialValue = 1f,
                            targetValue = 0.3f,
                            animationSpec = infiniteRepeatable(tween(700, easing = LinearOutSlowInEasing), RepeatMode.Reverse),
                            label = "alfaPulsoEnCurso",
                        )
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(MaterialTheme.colorScheme.error.copy(alpha = alfa)))
                        Spacer(Modifier.width(5.dp))
                        Text("EN CURSO", style = CapsLabelTextStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.tertiary)
                    }
                } else {
                    Text("PROGRAMADO", style = CapsLabelTextStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                }
                if (v.cancha != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(v.cancha, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }
            if (enCurso) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.inverseSurface).padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        "${v.sets_jugador1 ?: 0} - ${v.sets_jugador2 ?: 0}",
                        style = NumericTextStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                    )
                }
            } else {
                Text(
                    formatearFechaRelativaBuscar(v.fecha_hora),
                    style = NumericTextStyle.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceContainerHighest).padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            AvatarBuscar(item.jugador1Nombre, item.jugador1Foto, Modifier.weight(1f))
            Text("VS", style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic), color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(horizontal = 8.dp))
            AvatarBuscar(item.jugador2Nombre, item.jugador2Foto, Modifier.weight(1f))
        }

        // Ya no se puede apostar una vez que el partido comenzó — un versus
        // EN CURSO (JUGADO) en este modelo significa que alguno de los dos ya
        // reportó su marcador, así que ya efectivamente pasó (nunca es "en
        // vivo mientras juegan" de verdad). El botón solo tiene sentido para
        // un versus programado que todavía no llegó a su hora de inicio — el
        // backend rechaza cualquier otro caso, esto es solo para no mostrar
        // un botón que va a fallar siempre.
        val sePuedeApostar = !enCurso && !v.fecha_hora.yaComenzoElVersus()
        if (sePuedeApostar) {
            Spacer(Modifier.height(16.dp))
            BotonTactil(
                // Altura por defecto (52dp) -- BotonTactil usa un tamaño de letra
                // grande fijo (headlineSmall) sin importar la altura que se le
                // pida, así que forzarlo a 40dp (como estaba antes) apretaba el
                // texto largo y dejaba el ícono chico/cortado en el espacio que
                // sobraba. Con la altura por defecto se ve igual que el resto de
                // botones de la app.
                texto = "Apostar en este partido",
                icono = Icons.Filled.Handshake,
                onClick = onRegistrarCompromiso,
                colorContenedor = MaterialTheme.colorScheme.primaryContainer,
                colorTexto = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun AvatarBuscar(nombre: String, fotoUrl: String?, modifier: Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            val foto = fotoUrl.urlCompletaFoto()
            if (foto != null) {
                AsyncImage(model = foto, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(CircleShape))
            } else {
                Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(nombre.uppercase(), style = CapsLabelTextStyle.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1)
    }
}

