package com.example.aplicacion_fronton.ui.compromisos

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
import com.example.aplicacion_fronton.ui.componentes.CargandoPelotita
import com.example.aplicacion_fronton.ui.componentes.DialogSelectorJugador
import com.example.aplicacion_fronton.ui.componentes.TarjetaAlertaIncumplimiento
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle

@Composable
fun RegistrarCompromisoScreen(
    versusId: Int,
    onVolver: () -> Unit,
    onCompromisoRegistrado: () -> Unit,
    viewModel: RegistrarCompromisoViewModel = viewModel(),
) {
    val carga by viewModel.carga.collectAsStateWithLifecycle()
    val guardado by viewModel.guardado.collectAsStateWithLifecycle()

    LaunchedEffect(versusId) { viewModel.cargar(versusId) }
    LaunchedEffect(guardado) {
        if (guardado is GuardarCompromisoState.Guardado) onCompromisoRegistrado()
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
                    Text("APUESTA INFORMAL", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
            }
        },
    ) { padding ->
        when (val estado = carga) {
            is CargaCompromisoState.Cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CargandoPelotita()
            }
            is CargaCompromisoState.Error -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(estado.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            is CargaCompromisoState.Exito -> ContenidoRegistrar(
                estado = estado,
                guardado = guardado,
                padding = padding,
                onGuardar = { invitadoId, descripcion ->
                    viewModel.guardar(versusId, invitadoId, descripcion)
                },
            )
        }
    }
}

@Composable
private fun ContenidoRegistrar(
    estado: CargaCompromisoState.Exito,
    guardado: GuardarCompromisoState,
    padding: PaddingValues,
    onGuardar: (Int, String) -> Unit,
) {
    var descripcion by remember { mutableStateOf("") }
    var invitadoId by remember(estado) { mutableStateOf(estado.invitadoIdInicial) }
    var invitadoNombre by remember(estado) { mutableStateOf(estado.invitadoNombreInicial) }
    var invitadoFoto by remember(estado) { mutableStateOf(estado.invitadoFotoInicial) }
    var mostrarSelector by remember { mutableStateOf(false) }
    val v = estado.versus
    val soyJugador1 = v.jugador1_id == estado.miId || v.pareja1_id == estado.miId
    val misSets = if (estado.esEspectador) v.sets_jugador1 else if (soyJugador1) v.sets_jugador1 else v.sets_jugador2
    val susSets = if (estado.esEspectador) v.sets_jugador2 else if (soyJugador1) v.sets_jugador2 else v.sets_jugador1

    // Esta pantalla se alcanza desde un solo botón ya gateado en Detalle/Buscar
    // Versus, pero ese gate no alcanza como única defensa (mismo error ya
    // corregido antes con Reportar Marcador) — si alguien llega igual con un
    // link viejo o el versus cambió de estado mientras tenía la pantalla
    // abierta, el formulario no debe dejar guardar algo que el backend va a
    // rechazar de todas formas.
    val sePuedeApostar = v.estado == EstadoVersus.ACEPTADO && !v.fecha_hora.yaComenzoElVersus()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
    ) {
        // El encabezado refleja el estado real del versus — un compromiso solo
        // se puede CREAR mientras está ACEPTADO y no comenzó (ver
        // `sePuedeApostar`), pero esta pantalla también puede alcanzarse con un
        // versus que ya avanzó (link viejo, o cambió de estado mientras la
        // tenías abierta), así que el encabezado igual refleja la realidad en
        // vez de mentir "VERSUS PACTADO".
        val etiquetaEstado = when (v.estado) {
            EstadoVersus.CONFIRMADO -> "PARTIDO FINALIZADO"
            EstadoVersus.JUGADO -> "MARCADOR PARCIAL"
            EstadoVersus.DISPUTA -> "MARCADOR EN DISPUTA"
            else -> "RETO PACTADO"
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(16.dp),
        ) {
            Text(
                if (estado.esEspectador) "PARTIDO QUE ESTÁS MIRANDO — $etiquetaEstado" else etiquetaEstado,
                style = CapsLabelTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                JugadorCompromiso(
                    if (estado.esEspectador) estado.jugador1Nombre else estado.miNombre,
                    if (estado.esEspectador) estado.jugador1Foto else estado.miFotoUrl,
                    Modifier.weight(1f),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
                    if (misSets != null && susSets != null) {
                        Text("$misSets - $susSets", style = NumericTextStyle.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        Text("PUNTAJE", style = CapsLabelTextStyle.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("VS", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.tertiary)
                        Text("AÚN SIN JUGAR", style = CapsLabelTextStyle.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                JugadorCompromiso(
                    if (estado.esEspectador) estado.jugador2Nombre else (invitadoNombre ?: "Rival"),
                    if (estado.esEspectador) estado.jugador2Foto else invitadoFoto,
                    Modifier.weight(1f),
                )
            }
        }

        if (estado.esEspectador) {
            Spacer(Modifier.height(20.dp))
            Text("LA APUESTA ES CON", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(
                    invitadoNombre ?: "Elegir con quién apostaste",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (invitadoNombre != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = { mostrarSelector = true }) {
                    Text(if (invitadoNombre != null) "CAMBIAR" else "ELEGIR", style = CapsLabelTextStyle)
                }
            }
        }

        if (estado.incumplidosInvitado > 0 && !estado.esEspectador && invitadoId != null) {
            Spacer(Modifier.height(20.dp))
            TarjetaAlertaIncumplimiento(invitadoId!!, invitadoNombre ?: "tu rival", estado.incumplidosInvitado)
        }

        if (mostrarSelector) {
            DialogSelectorJugador(
                candidatos = estado.candidatosInvitado,
                titulo = "¿Con quién apostaste?",
                onSeleccionar = { jugador ->
                    invitadoId = jugador.id
                    invitadoNombre = jugador.nombre
                    invitadoFoto = jugador.foto_url
                    mostrarSelector = false
                },
                onCerrar = { mostrarSelector = false },
            )
        }

        Spacer(Modifier.height(24.dp))
        Text("LA APUESTA", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = descripcion,
            onValueChange = { if (it.length <= 280) descripcion = it },
            placeholder = { Text("¿Qué se jugaron? Ej: una gaseosa, un cebiche, S/10") },
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "Este es un registro informal para el honor de los jugadores.",
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp),
        )

        if (!sePuedeApostar) {
            Spacer(Modifier.height(12.dp))
            Text(
                if (v.estado != EstadoVersus.ACEPTADO) {
                    "Ya no se puede crear una apuesta sobre este reto — el resultado ya se está resolviendo o ya se sabe."
                } else {
                    "Ya no se puede crear una apuesta sobre este partido: ya comenzó."
                },
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (guardado is GuardarCompromisoState.Error) {
            Spacer(Modifier.height(12.dp))
            Text(guardado.mensaje, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        BotonTactil(
            texto = "Guardar apuesta",
            icono = Icons.Filled.Handshake,
            saltoElastico = true,
            cargando = guardado is GuardarCompromisoState.Guardando,
            enabled = sePuedeApostar && descripcion.trim().length >= 2 && invitadoId != null,
            onClick = { invitadoId?.let { onGuardar(it, descripcion.trim()) } },
            colorContenedor = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.fillMaxWidth().padding(top = 28.dp),
        )
    }
}

@Composable
private fun JugadorCompromiso(nombre: String, fotoUrl: String?, modifier: Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            val foto = fotoUrl.urlCompletaFoto()
            if (foto != null) {
                AsyncImage(model = foto, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(CircleShape))
            } else {
                Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(26.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(nombre, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, maxLines = 2)
    }
}
