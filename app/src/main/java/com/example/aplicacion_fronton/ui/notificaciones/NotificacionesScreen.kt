package com.example.aplicacion_fronton.ui.notificaciones

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplicacion_fronton.model.dto.NotificacionDto
import com.example.aplicacion_fronton.model.dto.TipoNotificacion
import com.example.aplicacion_fronton.ui.componentes.CargandoPelotita
import com.example.aplicacion_fronton.ui.componentes.EstadoVacio
import com.example.aplicacion_fronton.ui.componentes.PillFiltro
import com.example.aplicacion_fronton.ui.componentes.SeccionEnCascada
import com.example.aplicacion_fronton.ui.navigation.ItemBarraInferior
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import java.util.Calendar
import java.util.TimeZone

private fun formatearTiempoRelativo(iso: String): String = try {
    val fecha = iso.substringBefore("T")
    val hora = iso.substringAfter("T")
    val partesFecha = fecha.split("-")
    val partesHora = hora.split(":")
    val calendario = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(partesFecha[0].toInt(), partesFecha[1].toInt() - 1, partesFecha[2].toInt(), partesHora[0].toInt(), partesHora[1].toInt(), 0)
        set(Calendar.MILLISECOND, 0)
    }
    val diffMinutos = ((System.currentTimeMillis() - calendario.timeInMillis) / 60000).coerceAtLeast(0)
    when {
        diffMinutos < 1 -> "ahora"
        diffMinutos < 60 -> "hace ${diffMinutos}min"
        diffMinutos < 60 * 24 -> "hace ${diffMinutos / 60}h"
        else -> "hace ${diffMinutos / (60 * 24)}d"
    }
} catch (e: Exception) {
    ""
}

private fun iconoYColorTipo(tipo: TipoNotificacion): Pair<ImageVector, Color> = when (tipo) {
    TipoNotificacion.RETO_RECIBIDO -> Icons.Filled.SportsTennis to Color(0xFF146259)
    TipoNotificacion.RETO_ACEPTADO -> Icons.Filled.CheckCircle to Color(0xFF146259)
    TipoNotificacion.RETO_RECHAZADO -> Icons.Filled.Cancel to Color(0xFF782208)
    TipoNotificacion.MARCADOR_PENDIENTE -> Icons.Filled.Schedule to Color(0xFF7E5700)
    TipoNotificacion.RESULTADO_CONFIRMADO -> Icons.Filled.EmojiEvents to Color(0xFF146259)
    TipoNotificacion.RESULTADO_DISPUTA -> Icons.Filled.WarningAmber to Color(0xFFBA1A1A)
    TipoNotificacion.COMPROMISO_RECIBIDO -> Icons.Filled.Handshake to Color(0xFF146259)
    TipoNotificacion.COMPROMISO_ACEPTADO -> Icons.Filled.Handshake to Color(0xFF146259)
    TipoNotificacion.COMPROMISO_RECHAZADO -> Icons.Filled.Cancel to Color(0xFF782208)
    TipoNotificacion.COMPROMISO_COMPROBANTE_SUBIDO -> Icons.Filled.Receipt to Color(0xFF7E5700)
    TipoNotificacion.COMPROMISO_SALDADO -> Icons.Filled.CheckCircle to Color(0xFF146259)
    TipoNotificacion.COMPROMISO_DISPUTA -> Icons.Filled.WarningAmber to Color(0xFFBA1A1A)
    TipoNotificacion.RETO_CANCELACION_PROPUESTA -> Icons.Filled.Schedule to Color(0xFF7E5700)
    TipoNotificacion.RETO_CANCELADO -> Icons.Filled.Cancel to Color(0xFF782208)
    TipoNotificacion.RETO_CANCELACION_RECHAZADA -> Icons.Filled.SportsTennis to Color(0xFF146259)
    TipoNotificacion.RETO_INASISTENCIA -> Icons.Filled.WarningAmber to Color(0xFFBA1A1A)
    TipoNotificacion.RECORDATORIO_RETO -> Icons.Filled.Schedule to Color(0xFF7E5700)
}

// Los únicos 5 tipos donde todavía hay una decisión real esperando al
// usuario — el resto es informativo (ya pasó, no hay nada que resolver).
private val TIPOS_ACCIONABLES = setOf(
    TipoNotificacion.RETO_RECIBIDO,
    TipoNotificacion.MARCADOR_PENDIENTE,
    TipoNotificacion.COMPROMISO_RECIBIDO,
    TipoNotificacion.COMPROMISO_COMPROBANTE_SUBIDO,
    TipoNotificacion.RETO_CANCELACION_PROPUESTA,
)

@Composable
fun NotificacionesScreen(
    onSeleccionarTab: (ItemBarraInferior) -> Unit = {},
    onAbrirNotificacion: (NotificacionDto) -> Unit,
    onBuscarRivales: () -> Unit = {},
    onRetar: () -> Unit = {},
    paddingInterno: PaddingValues,
    viewModel: NotificacionesViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    // Al convertirse en pestaña raíz, este ViewModel no se recrea al volver
    // de otra pestaña (mismo bug ya corregido en Home/Retos/Perfil) — sin
    // esto, resolver un reto en otra pestaña y volver acá seguiría mostrando
    // la lista vieja.
    LaunchedEffect(Unit) { viewModel.cargar() }

    val notificaciones = (estado as? NotificacionesState.Exito)?.notificaciones
    val pendientes = notificaciones?.filter { !it.leida && it.tipo in TIPOS_ACCIONABLES } ?: emptyList()
    // Se decide una sola vez, con el primer dato que llega — after eso, el
    // usuario elige libremente sin que la pestaña "salte" sola.
    var mostrandoPendientes by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(notificaciones != null) {
        if (notificaciones != null && mostrandoPendientes == null) {
            mostrandoPendientes = pendientes.isNotEmpty()
        }
    }
    val pestañaPendientes = mostrandoPendientes ?: true

    Column(modifier = Modifier.fillMaxSize().padding(paddingInterno)) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
                ) {
                    Text(
                        "NOTIFICACIONES",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    // Marcar todo como leído mientras se mira PENDIENTES daría
                    // una falsa sensación de "ya lo atendí" sobre un reto o
                    // apuesta que sigue esperando respuesta — solo se ofrece
                    // mirando TODAS.
                    val hayNuevas = notificaciones?.any { !it.leida } == true
                    if (hayNuevas && !pestañaPendientes) {
                        TextButton(onClick = { viewModel.marcarTodasLeidas() }) {
                            Text("MARCAR LEÍDAS", style = CapsLabelTextStyle.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                if (!notificaciones.isNullOrEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
                    ) {
                        PillFiltro("PENDIENTES", pendientes.size, pestañaPendientes) { mostrandoPendientes = true }
                        PillFiltro("TODAS", 0, !pestañaPendientes) { mostrandoPendientes = false }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
        }

        when (val actual = estado) {
            is NotificacionesState.Cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CargandoPelotita()
            }
            is NotificacionesState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(actual.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            is NotificacionesState.Exito -> {
                if (actual.notificaciones.isEmpty()) {
                    // Antes era puro ícono+texto sin ninguna salida — la
                    // pantalla completa de notificaciones vacía era el
                    // "callejón sin salida" más notorio de la app.
                    EstadoVacio(
                        icono = Icons.Filled.EventBusy,
                        titulo = "No tienes notificaciones todavía",
                        subtitulo = "Los avisos de tus retos y resultados aparecerán aquí.",
                        textoBoton = "Buscar rival",
                        onBoton = onBuscarRivales,
                    )
                } else if (pestañaPendientes) {
                    if (pendientes.isEmpty()) {
                        EstadoVacio(
                            icono = Icons.Filled.CheckCircle,
                            titulo = "Nada pendiente por ahora",
                            subtitulo = "Cuando un reto o una apuesta necesite tu respuesta, aparecerá aquí.",
                        )
                    } else {
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { visible = true }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            itemsIndexed(pendientes, key = { _, n -> n.id }) { index, notificacion ->
                                // Escalonado por ítem (no solo por sección) —
                                // son todas accionables, así que cada una
                                // "entra" en cascada en vez de aparecer todas
                                // de golpe.
                                SeccionEnCascada(visible, retraso = index * 40) {
                                    TarjetaNotificacion(notificacion, destacada = true) {
                                        viewModel.marcarLeida(notificacion.id)
                                        onAbrirNotificacion(notificacion)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val nuevas = actual.notificaciones.filter { !it.leida }
                    val anteriores = actual.notificaciones.filter { it.leida }
                    // Antes se mostraban TODAS las antiguas siempre — con
                    // suficiente historial, la pantalla quedaba llena de avisos
                    // viejos y las nuevas se perdían más abajo. Se limita a las
                    // últimas 8 por defecto, con un botón para ver el resto.
                    var verTodasAnteriores by remember { mutableStateOf(false) }
                    val limiteAnteriores = 8
                    val anterioresVisibles = if (verTodasAnteriores) anteriores else anteriores.take(limiteAnteriores)

                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { visible = true }

                  SeccionEnCascada(visible, retraso = 0) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (nuevas.isNotEmpty()) {
                            item { EncabezadoSeccion("NUEVAS", MaterialTheme.colorScheme.tertiary) }
                            itemsIndexed(nuevas, key = { _, n -> n.id }) { index, notificacion ->
                                SeccionEnCascada(visible, retraso = index * 40) {
                                    TarjetaNotificacion(notificacion, destacada = true) {
                                        viewModel.marcarLeida(notificacion.id)
                                        onAbrirNotificacion(notificacion)
                                    }
                                }
                            }
                        }
                        if (anteriores.isNotEmpty()) {
                            item { EncabezadoSeccion("ANTERIORES", MaterialTheme.colorScheme.outline) }
                            items(anterioresVisibles, key = { it.id }) { notificacion ->
                                TarjetaNotificacion(notificacion, destacada = false) {
                                    onAbrirNotificacion(notificacion)
                                }
                            }
                            if (!verTodasAnteriores && anteriores.size > limiteAnteriores) {
                                item {
                                    TextButton(
                                        onClick = { verTodasAnteriores = true },
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text(
                                            "VER ${anteriores.size - limiteAnteriores} MÁS ANTIGUAS",
                                            style = CapsLabelTextStyle,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                            }
                        }
                    }
                  }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoSeccion(texto: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)) {
        Box(modifier = Modifier.width(16.dp).height(2.dp).background(color))
        Spacer(Modifier.width(8.dp))
        Text(texto, style = CapsLabelTextStyle, color = color)
    }
}

@Composable
private fun TarjetaNotificacion(notificacion: NotificacionDto, destacada: Boolean, onClick: () -> Unit) {
    val (icono, colorIcono) = iconoYColorTipo(notificacion.tipo)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (destacada) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        if (destacada) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
            Spacer(Modifier.width(10.dp))
        } else {
            Spacer(Modifier.width(18.dp))
        }
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colorIcono.copy(alpha = if (destacada) 0.16f else 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                notificacion.titulo,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (destacada) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                notificacion.mensaje,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                formatearTiempoRelativo(notificacion.created_at),
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic, fontSize = 11.sp),
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
