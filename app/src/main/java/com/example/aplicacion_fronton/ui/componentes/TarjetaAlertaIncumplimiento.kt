package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.aplicacion_fronton.model.dto.CompromisoHistorialItemDto
import com.example.aplicacion_fronton.model.dto.EstadoCompromiso
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import kotlinx.coroutines.delay

/**
 * Alerta factual de cumplimiento — se muestra antes de retar o comprometerse
 * con alguien que tiene compromisos sin saldar en su historial (mockups
 * `alerta_de_incumplimiento_versus` y `alerta_de_incumplimiento_compromiso`,
 * mismo concepto en ambos). Informativa, no bloqueante: el usuario decide si
 * sigue o no. Tocarla abre el detalle itemizado (`DialogHistorialCumplimiento`).
 */
@Composable
fun TarjetaAlertaIncumplimiento(usuarioId: Int, nombreRival: String, incumplidos: Int, modifier: Modifier = Modifier) {
    if (incumplidos <= 0) return
    var mostrarDetalle by remember { mutableStateOf(false) }

    // Shake corto 500ms después de aparecer — llama la atención sobre una
    // advertencia real sin bloquear nada (documentado en la plantilla
    // `alerta_de_incumplimiento_compromiso`, nunca implementado hasta ahora).
    val offsetXShake = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(500)
        for (valor in listOf(-6f, 6f, -4f, 4f, 0f)) {
            offsetXShake.animateTo(valor, tween(40))
        }
    }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .fillMaxWidth()
            .offset(x = offsetXShake.value.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
            .border(2.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(12.dp))
            .clickable { mostrarDetalle = true }
            .padding(16.dp),
    ) {
        Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 2.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                buildString {
                    append("Ojo: $nombreRival tiene ")
                    append(if (incumplidos == 1) "1 apuesta sin saldar" else "$incumplidos apuestas sin saldar")
                    append(" en su historial.")
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Esto es un acuerdo informal entre ustedes, la decisión de continuar es tuya. Toca para ver el detalle.",
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
            )
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = "Ver detalle", tint = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 2.dp))
    }

    if (mostrarDetalle) {
        DialogHistorialCumplimiento(usuarioId = usuarioId, nombre = nombreRival, onCerrar = { mostrarDetalle = false })
    }
}

@Composable
private fun DialogHistorialCumplimiento(usuarioId: Int, nombre: String, onCerrar: () -> Unit) {
    var historial by remember { mutableStateOf<List<CompromisoHistorialItemDto>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(usuarioId) {
        when (val resultado = safeApiCall { RetrofitClient.usuariosService.obtenerHistorialCumplimiento(usuarioId) }) {
            is ApiResult.Exito -> historial = resultado.datos
            is ApiResult.Error -> error = resultado.mensaje
        }
    }

    Dialog(onDismissRequest = onCerrar) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("HISTORIAL DE $nombre".uppercase(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            val listaActual = historial
            when {
                error != null -> Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error ?: "No se pudo cargar el historial.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
                listaActual == null -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                listaActual.isEmpty() -> Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sin historial itemizado todavía.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
                else -> LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(listaActual, key = { it.id }) { item -> FilaHistorialCumplimiento(item) }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            TextButton(onClick = onCerrar, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text("CERRAR", style = CapsLabelTextStyle)
            }
        }
    }
}

@Composable
private fun FilaHistorialCumplimiento(item: CompromisoHistorialItemDto) {
    val esSaldado = item.estado == EstadoCompromiso.SALDADO
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Icon(
            if (esSaldado) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = if (esSaldado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 2.dp).size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.descripcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "con ${item.contraparte_nombre} · ${formatearFechaHistorial(item.fecha)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            if (esSaldado) "SALDADO" else "DISPUTA",
            style = CapsLabelTextStyle.copy(fontSize = 9.sp),
            color = if (esSaldado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
        )
    }
}

private fun formatearFechaHistorial(iso: String): String = try {
    val partes = iso.substringBefore("T").split("-")
    "${partes[2]}/${partes[1]}/${partes[0]}"
} catch (e: Exception) {
    iso
}
