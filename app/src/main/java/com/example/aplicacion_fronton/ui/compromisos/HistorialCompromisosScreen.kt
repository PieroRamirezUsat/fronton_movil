package com.example.aplicacion_fronton.ui.compromisos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.model.dto.EstadoCompromiso
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonReintentar
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.EstadoVacio
import com.example.aplicacion_fronton.ui.componentes.PillFiltro
import com.example.aplicacion_fronton.ui.componentes.SkeletonListaFilas
import com.example.aplicacion_fronton.ui.navigation.ItemBarraInferior
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle

@Composable
fun HistorialCompromisosScreen(
    onSeleccionarTab: (ItemBarraInferior) -> Unit,
    onVerInvitacion: (Int) -> Unit,
    onAdjuntar: (Int) -> Unit,
    onVerificar: (Int) -> Unit,
    onVerDetalle: (Int) -> Unit,
    onVerReporte: () -> Unit,
    onBuscarRivales: () -> Unit,
    onBuscarVersus: () -> Unit,
    onRetar: () -> Unit = {},
    paddingInterno: PaddingValues,
    viewModel: HistorialCompromisosViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var pestañaActiva by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { viewModel.cargar() }

    Column(modifier = Modifier.fillMaxSize().padding(paddingInterno)) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            ) {
                Text(
                    "APUESTAS",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                // Botón lleno (mismo peso visual que el FAB de la barra
                // inferior o el editar-foto de Perfil) en vez de un ícono
                // plano — antes se perdía al lado del título, ahora se
                // nota como una acción propia de la pantalla.
                IconButton(
                    onClick = onVerReporte,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary),
                ) {
                    Icon(Icons.Filled.QueryStats, contentDescription = "Ver reporte de apuestas", tint = MaterialTheme.colorScheme.onTertiary)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
        }

        when (val actual = estado) {
            is HistorialCompromisosState.Cargando -> SkeletonListaFilas(PaddingValues())
            is HistorialCompromisosState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(actual.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    BotonReintentar(onClick = { viewModel.cargar() })
                }
            }
            is HistorialCompromisosState.Exito -> Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    PillFiltro("RECIBIDOS", actual.recibidos.size, pestañaActiva == 0) { pestañaActiva = 0 }
                    PillFiltro("ENVIADOS", actual.enviados.size, pestañaActiva == 1) { pestañaActiva = 1 }
                    PillFiltro("EN CURSO", actual.enCurso.size, pestañaActiva == 2) { pestañaActiva = 2 }
                    PillFiltro("SALDADOS", actual.saldados.size, pestañaActiva == 3) { pestañaActiva = 3 }
                    ChipBuscarVersus(onClick = onBuscarVersus)
                }

                var visible by remember(pestañaActiva) { mutableStateOf(false) }
                LaunchedEffect(pestañaActiva) { visible = true }

                val lista = when (pestañaActiva) {
                    0 -> actual.recibidos
                    1 -> actual.enviados
                    2 -> actual.enCurso
                    else -> actual.saldados
                }

                if (lista.isEmpty()) {
                    val (icono, titulo, subtitulo) = when (pestañaActiva) {
                        0 -> Triple(Icons.Filled.Handshake, "SIN INVITACIONES", "Cuando alguien te invite a una apuesta informal, aparecerá aquí.")
                        1 -> Triple(Icons.Filled.Handshake, "NO HAS ENVIADO NINGUNO", "Registra una apuesta desde el detalle de un reto confirmado, o busca un reto ajeno para apostar.")
                        2 -> Triple(Icons.Filled.Handshake, "NADA EN CURSO", "Aquí verás las apuestas aceptadas mientras se sube y verifica el comprobante.")
                        else -> Triple(Icons.Filled.Handshake, "TODAVÍA NO HAY NADA SALDADO", "Las apuestas que ya se confirmaron de ambos lados aparecerán aquí.")
                    }
                    // Antes solo ENVIADOS tenía una acción — RECIBIDOS y EN CURSO
                    // se quedaban como puro ícono+texto sin ninguna salida. SALDADOS
                    // se queda sin botón a propósito: no tiene sentido "ir a buscar"
                    // algo que por definición todavía no existe la primera vez.
                    val (textoBotonVacio, onBotonVacio) = when (pestañaActiva) {
                        0, 2 -> "Buscar Retos" to onBuscarVersus
                        1 -> "Buscar rival" to onBuscarRivales
                        else -> null to {}
                    }
                    EstadoVacio(
                        icono = icono,
                        titulo = titulo,
                        subtitulo = subtitulo,
                        colorCirculo = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                        colorIcono = MaterialTheme.colorScheme.tertiary,
                        textoBoton = textoBotonVacio,
                        iconoBoton = Icons.Filled.Handshake,
                        onBoton = onBotonVacio,
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(lista, key = { it.compromiso.id }) { item ->
                            AnimatedVisibility(visible = visible, enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 6 }) {
                                TarjetaCompromiso(
                                    item = item,
                                    pestaña = pestañaActiva,
                                    onClick = {
                                        when (pestañaActiva) {
                                            0, 1 -> onVerInvitacion(item.compromiso.id)
                                            2 -> when (item.accion) {
                                                AccionCompromiso.ADJUNTAR -> onAdjuntar(item.compromiso.id)
                                                AccionCompromiso.VERIFICAR -> onVerificar(item.compromiso.id)
                                                // ESPERANDO/GANADOR_ESPERA/NINGUNA no tienen una
                                                // pantalla de acción propia -- antes esto caía en
                                                // un click sin efecto, ahora va al detalle.
                                                else -> onVerDetalle(item.compromiso.id)
                                            }
                                            else -> onVerDetalle(item.compromiso.id)
                                        }
                                    },
                                )
                            }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

/**
 * Entrada al flujo de espectador — apostar en un partido ajeno con otro
 * usuario que tampoco lo está jugando. Antes era una tarjeta-banner suelta
 * arriba de todo (color dorado, el mismo reservado para "pestaña activa" en
 * la barra inferior, así que se leía como un elemento de navegación perdido,
 * no como contenido de esta pantalla). Ahora es un chip más, al final de la
 * misma fila de pestañas, con borde en vez de relleno para marcarlo como
 * acción distinta a un filtro — mismo lenguaje visual que ya existe, sin
 * competir por la primera mirada del usuario.
 */
@Composable
private fun ChipBuscarVersus(onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.5.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Icon(Icons.Filled.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("BUSCAR RETOS", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.tertiary)
    }
}

private val mesesAbrevCompromiso = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")

private fun formatearFechaCortaCompromiso(iso: String): String = try {
    val partes = iso.substringBefore("T").split("-")
    "${partes[2]} ${mesesAbrevCompromiso[partes[1].toInt() - 1]} ${partes[0]}"
} catch (e: Exception) {
    iso
}

private val patronMontoCompromiso = Regex("^\\s*[Ss]/\\.?\\s*\\d")

/**
 * Layout fiel a la plantilla `historial_de_compromisos`: avatar a la
 * izquierda, columna de texto apilada (nombre, ícono+descripción o monto,
 * fecha), y a la derecha una columna con la píldora de estado + un botón
 * corto de acción cuando corresponde — antes todo iba en filas apiladas con
 * la píldora al fondo de la tarjeta, y la descripción se perdía como texto
 * plano gris sin ningún acento visual.
 */
@Composable
private fun TarjetaCompromiso(item: CompromisoUi, pestaña: Int, onClick: () -> Unit) {
    val c = item.compromiso
    val (colorEstado, textoEstado) = when {
        pestaña == 0 -> MaterialTheme.colorScheme.tertiary to "RESPONDER"
        pestaña == 1 -> MaterialTheme.colorScheme.secondary to "PENDIENTE"
        pestaña == 3 -> MaterialTheme.colorScheme.primary to "SALDADO"
        item.accion == AccionCompromiso.GANADOR_ESPERA -> MaterialTheme.colorScheme.primary to "GANASTE — ESPERA COMPROBANTE"
        item.accion == AccionCompromiso.ADJUNTAR -> MaterialTheme.colorScheme.tertiary to "ADJUNTAR COMPROBANTE"
        item.accion == AccionCompromiso.VERIFICAR -> MaterialTheme.colorScheme.tertiary to "VERIFICAR PAGO"
        c.estado == EstadoCompromiso.DISPUTA -> MaterialTheme.colorScheme.error to "EN DISPUTA"
        else -> MaterialTheme.colorScheme.secondary to "ESPERANDO VERIFICACIÓN"
    }
    // Botón corto de acción — dispara lo mismo que ya hace el click de toda la
    // tarjeta (mismo mapeo en el onClick de más arriba), solo que ahora hay un
    // atajo visible en vez de que la única pista sea el color del borde.
    val textoBotonAccion = when {
        pestaña == 0 -> "RESPONDER"
        pestaña == 2 && item.accion == AccionCompromiso.ADJUNTAR -> "ADJUNTAR"
        pestaña == 2 && item.accion == AccionCompromiso.VERIFICAR -> "VERIFICAR"
        else -> null
    }

    // Antes ganar/perder solo se leía en la palabra "GANASTE"/"PERDISTE" junto
    // al nombre — con varias tarjetas en pantalla (SALDADOS) todas se veían
    // igual de neutras y había que leer cada una. Acá se tiñe la franja
    // izquierda con el mismo primary/tertiary ya establecido para victoria/
    // derrota en toda la app (Home, el badge) — nunca un rojo/verde nuevo que
    // desentone. Cuando no es derivable, se cae al color de estado (igual que
    // la plantilla, que colorea la franja por estado, no por resultado).
    val colorResultado = when (item.esGanador) {
        true -> MaterialTheme.colorScheme.primary
        false -> MaterialTheme.colorScheme.tertiary
        null -> null
    }
    val colorFranja = colorResultado ?: colorEstado

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .drawBehind { drawRect(color = colorFranja, size = Size(4.dp.toPx(), size.height)) }
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 12.dp),
    ) {
        Box(
            modifier = Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            val foto = item.otroFotoUrl.urlCompletaFoto()
            if (foto != null) {
                AsyncImage(model = foto, contentDescription = item.otroNombre, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(CircleShape))
            } else {
                Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("vs. ${item.otroNombre}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                // Mismo lenguaje de color que "VICTORIA"/"DERROTA" en Inicio
                // (primary/tertiary) — para que ganar o perder una apuesta se
                // lea con el mismo código de color ya establecido en la app,
                // sin introducir un rojo/verde que desentone con la paleta.
                if (item.esGanador != null) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (item.esGanador) "GANASTE" else "PERDISTE",
                        style = CapsLabelTextStyle.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = if (item.esGanador) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            if (patronMontoCompromiso.containsMatchIn(c.descripcion)) {
                Text(c.descripcion, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, maxLines = 1)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "\"${c.descripcion}\"",
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(formatearFechaCortaCompromiso(c.created_at), style = CapsLabelTextStyle.copy(fontSize = 9.sp), color = MaterialTheme.colorScheme.outline)
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                textoEstado,
                style = CapsLabelTextStyle.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                color = colorEstado,
                modifier = Modifier.clip(RoundedCornerShape(50)).background(colorEstado.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 4.dp),
            )
            if (textoBotonAccion != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    textoBotonAccion,
                    style = CapsLabelTextStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onClick)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
    }
}

