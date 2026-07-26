package com.example.aplicacion_fronton.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle

enum class ItemBarraInferior { INICIO, RANKING, RETOS, APUESTAS, NOTIFICACIONES, PERFIL }

/**
 * Barra de navegación inferior — 6 pestañas planas (Inicio, Ranking, Retos a
 * la izquierda del FAB; Apuestas, Notificaciones, Perfil a la derecha) + el
 * botón circular del centro como "Retar". Notificaciones se agregó para que
 * el FAB quede realmente centrado (antes eran 5 pestañas, 2 contra 3, y el
 * botón se veía corrido) — se ubicó justo antes de Perfil, replicando dónde
 * vive el ícono de actividad/notificaciones en TikTok/Instagram/Facebook, en
 * vez de cerca de Inicio.
 */
@Composable
fun BottomNavBar(
    seleccionado: ItemBarraInferior?,
    onSeleccionar: (ItemBarraInferior) -> Unit = {},
    onRetar: () -> Unit = {},
) {
    // El de Apuestas solo cuenta novedades de apuestas (invitación,
    // comprobante, disputa, saldado) — pedirlo con solo_apuestas=true evita
    // que un reto nuevo (que no tiene nada que ver con esta pestaña) encienda
    // el número acá y "mienta" sobre qué hay para ver. El de Notificaciones
    // es a propósito el conteo total sin filtrar (mismo dato que antes vivía
    // en la campana de Home, ahora acá).
    var novedadesApuestas by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        val resultado = safeApiCall { RetrofitClient.notificacionesService.contarNoLeidas(soloApuestas = true) }
        novedadesApuestas = (resultado as? ApiResult.Exito)?.datos?.conteo ?: 0
    }
    var novedadesTotales by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        val resultado = safeApiCall { RetrofitClient.notificacionesService.contarNoLeidas() }
        novedadesTotales = (resultado as? ApiResult.Exito)?.datos?.conteo ?: 0
    }

    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ItemBarra(Icons.Filled.Home, "Inicio", seleccionado == ItemBarraInferior.INICIO, modifier = Modifier.weight(1f)) {
                onSeleccionar(ItemBarraInferior.INICIO)
            }
            ItemBarra(Icons.Filled.EmojiEvents, "Ranking", seleccionado == ItemBarraInferior.RANKING, modifier = Modifier.weight(1f)) {
                onSeleccionar(ItemBarraInferior.RANKING)
            }
            ItemBarra(Icons.Filled.SportsHandball, "Retos", seleccionado == ItemBarraInferior.RETOS, modifier = Modifier.weight(1f)) {
                onSeleccionar(ItemBarraInferior.RETOS)
            }
            FabRetar(onClick = onRetar)
            ItemBarra(
                Icons.Filled.Handshake,
                "Apuestas",
                seleccionado == ItemBarraInferior.APUESTAS,
                contador = novedadesApuestas,
                modifier = Modifier.weight(1f),
            ) {
                onSeleccionar(ItemBarraInferior.APUESTAS)
            }
            ItemBarra(
                Icons.Filled.Notifications,
                "Avisos",
                seleccionado == ItemBarraInferior.NOTIFICACIONES,
                contador = novedadesTotales,
                contentDescription = "Notificaciones",
                modifier = Modifier.weight(1f),
            ) {
                onSeleccionar(ItemBarraInferior.NOTIFICACIONES)
            }
            ItemBarra(Icons.Filled.Person, "Perfil", seleccionado == ItemBarraInferior.PERFIL, modifier = Modifier.weight(1f)) {
                onSeleccionar(ItemBarraInferior.PERFIL)
            }
        }
    }
}

@Composable
private fun ItemBarra(
    icono: ImageVector,
    etiqueta: String,
    activo: Boolean,
    contador: Int = 0,
    contentDescription: String = etiqueta,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val fondo = if (activo) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    val contenido = if (activo) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .offset(y = if (activo) (-4).dp else 0.dp)
                .then(if (activo) Modifier.shadow(3.dp, RoundedCornerShape(12.dp)) else Modifier)
                .clip(RoundedCornerShape(12.dp))
                .background(fondo)
                .padding(horizontal = 6.dp, vertical = 6.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BadgedBox(
                    badge = {
                        if (contador > 0) {
                            Badge(containerColor = MaterialTheme.colorScheme.error) {
                                Text(if (contador > 9) "9+" else contador.toString())
                            }
                        }
                    },
                ) {
                    Icon(icono, contentDescription = contentDescription, tint = contenido, modifier = Modifier.size(24.dp))
                }
                Text(etiqueta, style = CapsLabelTextStyle, color = contenido, maxLines = 1, softWrap = false)
            }
        }
    }
}

@Composable
private fun FabRetar(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val presionado by interaction.collectIsPressedAsState()
    val escala by animateFloatAsState(targetValue = if (presionado) 0.9f else 1f, label = "escalaFabRetar")

    Box(
        modifier = Modifier
            .offset(y = (-18).dp)
            .scale(escala)
            .size(58.dp)
            .shadow(6.dp, CircleShape)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.tertiary)
            .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Filled.SportsTennis, contentDescription = "Retar", tint = MaterialTheme.colorScheme.onTertiary, modifier = Modifier.size(26.dp))
    }
}
