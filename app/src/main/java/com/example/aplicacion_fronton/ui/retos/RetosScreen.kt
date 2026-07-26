package com.example.aplicacion_fronton.ui.retos

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsTennis
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.model.dto.EstadoVersus
import com.example.aplicacion_fronton.model.dto.Modalidad
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonReintentar
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.EstadoVacio
import com.example.aplicacion_fronton.ui.componentes.SkeletonListaFilas
import com.example.aplicacion_fronton.ui.componentes.rememberInteraccionPresionable
import com.example.aplicacion_fronton.ui.navigation.BottomNavBar
import com.example.aplicacion_fronton.ui.navigation.ItemBarraInferior
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle

private val mesesAbrev = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")

private fun formatearFechaHora(iso: String): String = try {
    val fecha = iso.substringBefore("T")
    val hora = iso.substringAfter("T").take(5)
    val partes = fecha.split("-")
    "${partes[2]} ${mesesAbrev[partes[1].toInt() - 1]}, $hora"
} catch (e: Exception) {
    iso
}

@Composable
fun RetosScreen(
    onSeleccionarTab: (ItemBarraInferior) -> Unit,
    onVerDetalle: (Int) -> Unit = {},
    onRetar: () -> Unit = {},
    viewModel: RetosViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val idsRespondiendo by viewModel.idsRespondiendo.collectAsStateWithLifecycle()
    var pestañaActiva by remember { mutableIntStateOf(0) }

    // Igual que en Home: el ViewModel persiste mientras la pestaña siga en el
    // back stack, así que sin este refresco un reto aceptado/reportado desde
    // Detalle seguía apareciendo con su estado viejo al volver acá.
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
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Text(
                        "MIS RETOS",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
            }
        },
        bottomBar = { BottomNavBar(seleccionado = ItemBarraInferior.RETOS, onSeleccionar = onSeleccionarTab, onRetar = onRetar) },
    ) { paddingInterno ->
        when (val actual = estado) {
            is RetosState.Cargando -> SkeletonListaFilas(paddingInterno)
            is RetosState.Error -> Box(Modifier.fillMaxSize().padding(paddingInterno).padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(actual.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    BotonReintentar(onClick = { viewModel.cargar() })
                }
            }
            is RetosState.Exito -> Column(modifier = Modifier.fillMaxSize().padding(paddingInterno)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    PestañaReto("RECIBIDOS", actual.recibidos.size, pestañaActiva == 0) { pestañaActiva = 0 }
                    PestañaReto("ENVIADOS", actual.enviados.size, pestañaActiva == 1) { pestañaActiva = 1 }
                    PestañaReto("EN CURSO", actual.enCurso.size, pestañaActiva == 2) { pestañaActiva = 2 }
                }

                var visible by remember(pestañaActiva) { mutableStateOf(false) }
                LaunchedEffect(pestañaActiva) { visible = true }

                if (pestañaActiva == 2) {
                    if (actual.enCurso.isEmpty()) {
                        EstadoVacio(
                            icono = Icons.Filled.SportsTennis,
                            titulo = "SIN RETOS EN CURSO",
                            subtitulo = "Aquí verás tus partidos aceptados, jugados o en disputa apenas tengas uno.",
                            textoBoton = "Buscar rival",
                            onBoton = onRetar,
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(actual.enCurso, key = { it.versus.id }) { reto ->
                                AnimatedVisibility(
                                    visible = visible,
                                    enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 6 },
                                ) {
                                    TarjetaEnCurso(reto = reto, onClick = { onVerDetalle(reto.versus.id) })
                                }
                            }
                        }
                    }
                } else {
                    val lista = if (pestañaActiva == 0) actual.recibidos else actual.enviados
                    if (lista.isEmpty()) {
                        if (pestañaActiva == 0) {
                            EstadoVacio(
                                icono = Icons.Filled.EventBusy,
                                titulo = "SIN RETOS PENDIENTES",
                                subtitulo = "Cuando alguien te mande un reto, aparecerá aquí para que aceptes o rechaces.",
                            )
                        } else {
                            EstadoVacio(
                                icono = Icons.Filled.SportsTennis,
                                titulo = "NO HAS ENVIADO RETOS",
                                subtitulo = "Busca un rival y mándale tu primer reto.",
                                textoBoton = "Buscar rival",
                                onBoton = onRetar,
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(lista, key = { it.versus.id }) { reto ->
                                AnimatedVisibility(
                                    visible = visible,
                                    enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 6 },
                                ) {
                                    TarjetaReto(
                                        reto = reto,
                                        esRecibido = pestañaActiva == 0,
                                        respondiendo = reto.versus.id in idsRespondiendo,
                                        onAceptar = { viewModel.responder(reto.versus.id, true) },
                                        onRechazar = { viewModel.responder(reto.versus.id, false) },
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

@Composable
private fun PestañaReto(texto: String, contador: Int, activa: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (activa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            texto,
            style = CapsLabelTextStyle,
            color = if (activa) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (contador > 0) {
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (activa) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.tertiary)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(
                    contador.toString(),
                    style = CapsLabelTextStyle.copy(fontSize = 10.sp),
                    color = if (activa) Color.White else Color.White,
                )
            }
        }
    }
}

@Composable
private fun TarjetaReto(
    reto: RetoUi,
    esRecibido: Boolean,
    respondiendo: Boolean,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
) {
    val v = reto.versus
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(2.dp, MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                val foto = reto.rivalFoto.urlCompletaFoto()
                if (foto != null) {
                    AsyncImage(
                        model = foto,
                        contentDescription = reto.rivalNombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize().clip(CircleShape),
                    )
                } else {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(reto.rivalNombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val iconoModalidad: ImageVector = if (v.modalidad == Modalidad.DOBLES) Icons.Filled.Groups else Icons.Filled.SportsTennis
                    Icon(iconoModalidad, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (v.modalidad == Modalidad.DOBLES) "Dobles" else "Individual",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (!esRecibido) {
                Text(
                    "PENDIENTE",
                    style = CapsLabelTextStyle.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(formatearFechaHora(v.fecha_hora), style = NumericTextStyle.copy(fontSize = 13.sp), color = MaterialTheme.colorScheme.primary)
            if (v.cancha != null) {
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
                Text(v.cancha, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (esRecibido) {
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                val (interaccionRechazar, escalaRechazar) = rememberInteraccionPresionable()
                OutlinedButton(
                    onClick = onRechazar,
                    enabled = !respondiendo,
                    shape = RoundedCornerShape(12.dp),
                    interactionSource = interaccionRechazar,
                    modifier = Modifier.weight(1f).height(44.dp).scale(escalaRechazar),
                ) { Text("RECHAZAR", style = CapsLabelTextStyle) }
                BotonTactil(
                    texto = "Aceptar",
                    onClick = onAceptar,
                    cargando = respondiendo,
                    enabled = !respondiendo,
                    colorContenedor = MaterialTheme.colorScheme.primary,
                    altura = 40.dp,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TarjetaEnCurso(reto: RetoEnCursoUi, onClick: () -> Unit) {
    val v = reto.versus
    val (colorEstado, textoEstado) = when {
        v.estado == EstadoVersus.DISPUTA -> MaterialTheme.colorScheme.error to "EN DISPUTA"
        reto.miTurnoDeReportar -> MaterialTheme.colorScheme.tertiary to "TU TURNO DE REPORTAR"
        v.estado == EstadoVersus.JUGADO -> MaterialTheme.colorScheme.secondary to "ESPERANDO RIVAL"
        else -> MaterialTheme.colorScheme.primary to "PROGRAMADO"
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                2.dp,
                if (reto.miTurnoDeReportar || v.estado == EstadoVersus.DISPUTA) colorEstado.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                val foto = reto.rivalFoto.urlCompletaFoto()
                if (foto != null) {
                    AsyncImage(
                        model = foto,
                        contentDescription = reto.rivalNombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize().clip(CircleShape),
                    )
                } else {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(reto.rivalNombre, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val iconoModalidad: ImageVector = if (v.modalidad == Modalidad.DOBLES) Icons.Filled.Groups else Icons.Filled.SportsTennis
                    Icon(iconoModalidad, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (v.modalidad == Modalidad.DOBLES) "Dobles" else "Individual",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(formatearFechaHora(v.fecha_hora), style = NumericTextStyle.copy(fontSize = 13.sp), color = MaterialTheme.colorScheme.primary)
            if (v.cancha != null) {
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(2.dp))
                Text(v.cancha, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            textoEstado,
            style = CapsLabelTextStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = colorEstado,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(colorEstado.copy(alpha = 0.12f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

