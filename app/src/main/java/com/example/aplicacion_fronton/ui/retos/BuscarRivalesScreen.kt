package com.example.aplicacion_fronton.ui.retos

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.model.dto.CategoriaEdad
import com.example.aplicacion_fronton.model.dto.RankingEntryDto
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonReintentar
import com.example.aplicacion_fronton.ui.componentes.BotonVolver
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle

private val categoriasChip: List<Pair<CategoriaEdad?, String>> = listOf(
    null to "TODOS",
    CategoriaEdad.MENORES to "MENORES",
    CategoriaEdad.LIBRE to "LIBRE",
    CategoriaEdad.MAS_40 to "+40",
    CategoriaEdad.MAS_50 to "+50",
    CategoriaEdad.MAS_60 to "+60",
)

private val etiquetasCategoriaChip = mapOf(
    CategoriaEdad.MENORES to "Menores",
    CategoriaEdad.LIBRE to "Libre",
    CategoriaEdad.MAS_40 to "+40",
    CategoriaEdad.MAS_50 to "+50",
    CategoriaEdad.MAS_60 to "+60",
)

@Composable
fun BuscarRivalesScreen(
    onVolver: () -> Unit,
    onJugadorSeleccionado: (RankingEntryDto) -> Unit,
    onRetar: (RankingEntryDto) -> Unit,
    viewModel: BuscarRivalesViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var busqueda by remember { mutableStateOf("") }
    var categoriaActiva by remember { mutableStateOf<CategoriaEdad?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    // Antes esta pantalla mostraba la barra inferior completa
                    // aunque no fuera una pestaña propia — tocar otra pestaña
                    // por accidente descartaba en silencio la búsqueda en
                    // curso. Ahora es un flujo con principio y fin de verdad,
                    // como Crear Reto o Perfil de Jugador (un solo "volver").
                    BotonVolver(onClick = onVolver)
                    Text("BUSCAR RIVALES", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
            }
        },
    ) { padding ->
        when (val actual = estado) {
            is BuscarRivalesState.Cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is BuscarRivalesState.Error -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(actual.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    BotonReintentar(onClick = { viewModel.cargar() })
                }
            }
            is BuscarRivalesState.Exito -> {
                val filtrados = remember(actual.jugadores, busqueda, categoriaActiva) {
                    actual.jugadores.filter { j ->
                        (busqueda.isBlank() || j.nombre.contains(busqueda, ignoreCase = true)) &&
                            (categoriaActiva == null || j.categoria_edad == categoriaActiva)
                    }
                }

                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        TextField(
                            value = busqueda,
                            onValueChange = { busqueda = it },
                            placeholder = { Text("Buscar por nombre") },
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
                            categoriasChip.forEach { (valor, etiqueta) ->
                                ChipCategoria(etiqueta, categoriaActiva == valor) { categoriaActiva = valor }
                            }
                        }
                    }

                    if (filtrados.isEmpty()) {
                        EstadoVacioBusqueda(onVerTodos = { busqueda = ""; categoriaActiva = null })
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(filtrados, key = { it.id }) { jugador ->
                                TarjetaRival(
                                    jugador = jugador,
                                    onClick = { onJugadorSeleccionado(jugador) },
                                    onRetar = { onRetar(jugador) },
                                )
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipCategoria(etiqueta: String, activo: Boolean, onClick: () -> Unit) {
    Text(
        etiqueta,
        style = CapsLabelTextStyle,
        fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
        color = if (activo) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun TarjetaRival(jugador: RankingEntryDto, onClick: () -> Unit, onRetar: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
            .franjaIzquierdaRival(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(start = 20.dp, top = 12.dp, bottom = 12.dp, end = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                val foto = jugador.foto_url.urlCompletaFoto()
                if (foto != null) {
                    AsyncImage(model = foto, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(CircleShape))
                } else {
                    Text(jugador.nombre.trim().take(2).uppercase(), style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(jugador.nombre.uppercase(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Text("Categoría: ${etiquetasCategoriaChip[jugador.categoria_edad] ?: "-"}", style = CapsLabelTextStyle.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${jugador.elo} ELO", style = NumericTextStyle.copy(fontSize = 14.sp), color = MaterialTheme.colorScheme.tertiary)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.tertiary)
                .clickable(onClick = onRetar)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Icon(Icons.Filled.SportsHandball, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("RETAR", style = CapsLabelTextStyle, color = Color.White)
        }
    }
}

private fun Modifier.franjaIzquierdaRival(color: Color): Modifier = this.drawBehind {
    drawRect(color = color, size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height))
}

@Composable
private fun EstadoVacioBusqueda(onVerTodos: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Filled.PersonSearch, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(48.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("SIN RIVALES ENCONTRADOS", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            "No encontramos jugadores con ese nombre. Intenta con otros criterios o categorías.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Ver todos los jugadores",
            style = CapsLabelTextStyle,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onVerTodos),
        )
    }
}
