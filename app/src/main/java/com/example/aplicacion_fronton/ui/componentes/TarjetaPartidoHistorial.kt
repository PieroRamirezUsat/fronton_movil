package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.model.dto.Modalidad
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.retos.PartidoHistorialUi
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle

private val mesesAbrevPartido = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")

fun formatearFechaPartido(iso: String): String = try {
    val fecha = iso.substringBefore("T")
    val partes = fecha.split("-")
    "${partes[2]} ${mesesAbrevPartido[partes[1].toInt() - 1]}, ${partes[0]}"
} catch (e: Exception) {
    iso
}

private fun Modifier.franjaIzquierda(color: Color): Modifier = this.drawBehind {
    drawRect(color = color, size = Size(4.dp.toPx(), size.height))
}

/** Fila de un partido confirmado (rival, fecha, victoria/derrota, marcador) —
 * antes vivía privada y duplicada en `HistorialVersusScreen.kt`; se extrae
 * acá para reusarla también en el historial público del perfil de otro
 * jugador, mismo criterio ya aplicado con `SeccionEnCascada`/`ConfettiOverlay`. */
@Composable
fun TarjetaPartidoHistorial(partido: PartidoHistorialUi, onClick: () -> Unit) {
    val colorAcento = if (partido.gane) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))
            .franjaIzquierda(colorAcento)
            .clickable(onClick = onClick)
            .padding(start = 20.dp, top = 14.dp, bottom = 14.dp, end = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                val foto = partido.rivalFotoUrl.urlCompletaFoto()
                if (foto != null) {
                    AsyncImage(model = foto, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(CircleShape))
                } else {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(partido.rivalNombre.uppercase(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                Text(
                    "${formatearFechaPartido(partido.fechaHora)} • ${if (partido.modalidad == Modalidad.DOBLES) "DOBLES" else "INDIVIDUAL"}",
                    style = CapsLabelTextStyle.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (partido.gane) "VICTORIA" else "DERROTA",
                    style = CapsLabelTextStyle.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = colorAcento,
                )
                if (partido.gane) {
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.Stars, contentDescription = null, tint = colorAcento, modifier = Modifier.size(14.dp))
                }
            }
            Text(
                "${partido.misSets} - ${partido.susSets}",
                style = NumericTextStyle.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
