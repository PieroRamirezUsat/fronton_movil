package com.example.aplicacion_fronton.ui.retos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsTennis
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.model.dto.EstadoVersus
import com.example.aplicacion_fronton.model.dto.Modalidad
import com.example.aplicacion_fronton.model.dto.RankingEntryDto
import com.example.aplicacion_fronton.model.dto.ReportarResultadoDto
import com.example.aplicacion_fronton.model.dto.VersusDto
import com.example.aplicacion_fronton.model.dto.yaComenzoElVersus
import com.example.aplicacion_fronton.model.dto.yaPasoUnaHoraDesde
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.BotonVolver
import com.example.aplicacion_fronton.ui.componentes.SeccionEnCascada
import com.example.aplicacion_fronton.ui.componentes.rememberInteraccionPresionable
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle
import kotlinx.coroutines.launch

private val mesesAbrevDetalle = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")
private val diasAbrev = listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM")

private fun formatearFechaLarga(iso: String): String = try {
    val fecha = iso.substringBefore("T")
    val hora = iso.substringAfter("T").take(5)
    val partes = fecha.split("-")
    val (anio, mes, dia) = Triple(partes[0].toInt(), partes[1].toInt(), partes[2].toInt())
    // Zeller-ish simplificado: sin librería de fechas (minSdk 24, sin desugaring),
    // así que se omite el nombre del día si no vale la pena la complejidad.
    "$dia ${mesesAbrevDetalle[mes - 1]} $anio - $hora"
} catch (e: Exception) {
    iso
}

@Composable
fun DetalleVersusScreen(
    versusId: Int,
    onVolver: () -> Unit,
    onReportarMarcador: (Int) -> Unit,
    onRegistrarCompromiso: (Int) -> Unit = {},
    onVerPerfil: (RankingEntryDto) -> Unit = {},
) {
    var versus by remember { mutableStateOf<VersusDto?>(null) }
    var miId by remember { mutableStateOf<Int?>(null) }
    var jugadores by remember { mutableStateOf<List<RankingEntryDto>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var respondiendo by remember { mutableStateOf(false) }
    var errorRespuesta by remember { mutableStateOf<String?>(null) }
    var accionCancelacion by remember { mutableStateOf(false) }
    var errorCancelacion by remember { mutableStateOf<String?>(null) }
    var declarandoInasistencia by remember { mutableStateOf(false) }
    var errorInasistencia by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun responder(aceptar: Boolean) {
        scope.launch {
            respondiendo = true
            errorRespuesta = null
            val resultado = safeApiCall { RetrofitClient.versusService.responder(versusId, aceptar) }
            when (resultado) {
                // Se actualiza el versus en memoria con la respuesta del backend
                // en vez de navegar a otro lado — así la misma pantalla pasa de
                // "pendiente" a "aceptado" (o rechazado) al toque, sin perder el
                // contexto que el usuario ya estaba mirando.
                is ApiResult.Exito -> versus = resultado.datos
                is ApiResult.Error -> errorRespuesta = resultado.mensaje
            }
            respondiendo = false
        }
    }

    fun solicitarCancelacion() {
        scope.launch {
            accionCancelacion = true
            errorCancelacion = null
            val resultado = safeApiCall { RetrofitClient.versusService.solicitarCancelacion(versusId) }
            when (resultado) {
                is ApiResult.Exito -> versus = resultado.datos
                is ApiResult.Error -> errorCancelacion = resultado.mensaje
            }
            accionCancelacion = false
        }
    }

    fun rechazarCancelacion() {
        scope.launch {
            accionCancelacion = true
            errorCancelacion = null
            val resultado = safeApiCall { RetrofitClient.versusService.rechazarCancelacion(versusId) }
            when (resultado) {
                is ApiResult.Exito -> versus = resultado.datos
                is ApiResult.Error -> errorCancelacion = resultado.mensaje
            }
            accionCancelacion = false
        }
    }

    fun declararInasistencia() {
        scope.launch {
            declarandoInasistencia = true
            errorInasistencia = null
            val resultado = safeApiCall {
                RetrofitClient.versusService.reportarResultado(
                    versusId,
                    ReportarResultadoDto(sets_propios = 2, sets_rival = 0, inasistencia_rival = true),
                )
            }
            when (resultado) {
                is ApiResult.Exito -> {
                    val actualizado = safeApiCall { RetrofitClient.versusService.obtenerVersus(versusId) }
                    if (actualizado is ApiResult.Exito) versus = actualizado.datos
                }
                is ApiResult.Error -> errorInasistencia = resultado.mensaje
            }
            declarandoInasistencia = false
        }
    }

    LaunchedEffect(versusId) {
        cargando = true
        val perfil = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
        if (perfil is ApiResult.Exito) miId = perfil.datos.id

        val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
        if (ranking is ApiResult.Exito) jugadores = ranking.datos

        val resultado = safeApiCall { RetrofitClient.versusService.obtenerVersus(versusId) }
        when (resultado) {
            is ApiResult.Exito -> versus = resultado.datos
            is ApiResult.Error -> error = resultado.mensaje
        }
        cargando = false
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
                    Text("DETALLE DEL RETO", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
            }
        },
    ) { paddingInterno ->
        when {
            cargando -> Box(Modifier.fillMaxSize().padding(paddingInterno), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null || versus == null -> Box(Modifier.fillMaxSize().padding(paddingInterno).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(error ?: "No se pudo cargar este reto.", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            else -> ContenidoDetalle(
                versus = versus!!,
                miId = miId,
                jugadores = jugadores,
                padding = paddingInterno,
                onReportarMarcador = { onReportarMarcador(versusId) },
                onRegistrarCompromiso = { onRegistrarCompromiso(versusId) },
                respondiendo = respondiendo,
                errorRespuesta = errorRespuesta,
                onAceptar = { responder(true) },
                onRechazar = { responder(false) },
                onVerPerfil = onVerPerfil,
                accionCancelacion = accionCancelacion,
                errorCancelacion = errorCancelacion,
                onSolicitarCancelacion = { solicitarCancelacion() },
                onAceptarCancelacion = { solicitarCancelacion() },
                onRechazarCancelacion = { rechazarCancelacion() },
                declarandoInasistencia = declarandoInasistencia,
                errorInasistencia = errorInasistencia,
                onDeclararInasistencia = { declararInasistencia() },
            )
        }
    }
}

@Composable
private fun ContenidoDetalle(
    versus: VersusDto,
    miId: Int?,
    jugadores: List<RankingEntryDto>,
    padding: PaddingValues,
    onReportarMarcador: () -> Unit,
    onRegistrarCompromiso: () -> Unit,
    respondiendo: Boolean,
    errorRespuesta: String?,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    onVerPerfil: (RankingEntryDto) -> Unit = {},
    accionCancelacion: Boolean = false,
    errorCancelacion: String? = null,
    onSolicitarCancelacion: () -> Unit = {},
    onAceptarCancelacion: () -> Unit = {},
    onRechazarCancelacion: () -> Unit = {},
    declarandoInasistencia: Boolean = false,
    errorInasistencia: String? = null,
    onDeclararInasistencia: () -> Unit = {},
) {
    val jugador1 = jugadores.find { it.id == versus.jugador1_id }
    val jugador2 = jugadores.find { it.id == versus.jugador2_id }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
      SeccionEnCascada(visible, retraso = 0) {
        Column {
        // Cartel de pelea
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            JugadorCartel(
                jugador1,
                versus.jugador1_id,
                MaterialTheme.colorScheme.primary,
                Modifier.weight(1f),
                onClick = jugador1?.let { { onVerPerfil(it) } },
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 24.dp)) {
                Text(
                    "VS",
                    style = MaterialTheme.typography.headlineLarge.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            JugadorCartel(
                jugador2,
                versus.jugador2_id,
                MaterialTheme.colorScheme.secondary,
                Modifier.weight(1f),
                onClick = jugador2?.let { { onVerPerfil(it) } },
            )
        }

        Spacer(Modifier.height(24.dp))
        BadgeEstado(versus.estado)

        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FilaInfo(Icons.Filled.CalendarToday, "FECHA Y HORA", formatearFechaLarga(versus.fecha_hora))
            FilaInfo(Icons.Filled.LocationOn, "UBICACIÓN", versus.cancha ?: "Sin definir")
            FilaInfo(
                Icons.Filled.SportsTennis,
                "MODALIDAD",
                if (versus.modalidad == Modalidad.DOBLES) "Dobles" else "Individual",
            )
        }

        // Antes esta pantalla no tenía ninguna forma de aceptar/rechazar un
        // reto — esa acción vivía solo en las tarjetas de la bandeja de Retos.
        // Eso dejaba un callejón sin salida real: la animación de "duelo" al
        // recibir un reto lleva justo acá con "Ver reto", y el usuario no
        // encontraba cómo aceptar. Ahora, si el reto está pendiente y sos el
        // retado (jugador2 — el único que el backend deja responder), la
        // decisión se toma directo en el detalle, con todo el contexto a la
        // vista.
        val puedoResponder = miId != null && miId == versus.jugador2_id && versus.estado == EstadoVersus.PENDIENTE
        if (puedoResponder) {
            Spacer(Modifier.height(24.dp))
            Text("¿ACEPTÁS ESTE RETO?", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val (interaccionRechazar, escalaRechazar) = rememberInteraccionPresionable()
                OutlinedButton(
                    onClick = onRechazar,
                    enabled = !respondiendo,
                    shape = RoundedCornerShape(12.dp),
                    interactionSource = interaccionRechazar,
                    modifier = Modifier.weight(1f).height(48.dp).scale(escalaRechazar),
                ) { Text("RECHAZAR", style = CapsLabelTextStyle) }
                // BotonTactil mide altura + alturaSombra (48+4=52dp) — mismo
                // total que el OutlinedButton de al lado (48dp + su propio
                // borde de 1dp más el padding por defecto de M3 suma un total
                // visual equivalente). Antes quedaba con la altura por
                // defecto (52+4=56dp) y sin alinear verticalmente, así que se
                // veía más grande y "flotando" más abajo que Rechazar.
                BotonTactil(
                    texto = "Aceptar",
                    icono = Icons.Filled.CheckCircle,
                    onClick = onAceptar,
                    cargando = respondiendo,
                    enabled = !respondiendo,
                    saltoElastico = true,
                    colorContenedor = MaterialTheme.colorScheme.primary,
                    altura = 48.dp,
                    modifier = Modifier.weight(1f),
                )
            }
            if (errorRespuesta != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorRespuesta, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        } else if (versus.estado == EstadoVersus.PENDIENTE && miId == versus.jugador1_id) {
            Spacer(Modifier.height(16.dp))
            TextoEstadoSecundario("Esperando que ${jugador2?.nombre ?: "tu rival"} responda el reto.")
        }

        // Cancelación de mutuo acuerdo — sirve también para "reprogramar"
        // (cancelan y arman un reto nuevo con la fecha que acuerden por
        // fuera) y para rechazar un reto que ya se había aceptado. Solo los
        // dos jugadores designados pueden actuar acá, nunca su pareja de
        // dobles — mismo criterio que aceptar/rechazar y reportar marcador.
        val esJugadorDesignadoParaCancelar = miId != null && (versus.jugador1_id == miId || versus.jugador2_id == miId)
        if (esJugadorDesignadoParaCancelar && versus.estado == EstadoVersus.ACEPTADO) {
            val rivalNombre = (if (miId == versus.jugador1_id) jugador2?.nombre else jugador1?.nombre) ?: "tu rival"
            when (versus.cancelacion_solicitada_por) {
                null -> {
                    Spacer(Modifier.height(24.dp))
                    val (interaccionCancelar, escalaCancelar) = rememberInteraccionPresionable()
                    OutlinedButton(
                        onClick = onSolicitarCancelacion,
                        enabled = !accionCancelacion,
                        shape = RoundedCornerShape(12.dp),
                        interactionSource = interaccionCancelar,
                        modifier = Modifier.fillMaxWidth().height(48.dp).scale(escalaCancelar),
                    ) { Text("CANCELAR / REPROGRAMAR RETO", style = CapsLabelTextStyle) }
                }
                miId -> {
                    Spacer(Modifier.height(16.dp))
                    TextoEstadoSecundario("Esperando que $rivalNombre acepte cancelar el reto.")
                }
                else -> {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "$rivalNombre propuso cancelar este reto",
                        style = CapsLabelTextStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val (interaccionMantener, escalaMantener) = rememberInteraccionPresionable()
                        OutlinedButton(
                            onClick = onRechazarCancelacion,
                            enabled = !accionCancelacion,
                            shape = RoundedCornerShape(12.dp),
                            interactionSource = interaccionMantener,
                            modifier = Modifier.weight(1f).height(48.dp).scale(escalaMantener),
                        ) { Text("MANTENER RETO", style = CapsLabelTextStyle) }
                        BotonTactil(
                            texto = "Aceptar cancelación",
                            onClick = onAceptarCancelacion,
                            cargando = accionCancelacion,
                            enabled = !accionCancelacion,
                            colorContenedor = MaterialTheme.colorScheme.error,
                            altura = 48.dp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            if (errorCancelacion != null) {
                Spacer(Modifier.height(8.dp))
                Text(errorCancelacion, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            // Solo aparece si el rival nunca llegó a reportar nada (todavía
            // ACEPTADO) y ya pasó 1 hora de la hora pactada — el backend
            // vuelve a chequear las dos cosas igual, esto es solo para no
            // mostrar un botón que va a fallar.
            if (versus.fecha_hora.yaPasoUnaHoraDesde()) {
                var mostrarDialogoInasistencia by remember { mutableStateOf(false) }
                Spacer(Modifier.height(12.dp))
                val (interaccionInasistencia, escalaInasistencia) = rememberInteraccionPresionable()
                OutlinedButton(
                    onClick = { mostrarDialogoInasistencia = true },
                    enabled = !declarandoInasistencia,
                    shape = RoundedCornerShape(12.dp),
                    interactionSource = interaccionInasistencia,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth().height(48.dp).scale(escalaInasistencia),
                ) { Text("DECLARAR VICTORIA POR INASISTENCIA", style = CapsLabelTextStyle) }
                if (errorInasistencia != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorInasistencia, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                if (mostrarDialogoInasistencia) {
                    AlertDialog(
                        onDismissRequest = { mostrarDialogoInasistencia = false },
                        title = { Text("¿Declarar victoria por inasistencia?") },
                        text = { Text("$rivalNombre no se presentó al reto programado. Esto confirma el partido a tu favor y actualiza el Elo de los dos — si no es correcto, resuélvanlo entre ustedes antes de confirmar.") },
                        confirmButton = {
                            TextButton(onClick = { mostrarDialogoInasistencia = false; onDeclararInasistencia() }) {
                                Text("Confirmar", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { mostrarDialogoInasistencia = false }) { Text("Cancelar") }
                        },
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("RESULTADO", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        TarjetaMarcador(versus)

        // "Jugador designado" (jugador1_id/jugador2_id) — los dos que armaron el
        // reto — es lo único que el backend deja reportar el marcador, ni
        // siquiera a su propia pareja de dobles (POST /versus/{id}/resultado
        // exige jugador1_id/jugador2_id exactos). "Participante" en cambio es
        // el equipo completo (incluye pareja1_id/pareja2_id) — antes esta
        // pantalla usaba la versión angosta para AMBAS cosas, así que en un
        // versus de dobles la pareja (que sí juega el partido) se veía tratada
        // como espectador ajeno: el botón le decía "APOSTAR CON OTRO USUARIO"
        // en vez de "REGISTRAR COMPROMISO", igual que a alguien que ni siquiera
        // juega este versus.
        val esJugadorDesignado = miId != null && (versus.jugador1_id == miId || versus.jugador2_id == miId)
        val esParejaNoDesignada = miId != null && !esJugadorDesignado && (versus.pareja1_id == miId || versus.pareja2_id == miId)
        val soyParticipante = esJugadorDesignado || esParejaNoDesignada
        // En disputa, cualquiera de los dos puede volver a reportar para corregir
        // su marcador — por eso acá no se exige "!mi_reporte_enviado" como en
        // ACEPTADO/JUGADO (donde ya reportar una vez basta y hay que esperar).
        val puedoReportar = esJugadorDesignado && when (versus.estado) {
            EstadoVersus.ACEPTADO -> true
            EstadoVersus.JUGADO -> !versus.mi_reporte_enviado
            EstadoVersus.DISPUTA -> true
            else -> false
        }

        if (puedoReportar) {
            BotonTactil(
                texto = if (versus.estado == EstadoVersus.DISPUTA) "Corregir marcador" else "Reportar marcador",
                icono = Icons.Filled.SportsTennis,
                onClick = onReportarMarcador,
                colorContenedor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            )
            if (versus.estado == EstadoVersus.DISPUTA) {
                TextoEstadoSecundario("Los marcadores reportados no coincidieron. Conversen cuál fue el resultado real y vuelvan a reportarlo — se confirma en cuanto ambos coincidan.")
            }
        } else if (versus.mi_reporte_enviado && versus.estado == EstadoVersus.JUGADO) {
            TextoEstadoSecundario("Ya enviaste tu marcador — esperando el reporte de tu rival.")
        } else if (esParejaNoDesignada && versus.estado in listOf(EstadoVersus.ACEPTADO, EstadoVersus.JUGADO, EstadoVersus.DISPUTA)) {
            val companero = if (versus.pareja1_id == miId) jugador1?.nombre else jugador2?.nombre
            TextoEstadoSecundario("El marcador lo reporta ${companero ?: "tu compañero de equipo"}, quien armó el reto — vos podés seguir el resultado acá.")
        }

        // El compromiso (qué se apuesta) solo tiene sentido mientras el
        // resultado todavía no se sabe — nunca sobre uno ya CONFIRMADO (ya se
        // sabe quién ganó, alguien podría "apostar" con ventaja) ni una vez
        // que el partido ya comenzó, aunque el marcador siga sin reportar
        // (JUGADO, en este modelo, significa que uno de los dos ya lo jugó y
        // reportó su lado — no es "en vivo"). El backend rechaza cualquier
        // otro caso (`POST /compromisos`); este chequeo es solo para no
        // mostrar un botón que siempre va a fallar.
        // Un espectador (ninguno de los dos jugadores) también puede registrar
        // un compromiso: apostando con otro espectador sobre el resultado de
        // un partido ajeno — el backend ya lo permite (GET /versus/{id} y
        // POST /compromisos no exigen ser participante).
        val puedeComprometerse = versus.estado in listOf(EstadoVersus.ACEPTADO, EstadoVersus.DISPUTA) &&
            !versus.fecha_hora.yaComenzoElVersus()
        if (puedeComprometerse) {
            val (interaccionCompromiso, escalaCompromiso) = rememberInteraccionPresionable()
            OutlinedButton(
                onClick = onRegistrarCompromiso,
                shape = RoundedCornerShape(12.dp),
                interactionSource = interaccionCompromiso,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(52.dp).scale(escalaCompromiso),
            ) {
                Icon(Icons.Filled.Handshake, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                // Para un jugador, "compromiso" ya es un término conocido de la
                // app. Para un espectador que llegó por primera vez a este
                // partido ajeno, esa palabra no comunica nada — necesita algo
                // que se lea directo como "acá apuesto con otra persona".
                Text(
                    if (soyParticipante) "REGISTRAR APUESTA" else "APOSTAR CON OTRO USUARIO",
                    style = CapsLabelTextStyle,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        }
      }
    }
}

@Composable
private fun JugadorCartel(
    jugador: RankingEntryDto?,
    idFallback: Int,
    colorBorde: Color,
    modifier: Modifier,
    onClick: (() -> Unit)? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .border(4.dp, colorBorde, CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            val foto = jugador?.foto_url.urlCompletaFoto()
            if (foto != null) {
                AsyncImage(model = foto, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(CircleShape))
            } else {
                Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(36.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            (jugador?.nombre ?: "Jugador #$idFallback").uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        if (jugador != null) {
            Text("${jugador.elo} PTS", style = NumericTextStyle.copy(fontSize = 13.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BadgeEstado(estado: EstadoVersus) {
    val (color, texto) = when (estado) {
        EstadoVersus.PENDIENTE -> MaterialTheme.colorScheme.secondary to "PENDIENTE DE RESPUESTA"
        EstadoVersus.ACEPTADO -> MaterialTheme.colorScheme.primary to "ACEPTADO"
        EstadoVersus.RECHAZADO -> MaterialTheme.colorScheme.error to "RECHAZADO"
        EstadoVersus.JUGADO -> MaterialTheme.colorScheme.secondary to "MARCADOR PARCIAL"
        EstadoVersus.CONFIRMADO -> MaterialTheme.colorScheme.primary to "CONFIRMADO"
        EstadoVersus.DISPUTA -> MaterialTheme.colorScheme.error to "EN DISPUTA"
        EstadoVersus.CANCELADO -> MaterialTheme.colorScheme.outline to "CANCELADO"
    }
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(vertical = 8.dp),
    ) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, style = CapsLabelTextStyle.copy(fontWeight = FontWeight.Bold), color = color)
    }
}

@Composable
private fun FilaInfo(icono: androidx.compose.ui.graphics.vector.ImageVector, etiqueta: String, valor: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(etiqueta, style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valor, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun TarjetaMarcador(versus: VersusDto) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)))
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (versus.sets_jugador1 != null && versus.sets_jugador2 != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(versus.sets_jugador1.toString(), style = NumericTextStyle.copy(fontSize = 56.sp, fontWeight = FontWeight.Bold), color = Color.White)
                Box(modifier = Modifier.width(2.dp).height(48.dp).background(Color.White.copy(alpha = 0.5f)))
                Text(versus.sets_jugador2.toString(), style = NumericTextStyle.copy(fontSize = 56.sp, fontWeight = FontWeight.Bold), color = Color.White.copy(alpha = 0.9f))
            }
        } else {
            Text(
                "Todavía no hay marcador reportado.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
    }
}

@Composable
private fun TextoEstadoSecundario(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
    )
}
