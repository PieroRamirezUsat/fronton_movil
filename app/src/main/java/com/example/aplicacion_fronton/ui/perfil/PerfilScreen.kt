package com.example.aplicacion_fronton.ui.perfil

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.model.dto.CategoriaEdad
import com.example.aplicacion_fronton.model.dto.Modalidad
import com.example.aplicacion_fronton.model.dto.NivelCumplimiento
import com.example.aplicacion_fronton.model.dto.UsuarioDto
import com.example.aplicacion_fronton.network.uriAParteMultipart
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonReintentar
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.DialogSelectorJugador
import com.example.aplicacion_fronton.ui.componentes.SeccionEnCascada
import com.example.aplicacion_fronton.ui.componentes.SkeletonBox
import com.example.aplicacion_fronton.ui.componentes.SkeletonCircle
import com.example.aplicacion_fronton.ui.componentes.SkeletonFilaCard
import com.example.aplicacion_fronton.ui.componentes.SkeletonLine
import com.example.aplicacion_fronton.ui.navigation.ItemBarraInferior
import com.example.aplicacion_fronton.ui.retos.PartidoHistorialUi
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle

private val etiquetasCategoria = mapOf(
    CategoriaEdad.MENORES to "Menores",
    CategoriaEdad.LIBRE to "Libre",
    CategoriaEdad.MAS_40 to "+40",
    CategoriaEdad.MAS_50 to "+50",
    CategoriaEdad.MAS_60 to "+60",
)

@Composable
fun PerfilScreen(
    onSeleccionarTab: (ItemBarraInferior) -> Unit,
    onSesionCerrada: () -> Unit = {},
    onVerHistorial: () -> Unit = {},
    onAjustes: () -> Unit = {},
    onRetar: () -> Unit = {},
    paddingInterno: PaddingValues,
    viewModel: PerfilViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val subiendoFoto by viewModel.subiendoFoto.collectAsStateWithLifecycle()
    val errorPareja by viewModel.errorPareja.collectAsStateWithLifecycle()
    val sesionCerrada by viewModel.sesionCerrada.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // Mismo motivo que en Home/Retos: sin esto el ELO, el cumplimiento y los
    // "últimos versus" quedaban con los valores de la primera carga aunque
    // hubiera cambios reales (partido confirmado, pareja habitual, etc.).
    LaunchedEffect(Unit) { viewModel.cargar() }

    // Se espera a que el ViewModel confirme que ya intentó borrar el token de
    // push del servidor (con sesión todavía válida) antes de navegar — antes
    // esto se llamaba sincrónico junto a la navegación, sin garantía de que
    // cualquier llamada de red asociada al logout llegara a salir.
    LaunchedEffect(sesionCerrada) {
        if (sesionCerrada) onSesionCerrada()
    }
    val selectorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) viewModel.subirFoto(uriAParteMultipart(contexto, uri)) }
    var selectorParejaAbierto by remember { mutableStateOf(false) }

    when (val actual = estado) {
            is PerfilState.Cargando -> SkeletonPerfil(paddingInterno)
            is PerfilState.Error -> Box(Modifier.fillMaxSize().padding(paddingInterno).padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(actual.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    BotonReintentar(onClick = { viewModel.cargar() })
                }
            }
            is PerfilState.Exito -> {
                if (selectorParejaAbierto) {
                    DialogSelectorJugador(
                        // Se excluyen jugadores que ya tienen compañero habitual
                        // asignado a otra persona — el emparejamiento es mutuo y
                        // exclusivo, así que no tiene sentido dejarlos elegibles.
                        candidatos = actual.jugadores.filter {
                            it.id != actual.usuario.id && (it.pareja_habitual_id == null || it.pareja_habitual_id == actual.usuario.id)
                        },
                        titulo = "Elegir compañero habitual",
                        onSeleccionar = {
                            viewModel.actualizarParejaHabitual(it.id)
                            selectorParejaAbierto = false
                        },
                        onCerrar = { selectorParejaAbierto = false },
                    )
                }
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }

                Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingInterno)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
            ) {
                SeccionEnCascada(visible, retraso = 0) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            IconButton(onClick = onAjustes) {
                                Icon(Icons.Filled.Settings, contentDescription = "Ajustes", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        CabeceraPerfil(
                            usuario = actual.usuario,
                            subiendoFoto = subiendoFoto,
                            onElegirFoto = { selectorFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                SeccionEnCascada(visible, retraso = 100) {
                    Column {
                        TarjetaEloYRanking(actual.usuario, actual.posicionRanking)
                        Spacer(Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            EstadisticaPerfil("ELO INDIVIDUAL", actual.usuario.elo_individual.toString(), Modifier.weight(1f))
                            EstadisticaPerfil("ELO DOBLES", actual.usuario.elo_dobles.toString(), Modifier.weight(1f))
                            EstadisticaPerfil("FICHAS", actual.usuario.fichas_cancha.toString(), Modifier.weight(1f))
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                SeccionEnCascada(visible, retraso = 200) {
                    Column {
                        Text("COMPAÑERO HABITUAL DE DOBLES", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        SelectorParejaHabitual(
                            nombre = actual.jugadores.firstOrNull { it.id == actual.usuario.pareja_habitual_id }?.nombre,
                            onClick = { selectorParejaAbierto = true },
                            onQuitar = if (actual.usuario.pareja_habitual_id != null) {
                                { viewModel.actualizarParejaHabitual(null) }
                            } else null,
                        )
                        if (errorPareja != null) {
                            Text(
                                errorPareja.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                SeccionEnCascada(visible, retraso = 300) {
                    TarjetaCumplimiento(actual.cumplimiento)
                }

                Spacer(Modifier.height(24.dp))
                SeccionEnCascada(visible, retraso = 400) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("ÚLTIMOS RETOS", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                            // Siempre visible, no solo cuando ya hay partidos — si no,
                            // alguien sin partidos confirmados todavía no tenía ninguna
                            // forma de entrar al Historial (que sí tiene su propio estado
                            // vacío honesto con un CTA real a "Buscar rivales").
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable(onClick = onVerHistorial),
                            ) {
                                Text("VER TODOS", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.tertiary)
                                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                            }
                        }
                        if (actual.ultimosVersus.isEmpty()) {
                            Box(modifier = Modifier.clickable(onClick = onVerHistorial)) {
                                EstadoVacioPerfil("Aún no hay historial de partidos para mostrar aquí.")
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                                actual.ultimosVersus.forEach { partido ->
                                    FilaVersusReciente(partido, onClick = onVerHistorial)
                                }
                            }
                        }

                        BotonTactil(
                            texto = "Cerrar sesión",
                            icono = Icons.AutoMirrored.Filled.Logout,
                            onClick = { viewModel.cerrarSesion() },
                            colorContenedor = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                        )
                    }
                }
            }
            }
        }
}

/** Mismo lenguaje visual que `SelectorCompañero` en Crear Reto — círculo con
 * ícono de persona, nombre + descripción, y una acción a la derecha (chevron
 * para elegir, o una X compacta para quitar). Nada de texto suelto compitiendo
 * por espacio horizontal, que era lo que rompía el layout antes. */
@Composable
private fun SelectorParejaHabitual(nombre: String?, onClick: () -> Unit, onQuitar: (() -> Unit)?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(
                width = 2.dp,
                color = if (nombre != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
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
        Column(modifier = Modifier.weight(1f)) {
            Text(nombre ?: "Elegir compañero", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                if (nombre != null) "Se usa automáticamente al crear un reto de dobles" else "Invita a alguien de tu club",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (onQuitar != null) {
            IconButton(onClick = onQuitar) {
                Icon(Icons.Filled.Close, contentDescription = "Quitar compañero habitual", tint = MaterialTheme.colorScheme.tertiary)
            }
        } else {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun CabeceraPerfil(usuario: UsuarioDto, subiendoFoto: Boolean, onElegirFoto: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(128.dp)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .clickable(enabled = !subiendoFoto, onClick = onElegirFoto),
                contentAlignment = Alignment.Center,
            ) {
                val modeloFoto = usuario.foto_url.urlCompletaFoto()
                if (modeloFoto != null) {
                    AsyncImage(
                        model = modeloFoto,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize().clip(CircleShape),
                    )
                } else {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(48.dp))
                }
                if (subiendoFoto) {
                    Box(
                        modifier = Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
            // Badge tipo FAB — mismo patrón que el paso de perfil del registro:
            // solo la imagen es tocable, sin texto debajo.
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    .clickable(enabled = !subiendoFoto, onClick = onElegirFoto),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = "Cambiar foto de perfil", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            usuario.nombre.uppercase(),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "CATEGORÍA ${etiquetasCategoria[usuario.categoria_edad]?.uppercase() ?: ""}",
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
                if (usuario.club != null) {
                    Icon(Icons.Filled.Stadium, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    usuario.club?.uppercase() ?: "JUGADOR LIBRE",
                    style = CapsLabelTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TarjetaEloYRanking(usuario: UsuarioDto, posicionRanking: Int?) {
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("RATING ELO INDIVIDUAL", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                usuario.elo_individual.toString(),
                style = NumericTextStyle.copy(fontSize = 40.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
        }
        if (posicionRanking != null) {
            Column(horizontalAlignment = Alignment.End) {
                Text("RANKING NACIONAL", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("#$posicionRanking", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun EstadisticaPerfil(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(etiqueta, style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = NumericTextStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun TarjetaCumplimiento(cumplimiento: com.example.aplicacion_fronton.model.dto.CumplimientoDto?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(16.dp),
    ) {
        Text("HISTORIAL DE CUMPLIMIENTO", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (cumplimiento == null) {
            Text(
                "No se pudo cargar tu nivel de cumplimiento.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
            return@Column
        }

        val (colorNivel, textoNivel) = when (cumplimiento.nivel) {
            NivelCumplimiento.CUMPLE_SIEMPRE -> MaterialTheme.colorScheme.primary to "CUMPLE SIEMPRE"
            NivelCumplimiento.CUMPLE_CASI_SIEMPRE -> MaterialTheme.colorScheme.secondary to "CUMPLE CASI SIEMPRE"
            NivelCumplimiento.HISTORIAL_INCUMPLIMIENTOS -> MaterialTheme.colorScheme.tertiary to "HISTORIAL DE INCUMPLIMIENTOS"
        }

        Text(
            textoNivel,
            style = CapsLabelTextStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier
                .padding(top = 12.dp, bottom = 12.dp)
                .clip(RoundedCornerShape(50))
                .background(colorNivel)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

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

@Composable
private fun FilaVersusReciente(partido: PartidoHistorialUi, onClick: () -> Unit) {
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

@Composable
private fun EstadoVacioPerfil(texto: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp),
    ) {
        Icon(Icons.Filled.EventBusy, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(texto, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Silueta de cabecera (avatar grande) + 2 tarjetas + lista, en vez de un
 * spinner sin relación con lo que va a aparecer — es la pantalla más
 * "personal" de la app, la que más se nota vacía mientras carga. */
@Composable
private fun SkeletonPerfil(padding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SkeletonCircle(96.dp)
        SkeletonLine(160.dp)
        SkeletonBox(Modifier.fillMaxWidth().height(72.dp), forma = RoundedCornerShape(12.dp))
        SkeletonBox(Modifier.fillMaxWidth().height(100.dp), forma = RoundedCornerShape(12.dp))
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SkeletonFilaCard()
            SkeletonFilaCard()
        }
    }
}