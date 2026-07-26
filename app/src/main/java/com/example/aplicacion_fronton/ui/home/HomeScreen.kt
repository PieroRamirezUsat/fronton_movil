package com.example.aplicacion_fronton.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsHandball
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.model.dto.Modalidad
import com.example.aplicacion_fronton.model.dto.RankingEntryDto
import com.example.aplicacion_fronton.network.PushTokenRegistrar
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonReintentar
import com.example.aplicacion_fronton.ui.componentes.SeccionEnCascada
import com.example.aplicacion_fronton.ui.componentes.SkeletonBox
import com.example.aplicacion_fronton.ui.componentes.SkeletonFilaCard
import com.example.aplicacion_fronton.ui.navigation.BottomNavBar
import com.example.aplicacion_fronton.ui.navigation.ItemBarraInferior
import com.example.aplicacion_fronton.ui.retos.PartidoHistorialUi
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle

private val mesesAbrevHome = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")

private fun formatearFechaHomeCorta(iso: String): String = try {
    val fecha = iso.substringBefore("T")
    val hora = iso.substringAfter("T").take(5)
    val partes = fecha.split("-")
    "${partes[2]} ${mesesAbrevHome[partes[1].toInt() - 1]}, $hora"
} catch (e: Exception) {
    iso
}

@Composable
fun HomeScreen(
    onSeleccionarTab: (ItemBarraInferior) -> Unit = {},
    onVersusSeleccionado: (Int) -> Unit = {},
    onRetar: () -> Unit = {},
    onSubidaRanking: (posicionAnterior: Int, posicionNueva: Int) -> Unit = { _, _ -> },
    viewModel: HomeViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // El ViewModel vive mientras el destino "home" siga en el back stack (que es
    // siempre, por el popUpTo de irATab) — sin esto, los datos se cargan una sola
    // vez y quedan obsoletos: aceptar un reto en otra pestaña y volver a Inicio
    // no reflejaba el nuevo "Próximo versus" hasta reiniciar la app.
    LaunchedEffect(Unit) { viewModel.cargarPerfil() }

    // Notificaciones push: el permiso en tiempo de ejecución (recién hace
    // falta desde API 33) se pide acá, con contexto ya adentro de la app, no
    // en Splash/Login — y se registra el token actual en el backend cada vez
    // que se entra a Home, mismo criterio que el refresco de datos de arriba.
    val lanzadorPermisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* Si lo rechaza, simplemente no le llegan pushes — no hay nada más que hacer acá. */ }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val yaConcedido = ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!yaConcedido) lanzadorPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        PushTokenRegistrar.registrarTokenActual()
    }

    // Se dispara una sola vez por detección: `cargarPerfil()` ya actualiza el
    // store con la posición actual en la misma pasada que arma el estado, así
    // que la próxima carga (por ejemplo al volver de la propia pantalla de
    // celebración) ya no vuelve a detectar la misma subida.
    LaunchedEffect((estado as? HomeState.Exito)?.subidaRankingDetectada) {
        val subida = (estado as? HomeState.Exito)?.subidaRankingDetectada
        if (subida != null) onSubidaRanking(subida.first, subida.second)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavBar(seleccionado = ItemBarraInferior.INICIO, onSeleccionar = onSeleccionarTab, onRetar = onRetar) },
    ) { paddingInterno ->
        when (val actual = estado) {
            is HomeState.Cargando -> CargandoHome(paddingInterno)
            is HomeState.Error -> ErrorHome(paddingInterno, actual.mensaje) { viewModel.cargarPerfil() }
            is HomeState.Exito -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingInterno),
            ) {
                // TopAppBar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (actual.usuario.foto_url != null) {
                            AsyncImage(
                                model = actual.usuario.foto_url.urlCompletaFoto(),
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize().clip(CircleShape),
                            )
                        } else {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Hola, ${actual.usuario.nombre}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)

                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    // Cascada de aparición por sección (fade+slide con delay escalonado
                    // de 100ms) — replica el ".stagger-1..5" del mockup de Stitch, que
                    // en la primera pasada de fidelidad visual se había omitido.
                    SeccionEnCascada(visible, retraso = 0) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            TarjetaEstadistica("ELO INDIVIDUAL", actual.usuario.elo_individual.toString(), Modifier.weight(1f))
                            TarjetaEstadistica("ELO DOBLES", actual.usuario.elo_dobles.toString(), Modifier.weight(1f))
                            TarjetaEstadistica("FICHAS", actual.usuario.fichas_cancha.toString(), Modifier.weight(1f))
                        }
                    }

                    // Próximo versus
                    SeccionEnCascada(visible, retraso = 100) {
                        Column {
                            Text("Próximo reto", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                            if (actual.proximoVersus != null) {
                                TarjetaProximoVersus(actual.proximoVersus, onVerDetalle = { onVersusSeleccionado(actual.proximoVersus.versusId) })
                            } else {
                                EstadoVacioSeccion(
                                    icono = Icons.Filled.EventBusy,
                                    texto = "Aún no tienes un reto programado.",
                                    textoBoton = "Buscar rival",
                                    onBoton = onRetar,
                                )
                            }
                        }
                    }

                    // Retos pendientes
                    SeccionEnCascada(visible, retraso = 200) {
                        Column {
                            Text("Retos pendientes", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                            if (actual.retosPendientes > 0) {
                                TarjetaRetosPendientes(actual.retosPendientes, onClick = { onSeleccionarTab(ItemBarraInferior.RETOS) })
                            } else {
                                EstadoVacioSeccion(
                                    icono = Icons.Filled.EventBusy,
                                    texto = "No tienes retos pendientes por responder.",
                                )
                            }
                        }
                    }

                    // Actividad reciente — cuando el jugador todavía no tiene partidos
                    // confirmados, un placeholder vacío deja el Home sin nada que ver.
                    // Se reemplaza por un adelanto del ranking (mismos datos ya
                    // cargados para "Próximo versus", sin pedidos extra) para que
                    // siempre haya contenido útil que invite a jugar.
                    SeccionEnCascada(visible, retraso = 300) {
                        Column {
                            Text(
                                if (actual.actividadReciente.isNotEmpty()) "Actividad reciente" else "Top del ranking",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            if (actual.actividadReciente.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                                    actual.actividadReciente.forEach { partido ->
                                        FilaActividadReciente(partido, onClick = { onVersusSeleccionado(partido.versusId) })
                                    }
                                }
                            } else if (actual.rankingPreview.isNotEmpty()) {
                                TarjetaRankingPreview(
                                    entradas = actual.rankingPreview,
                                    miId = actual.usuario.id,
                                    onVerRanking = { onSeleccionarTab(ItemBarraInferior.RANKING) },
                                )
                            } else {
                                EstadoVacioSeccion(
                                    icono = Icons.Filled.EventBusy,
                                    texto = "Todavía no hay actividad para mostrar.",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaEstadistica(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(etiqueta, style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = NumericTextStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun TarjetaProximoVersus(versus: ProximoVersusUi, onVerDetalle: () -> Unit) {
    val colorFranja = MaterialTheme.colorScheme.tertiary
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .drawBehind {
                drawRect(color = colorFranja, size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height))
            }
            .padding(start = 20.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(formatearFechaHomeCorta(versus.fechaHora), style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.tertiary)
                Text("vs ${versus.rivalNombre}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(versus.cancha ?: "Sin cancha definida", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                val foto = versus.rivalFotoUrl.urlCompletaFoto()
                if (foto != null) {
                    AsyncImage(model = foto, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(CircleShape))
                } else {
                    Icon(Icons.Filled.SportsHandball, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        TextButton(
            onClick = onVerDetalle,
            colors = ButtonDefaults.textButtonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("VER DETALLE", style = CapsLabelTextStyle)
        }
    }
}

@Composable
private fun TarjetaRetosPendientes(cantidad: Int, onClick: () -> Unit) {
    val colorFranja = MaterialTheme.colorScheme.tertiary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .drawBehind {
                drawRect(color = colorFranja, size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height))
            }
            .clickable(onClick = onClick)
            .padding(start = 20.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(cantidad.toString(), style = NumericTextStyle.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.tertiary)
        }
        Spacer(Modifier.width(12.dp))
        Text(
            if (cantidad == 1) "Tienes 1 reto por responder." else "Tienes $cantidad retos por responder.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FilaActividadReciente(partido: PartidoHistorialUi, onClick: () -> Unit) {
    val colorResultado = if (partido.gane) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Column {
            Text(
                "vs ${partido.rivalNombre}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                if (partido.modalidad == Modalidad.DOBLES) "Dobles" else "Individual",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (partido.gane) "VICTORIA" else "DERROTA",
                style = CapsLabelTextStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = colorResultado,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text(
                "${partido.misSets}-${partido.susSets}",
                style = NumericTextStyle.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/** Franja "court-line" (borde izquierdo terracota de 4dp) — firma visual de las
 * cards destacadas en todo el sistema de diseño Stitch, usada acá para que las
 * secciones vacías del Home no se vean como simples placeholders genéricos. */
@Composable
private fun EstadoVacioSeccion(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    textoBoton: String? = null,
    onBoton: () -> Unit = {},
) {
    val colorFranja = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
    Column(
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(texto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        }
        if (textoBoton != null) {
            TextButton(
                onClick = onBoton,
                colors = ButtonDefaults.textButtonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                Text(textoBoton, style = CapsLabelTextStyle)
            }
        }
    }
}

/** Adelanto del ranking (top 3) mostrado en Home cuando el jugador todavía no
 * tiene "Actividad reciente" propia — reusa la misma lista ya cargada para
 * "Próximo versus" (sin pedidos extra al backend) para que el Home nunca se
 * sienta vacío del todo. */
@Composable
private fun TarjetaRankingPreview(entradas: List<RankingEntryDto>, miId: Int, onVerRanking: () -> Unit) {
    val colorFranja = MaterialTheme.colorScheme.secondaryContainer
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .drawBehind {
                drawRect(color = colorFranja, size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height))
            }
            .padding(start = 20.dp, top = 16.dp, bottom = 16.dp, end = 16.dp),
    ) {
        entradas.forEachIndexed { indice, entrada ->
            val destacado = entrada.id == miId
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
            ) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (entrada.posicion <= 3) {
                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(18.dp))
                    } else {
                        Text(entrada.posicion.toString(), style = NumericTextStyle.copy(fontSize = 13.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .border(if (destacado) 2.dp else 0.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    val foto = entrada.foto_url.urlCompletaFoto()
                    if (foto != null) {
                        AsyncImage(model = foto, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(CircleShape))
                    } else {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    entrada.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (destacado) FontWeight.Bold else FontWeight.Normal,
                    color = if (destacado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Text("${entrada.elo}", style = NumericTextStyle.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            }
            if (indice < entradas.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 1.dp)
            }
        }
        TextButton(
            onClick = onVerRanking,
            colors = ButtonDefaults.textButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            Text("VER RANKING COMPLETO", style = CapsLabelTextStyle)
        }
    }
}

@Composable
private fun CargandoHome(padding: PaddingValues) {
    // Silueta del contenido real (3 stat-chips, card de próximo versus,
    // 2 filas de actividad) en vez de un spinner genérico sin relación con
    // lo que va a aparecer.
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            repeat(3) { SkeletonBox(Modifier.weight(1f).height(64.dp), forma = RoundedCornerShape(12.dp)) }
        }
        SkeletonBox(Modifier.fillMaxWidth().height(120.dp), forma = RoundedCornerShape(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonFilaCard()
            SkeletonFilaCard()
        }
    }
}

@Composable
private fun ErrorHome(padding: PaddingValues, mensaje: String, onReintentar: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(mensaje, color = MaterialTheme.colorScheme.error)
            BotonReintentar(onClick = onReintentar)
        }
    }
}
