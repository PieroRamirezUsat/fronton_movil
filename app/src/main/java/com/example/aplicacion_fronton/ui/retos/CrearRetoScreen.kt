package com.example.aplicacion_fronton.ui.retos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.model.dto.CategoriaEdad
import com.example.aplicacion_fronton.model.dto.Modalidad
import com.example.aplicacion_fronton.model.dto.RankingEntryDto
import com.example.aplicacion_fronton.model.dto.VersusCreateDto
import com.example.aplicacion_fronton.network.direccionDesdeCoordenadas
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.BotonVolver
import com.example.aplicacion_fronton.ui.componentes.DialogSelectorJugador
import com.example.aplicacion_fronton.ui.componentes.DialogoSelectorFecha
import com.example.aplicacion_fronton.ui.componentes.DialogoSelectorHora
import com.example.aplicacion_fronton.ui.componentes.LATITUD_CHICLAYO_DEFECTO
import com.example.aplicacion_fronton.ui.componentes.LONGITUD_CHICLAYO_DEFECTO
import com.example.aplicacion_fronton.ui.componentes.SelectorUbicacionDialog
import com.example.aplicacion_fronton.ui.componentes.TarjetaAlertaIncumplimiento
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle
import kotlinx.coroutines.launch

private val etiquetasCategoriaReto = mapOf(
    CategoriaEdad.MENORES to "Menores",
    CategoriaEdad.LIBRE to "Libre",
    CategoriaEdad.MAS_40 to "+40",
    CategoriaEdad.MAS_50 to "+50",
    CategoriaEdad.MAS_60 to "+60",
)

@Composable
fun CrearRetoScreen(
    rival: RetoHolder.Datos,
    onRetoEnviado: () -> Unit,
    onVolver: () -> Unit,
    viewModel: CrearRetoViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val jugadores by viewModel.jugadores.collectAsStateWithLifecycle()
    val miUsuario by viewModel.miUsuario.collectAsStateWithLifecycle()
    val incumplidosRival by viewModel.incumplidosRival.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(rival.rivalId) { viewModel.cargarCumplimientoRival(rival.rivalId) }

    var modalidad by remember { mutableStateOf(Modalidad.INDIVIDUAL) }
    var pareja by remember { mutableStateOf<RankingEntryDto?>(null) }
    var parejaRival by remember { mutableStateOf<RankingEntryDto?>(null) }
    var fecha by remember { mutableStateOf<String?>(null) }
    var hora by remember { mutableStateOf<String?>(null) }
    var mostrarSelectorFecha by remember { mutableStateOf(false) }
    var mostrarSelectorHora by remember { mutableStateOf(false) }
    var cancha by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf(LATITUD_CHICLAYO_DEFECTO) }
    var longitud by remember { mutableStateOf(LONGITUD_CHICLAYO_DEFECTO) }
    var ubicacionElegidaEnMapa by remember { mutableStateOf(false) }
    var mostrarSelectorMapa by remember { mutableStateOf(false) }
    var nota by remember { mutableStateOf("") }
    var apuesta by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var selectorAbierto by remember { mutableStateOf<String?>(null) } // "pareja" | "parejaRival" | null
    var autoRellenoHecho by remember { mutableStateOf(false) }

    LaunchedEffect(estado) {
        val actual = estado
        if (actual is CrearRetoState.Exito) {
            if (actual.advertenciaCompromiso != null) {
                android.widget.Toast.makeText(contexto, actual.advertenciaCompromiso, android.widget.Toast.LENGTH_LONG).show()
            }
            onRetoEnviado()
        }
    }

    // Auto-relleno con los compañeros habituales, si ambos jugadores los tienen
    // configurados — se hace una sola vez, para no pisar una elección manual.
    LaunchedEffect(jugadores, miUsuario) {
        if (autoRellenoHecho || jugadores.isEmpty() || miUsuario == null) return@LaunchedEffect
        autoRellenoHecho = true
        pareja = miUsuario?.pareja_habitual_id?.let { id -> jugadores.find { it.id == id } }
        val parejaHabitualRival = jugadores.find { it.id == rival.rivalId }?.pareja_habitual_id
        parejaRival = parejaHabitualRival?.let { id -> jugadores.find { it.id == id } }
    }

    val candidatos = remember(jugadores, miUsuario, pareja, parejaRival) {
        jugadores.filter { it.id != rival.rivalId && it.id != miUsuario?.id && it.id != pareja?.id && it.id != parejaRival?.id }
    }

    if (selectorAbierto != null) {
        DialogSelectorJugador(
            candidatos = candidatos,
            onSeleccionar = {
                if (selectorAbierto == "pareja") pareja = it else parejaRival = it
                selectorAbierto = null
            },
            onCerrar = { selectorAbierto = null },
        )
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
                    Text("NUEVO RETO", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
            }
        },
    ) { paddingInterno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
        ) {
            // Card resumen del rival — misma franja "court-line" del sistema.
            val colorFranja = MaterialTheme.colorScheme.tertiary
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .drawBehindFranja(colorFranja)
                    .padding(start = 20.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center,
                ) {
                    val foto = rival.rivalFotoUrl.urlCompletaFoto()
                    if (foto != null) {
                        AsyncImage(model = foto, contentDescription = rival.rivalNombre, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(CircleShape))
                    } else {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(rival.rivalNombre, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            "CATEGORÍA ${etiquetasCategoriaReto[rival.rivalCategoria]?.uppercase() ?: ""}",
                            style = CapsLabelTextStyle.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(rival.rivalElo.toString(), style = NumericTextStyle.copy(fontSize = 14.sp), color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            if (incumplidosRival > 0) {
                Spacer(Modifier.height(16.dp))
                TarjetaAlertaIncumplimiento(rival.rivalId, rival.rivalNombre, incumplidosRival)
            }

            Spacer(Modifier.height(24.dp))
            Text("MODALIDAD DE JUEGO", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(4.dp),
            ) {
                listOf(Modalidad.INDIVIDUAL to "Individual", Modalidad.DOBLES to "Dobles").forEach { (valor, etiqueta) ->
                    val activo = modalidad == valor
                    Text(
                        etiqueta,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activo) MaterialTheme.colorScheme.surfaceContainerLow else Color.Transparent)
                            .clickable { modalidad = valor }
                            .padding(vertical = 12.dp),
                    )
                }
            }

            AnimatedVisibility(
                visible = modalidad == Modalidad.DOBLES,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // El mockup solo pedía "mi compañero", pero el backend exige los 4
                    // jugadores de un dobles al crear el reto — se agrega también el
                    // compañero del rival, si no la creación del versus siempre fallaría.
                    SelectorCompañero("Elegir mi compañero", pareja) { selectorAbierto = "pareja" }
                    Spacer(Modifier.height(12.dp))
                    SelectorCompañero("Elegir compañero del rival", parejaRival) { selectorAbierto = "parejaRival" }
                }
            }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                CampoFechaHora(
                    etiqueta = "FECHA",
                    valor = fecha ?: "Seleccionar",
                    icono = Icons.Filled.CalendarToday,
                    modifier = Modifier.weight(1f),
                    onClick = { mostrarSelectorFecha = true },
                )
                CampoFechaHora(
                    etiqueta = "HORA",
                    valor = hora ?: "Seleccionar",
                    icono = Icons.Filled.Schedule,
                    modifier = Modifier.weight(1f),
                    onClick = { mostrarSelectorHora = true },
                )
            }
            // Los diálogos nativos de Android (`android.app.DatePickerDialog`/
            // `TimePickerDialog`) son vistas del sistema, ajenas al MaterialTheme
            // de la app — de ahí que se vieran desentonados. Estos dos son
            // Compose/Material3 (`ui/componentes/SelectorFechaHora.kt`), heredan
            // la paleta Frontón sola.
            if (mostrarSelectorFecha) {
                DialogoSelectorFecha(
                    fechaInicial = fecha,
                    onConfirmar = { fecha = it; mostrarSelectorFecha = false },
                    onCerrar = { mostrarSelectorFecha = false },
                )
            }
            if (mostrarSelectorHora) {
                DialogoSelectorHora(
                    horaInicial = hora,
                    onConfirmar = { hora = it; mostrarSelectorHora = false },
                    onCerrar = { mostrarSelectorHora = false },
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("CANCHA O LUGAR (OPCIONAL)", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextField(
                value = cancha,
                onValueChange = { cancha = it },
                placeholder = { Text("Ej: Rinconada, Country Club...") },
                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                singleLine = true,
                colors = camposSinCajaReto(),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))
            // Antes el mapa iba embebido en este mismo Column con scroll — el
            // gesto de arrastrar para mover el mapa competía con el scroll del
            // formulario y se volvía muy difícil de manejar. Ahora es un botón
            // que abre el mapa a pantalla completa (`SelectorUbicacionDialog`,
            // sin nada más con quien pelearse el gesto).
            OutlinedButton(
                onClick = { mostrarSelectorMapa = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (ubicacionElegidaEnMapa) "Ubicación marcada en el mapa — toca para cambiarla" else "Elegir la ubicación en el mapa",
                    style = CapsLabelTextStyle,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (mostrarSelectorMapa) {
                SelectorUbicacionDialog(
                    latitudInicial = latitud,
                    longitudInicial = longitud,
                    onCerrar = { mostrarSelectorMapa = false },
                    onConfirmar = { lat, lng ->
                        latitud = lat
                        longitud = lng
                        ubicacionElegidaEnMapa = true
                        mostrarSelectorMapa = false
                        // Se llena el campo de texto solo con la dirección legible
                        // — el usuario puede seguir editándolo a mano después si
                        // la dirección que resuelve el geocoder no queda del todo
                        // bien (nombres de canchas informales, etc.).
                        scope.launch {
                            direccionDesdeCoordenadas(contexto, lat, lng)?.let { direccion -> cancha = direccion }
                        }
                    },
                )
            }
            Text(
                "Al elegir la ubicación en el mapa, el campo de arriba se completa solo con la dirección.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            Spacer(Modifier.height(16.dp))
            Text("NOTA (OPCIONAL)", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextField(
                value = nota,
                onValueChange = { nota = it },
                placeholder = { Text("Algún mensaje para tu rival...") },
                leadingIcon = { Icon(Icons.Filled.EditNote, contentDescription = null) },
                singleLine = true,
                colors = camposSinCajaReto(),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            Text("¿QUÉ SE APUESTAN? (OPCIONAL)", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextField(
                value = apuesta,
                onValueChange = { if (it.length <= 280) apuesta = it },
                placeholder = { Text("Ej: una gaseosa, un cebiche, S/10") },
                leadingIcon = { Icon(Icons.Filled.Handshake, contentDescription = null) },
                minLines = 2,
                colors = camposSinCajaReto(),
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                "Este es un registro informal para el honor de los jugadores — se les pide confirmar el acuerdo antes de saldarlo.",
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            AnimatedVisibility(visible = error != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 12.dp))
            }
            AnimatedVisibility(visible = estado is CrearRetoState.Error, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Text(
                    (estado as? CrearRetoState.Error)?.mensaje.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }

            BotonTactil(
                texto = "Enviar reto",
                icono = Icons.AutoMirrored.Filled.Send,
                saltoElastico = true,
                cargando = estado is CrearRetoState.Cargando,
                onClick = {
                    error = when {
                        fecha == null || hora == null -> "Elige fecha y hora para el reto."
                        modalidad == Modalidad.DOBLES && pareja == null -> "Elige a tu compañero de dobles."
                        modalidad == Modalidad.DOBLES && parejaRival == null -> "Elige el compañero del rival."
                        else -> null
                    }
                    if (error == null) {
                        viewModel.enviarReto(
                            VersusCreateDto(
                                rival_id = rival.rivalId,
                                modalidad = modalidad,
                                // El auto-relleno de pareja habitual corre apenas cargan los
                                // datos, sin importar la modalidad activa en ese momento — si
                                // no se descarta acá, un reto individual manda un pareja_id
                                // "fantasma" y el backend lo rechaza (422 -> "Ocurrió un error
                                // inesperado" en el cliente, sin pista de la causa real).
                                pareja_id = if (modalidad == Modalidad.DOBLES) pareja?.id else null,
                                rival_pareja_id = if (modalidad == Modalidad.DOBLES) parejaRival?.id else null,
                                fecha_hora = "${fecha}T${hora}:00",
                                cancha = cancha.trim().ifBlank { null },
                                latitud = latitud,
                                longitud = longitud,
                                nota = nota.trim().ifBlank { null },
                            ),
                            apuesta = apuesta.trim().ifBlank { null },
                            invitadoIdApuesta = rival.rivalId,
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 24.dp),
            )
        }
    }
}

@Composable
private fun SelectorCompañero(etiqueta: String, elegido: RankingEntryDto?, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, if (elegido != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(elegido?.nombre ?: etiqueta, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    if (elegido != null) "Toca para cambiar" else "Invita a alguien de tu club",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CampoFechaHora(etiqueta: String, valor: String, icono: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier = modifier) {
        Text(etiqueta, style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(top = 8.dp, bottom = 8.dp),
        ) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(valor, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 2.dp)
    }
}

@Composable
internal fun camposSinCajaReto() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
)

private fun Modifier.drawBehindFranja(color: Color): Modifier = this.drawBehind {
    drawRect(color = color, size = Size(4.dp.toPx(), size.height))
}
