package com.example.aplicacion_fronton.ui.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aplicacion_fronton.ui.componentes.InsigniaDivision
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.model.dto.RankingEntryDto
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonReintentar
import com.example.aplicacion_fronton.ui.componentes.SeccionEnCascada
import com.example.aplicacion_fronton.ui.componentes.SkeletonBox
import com.example.aplicacion_fronton.ui.componentes.SkeletonCircle
import com.example.aplicacion_fronton.ui.componentes.SkeletonLine
import com.example.aplicacion_fronton.ui.navigation.ItemBarraInferior
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle
import kotlinx.coroutines.launch

private val categorias = listOf(
    null to "TODAS",
    "libre" to "LIBRE",
    "menores" to "MENORES",
    "mas_40" to "+40",
    "mas_50" to "+50",
    "mas_60" to "+60",
)

@Composable
fun RankingScreen(
    onSeleccionarTab: (ItemBarraInferior) -> Unit,
    onJugadorSeleccionado: (RankingEntryDto, String) -> Unit = { _, _ -> },
    onRetar: () -> Unit = {},
    paddingInterno: PaddingValues,
    viewModel: RankingViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var busqueda by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Antes dependía solo de RankingViewModel.init{} — corría una sola vez por
    // instancia. Con las 6 pestañas compartiendo un solo ViewModelStore (ver
    // TabsShellScreen), el ViewModel ya no se recrea al revisitar la pestaña,
    // así que sin esto el ranking quedaba desactualizado tras la primera vez.
    LaunchedEffect(Unit) { viewModel.cargar() }

    Column(modifier = Modifier.fillMaxSize().padding(paddingInterno)) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Text(
                    "RANKING NACIONAL",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
        }

        when (val actual = estado) {
            is RankingState.Cargando -> SkeletonRanking(PaddingValues())
            is RankingState.Error -> Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(actual.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    BotonReintentar(onClick = { viewModel.cargar() })
                }
            }
            is RankingState.Exito -> {
                val filtrados = remember(actual.entradas, busqueda) {
                    if (busqueda.isBlank()) actual.entradas else actual.entradas.filter { it.nombre.contains(busqueda, ignoreCase = true) }
                }
                val indiceUsuario = filtrados.indexOfFirst { it.id == actual.miId }
                // Tocar tu propia fila lleva a "Mi Perfil" (con edición de foto y
                // cerrar sesión), no al perfil público de solo lectura de otro jugador.
                val manejarSeleccion: (RankingEntryDto) -> Unit = { entrada ->
                    if (entrada.id == actual.miId) {
                        onSeleccionarTab(ItemBarraInferior.PERFIL)
                    } else {
                        onJugadorSeleccionado(entrada, viewModel.modalidad)
                    }
                }
                // El podio solo tiene sentido para el ranking completo — mostrarlo
                // sobre resultados de búsqueda sugeriría un "1er lugar" falso que
                // solo es el primero entre las coincidencias, no el ranking real.
                val mostrarPodio = busqueda.isBlank() && filtrados.size >= 3

                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }

                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                        item {
                          SeccionEnCascada(visible, retraso = 0) {
                            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                                TextField(
                                    value = busqueda,
                                    onValueChange = { busqueda = it },
                                    placeholder = { Text("Buscar jugador...") },
                                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions.Default,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                    categorias.forEach { (valor, etiqueta) ->
                                        ChipFiltro(etiqueta, viewModel.categoriaEdad == valor) { viewModel.cambiarFiltro(categoriaEdad = valor) }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ChipFiltro("INDIVIDUAL", viewModel.modalidad == "individual", colorActivo = MaterialTheme.colorScheme.tertiary) {
                                        viewModel.cambiarFiltro(modalidad = "individual")
                                    }
                                    ChipFiltro("DOBLES", viewModel.modalidad == "dobles", colorActivo = MaterialTheme.colorScheme.tertiary) {
                                        viewModel.cambiarFiltro(modalidad = "dobles")
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ChipFiltro("TODOS", viewModel.genero == null) { viewModel.cambiarFiltro(genero = null) }
                                    ChipFiltro("HOMBRES", viewModel.genero == "masculino") { viewModel.cambiarFiltro(genero = "masculino") }
                                    ChipFiltro("MUJERES", viewModel.genero == "femenino") { viewModel.cambiarFiltro(genero = "femenino") }
                                }
                            }
                          }
                        }

                        if (mostrarPodio) {
                            item {
                                SeccionEnCascada(visible, retraso = 100) {
                                    Podio(filtrados.take(3), manejarSeleccion, listState)
                                }
                            }
                        }

                        if (filtrados.isEmpty()) {
                            item {
                                Text(
                                    "No hay jugadores en esta categoría todavía.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                )
                            }
                        } else {
                            item {
                                // Mismo offset izquierdo que FilaRanking (franja 4dp + 8dp de
                                // padding + ancho 40dp de la posición + avatar 32dp + 12dp) para
                                // que el encabezado quede realmente alineado con las columnas.
                                val colorEtiqueta = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                                ) {
                                    Spacer(Modifier.width(4.dp))
                                    Text("POS", style = CapsLabelTextStyle.copy(fontSize = 10.sp), color = colorEtiqueta, modifier = Modifier.width(40.dp))
                                    Spacer(Modifier.width(44.dp))
                                    Text("JUGADOR", style = CapsLabelTextStyle.copy(fontSize = 10.sp), color = colorEtiqueta, modifier = Modifier.weight(1f))
                                    Text("ELO", style = CapsLabelTextStyle.copy(fontSize = 10.sp), color = colorEtiqueta)
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                            }

                            val listaTabla = if (mostrarPodio) filtrados.drop(3) else filtrados
                            itemsIndexed(listaTabla, key = { _, entrada -> entrada.id }) { _, entrada ->
                                FilaRanking(entrada, esYo = entrada.id == actual.miId, onClick = { manejarSeleccion(entrada) })
                            }
                        }

                        item { Spacer(Modifier.height(80.dp)) }
                    }

                    if (indiceUsuario >= 0) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                scope.launch {
                                    // +2 por el item de filtros y el header de la tabla que preceden la lista
                                    val offsetItems = if (mostrarPodio) 3 else 2
                                    listState.animateScrollToItem((indiceUsuario + offsetItems).coerceAtLeast(0))
                                }
                            },
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary,
                            icon = { Icon(Icons.Filled.KeyboardDoubleArrowDown, contentDescription = null) },
                            text = { Text("MI POSICIÓN", style = CapsLabelTextStyle) },
                            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Chip discreto: sin relleno sólido — cuando está activo se distingue por un
 * borde de color + texto en negrita del mismo color, no por un bloque de color
 * saturado. Menos invasivo que un pill relleno, pero sigue siendo imposible de
 * pasar por alto por el contraste de borde+peso de fuente. */
@Composable
private fun ChipFiltro(
    etiqueta: String,
    activo: Boolean,
    colorActivo: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
) {
    Text(
        etiqueta,
        style = CapsLabelTextStyle,
        fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
        color = if (activo) colorActivo else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (activo) colorActivo.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                width = if (activo) 1.5.dp else 1.dp,
                color = if (activo) colorActivo else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(50),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun Podio(top3: List<RankingEntryDto>, onJugadorSeleccionado: (RankingEntryDto) -> Unit, listState: LazyListState) {
    // top3[0] = 1er lugar, top3[1] = 2do, top3[2] = 3ro
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            // Parallax sutil y puntual (único lugar de este pase con este
            // efecto): mientras el buscador (item 0) se desplaza fuera de
            // vista, el podio "se retrasa" un poco respecto al resto del
            // scroll — el elemento más "hero" de la pantalla ya no aparece
            // completamente estático. Se lee el offset dentro del lambda de
            // graphicsLayer (fase de dibujo) para no recomponer en cada
            // frame de scroll.
            .graphicsLayer {
                val desplazamiento = if (listState.firstVisibleItemIndex == 0) listState.firstVisibleItemScrollOffset else 0
                translationY = -desplazamiento * 0.25f
            },
    ) {
        PuestoPodio(top3.getOrNull(1), "2do", 64.dp, 96.dp, MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f), onClick = onJugadorSeleccionado)
        PuestoPodio(top3.getOrNull(0), "1er", 80.dp, 128.dp, MaterialTheme.colorScheme.primary, Color.White, Modifier.weight(1f), destacado = true, onClick = onJugadorSeleccionado)
        PuestoPodio(top3.getOrNull(2), "3ro", 64.dp, 80.dp, MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f), onClick = onJugadorSeleccionado)
    }
}

@Composable
private fun PuestoPodio(
    entrada: RankingEntryDto?,
    medalla: String,
    tamanoAvatar: Dp,
    alturaPlinto: Dp,
    colorPlinto: Color,
    colorTextoPlinto: Color,
    modifier: Modifier = Modifier,
    destacado: Boolean = false,
    onClick: (RankingEntryDto) -> Unit = {},
) {
    if (entrada == null) {
        Spacer(modifier.width(1.dp))
        return
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable { onClick(entrada) },
    ) {
        if (destacado) {
            Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(20.dp))
        }
        Box(
            modifier = Modifier
                .padding(top = 4.dp, bottom = 8.dp)
                .size(tamanoAvatar)
                .clip(CircleShape)
                .border(3.dp, if (destacado) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.outlineVariant, CircleShape),
        ) {
            AvatarConFallback(entrada.foto_url, entrada.nombre, tamanoAvatar)
        }
        Text(
            entrada.nombre.uppercase(),
            style = CapsLabelTextStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(alturaPlinto)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(colorPlinto)
                .padding(top = 12.dp),
        ) {
            Text(medalla, style = CapsLabelTextStyle, color = colorTextoPlinto.copy(alpha = 0.8f))
            Text(entrada.elo.toString(), style = NumericTextStyle.copy(fontSize = if (destacado) 22.sp else 18.sp, fontWeight = FontWeight.Bold), color = colorTextoPlinto)
        }
    }
}

@Composable
private fun FilaRanking(entrada: RankingEntryDto, esYo: Boolean, onClick: () -> Unit) {
    val colorFranja = if (esYo) MaterialTheme.colorScheme.tertiary else Color.Transparent
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(if (esYo) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Box(modifier = Modifier.width(4.dp).height(32.dp).background(colorFranja))
        Text(
            "%02d".format(entrada.posicion),
            style = NumericTextStyle.copy(fontSize = 18.sp),
            color = if (esYo) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            modifier = Modifier.width(40.dp).padding(start = 8.dp),
        )
        AvatarConFallback(entrada.foto_url, entrada.nombre, 32.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (esYo) "Tú (${entrada.nombre})" else entrada.nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (esYo) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
        InsigniaDivision(entrada.division)
        Spacer(Modifier.width(8.dp))
        Text(
            entrada.elo.toString(),
            style = NumericTextStyle.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            color = if (esYo) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun AvatarConFallback(fotoUrl: String?, nombre: String, tamano: Dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        if (fotoUrl != null) {
            AsyncImage(
                model = fotoUrl.urlCompletaFoto(),
                contentDescription = nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().clip(CircleShape),
            )
        } else {
            Text(
                nombre.trim().take(2).uppercase(),
                style = CapsLabelTextStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Silueta del podio (el elemento más "hero" de la pantalla) + filas de
 * tabla, en vez de un spinner sin relación con el contenido real. */
@Composable
private fun SkeletonRanking(padding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.weight(1f))
            SkeletonColumnaPodio(tamañoAvatar = 64.dp, alturaPlinto = 60.dp)
            SkeletonColumnaPodio(tamañoAvatar = 80.dp, alturaPlinto = 80.dp)
            SkeletonColumnaPodio(tamañoAvatar = 64.dp, alturaPlinto = 44.dp)
            Spacer(Modifier.weight(1f))
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(6) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    SkeletonLine(20.dp, alto = 16.dp)
                    SkeletonCircle(32.dp)
                    SkeletonLine(120.dp)
                    Spacer(Modifier.weight(1f))
                    SkeletonLine(40.dp)
                }
            }
        }
    }
}

@Composable
private fun SkeletonColumnaPodio(tamañoAvatar: androidx.compose.ui.unit.Dp, alturaPlinto: androidx.compose.ui.unit.Dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SkeletonCircle(tamañoAvatar)
        Spacer(Modifier.height(6.dp))
        SkeletonBox(Modifier.width(56.dp).height(alturaPlinto), forma = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
    }
}
