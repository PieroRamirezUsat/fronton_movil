package com.example.aplicacion_fronton.ui.compromisos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.model.dto.EstadoCompromiso
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonCerrar
import com.example.aplicacion_fronton.ui.componentes.CargandoPelotita
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import com.example.aplicacion_fronton.ui.theme.NumericTextStyle

private val mesesAbrevDetalleCompromiso = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")

private fun formatearFechaDetalleCompromiso(iso: String): String = try {
    val fecha = iso.substringBefore("T")
    val hora = iso.substringAfter("T").take(5)
    val partes = fecha.split("-")
    "${partes[2]} ${mesesAbrevDetalleCompromiso[partes[1].toInt() - 1]}, $hora"
} catch (e: Exception) {
    iso
}

private fun etiquetaEstado(estado: EstadoCompromiso): Pair<String, Boolean> = when (estado) {
    EstadoCompromiso.PENDIENTE_INVITACION -> "PENDIENTE DE RESPUESTA" to false
    EstadoCompromiso.ACEPTADO -> "EN CURSO" to false
    EstadoCompromiso.RECHAZADO -> "RECHAZADO" to false
    EstadoCompromiso.SALDADO -> "SALDADO" to true
    EstadoCompromiso.DISPUTA -> "EN DISPUTA" to false
}

@Composable
fun DetalleCompromisoScreen(
    compromisoId: Int,
    onCerrar: () -> Unit,
    onVerVersus: (Int) -> Unit,
    viewModel: DetalleCompromisoViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(compromisoId) { viewModel.cargar(compromisoId) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(12.dp), horizontalArrangement = Arrangement.End) {
                BotonCerrar(onClick = onCerrar)
            }
        },
    ) { padding ->
        when (val actual = estado) {
            is DetalleCompromisoState.Cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CargandoPelotita()
            }
            is DetalleCompromisoState.Error -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(actual.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            is DetalleCompromisoState.Exito -> ContenidoDetalleCompromiso(actual, padding, onVerVersus)
        }
    }
}

@Composable
private fun ContenidoDetalleCompromiso(
    estado: DetalleCompromisoState.Exito,
    padding: PaddingValues,
    onVerVersus: (Int) -> Unit,
) {
    val c = estado.compromiso
    val (textoEstado, esPositivo) = etiquetaEstado(c.estado)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            val foto = estado.otroFotoUrl.urlCompletaFoto()
            if (foto != null) {
                AsyncImage(model = foto, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize().clip(CircleShape))
            } else {
                Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(44.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("vs. ${estado.otroNombre}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(
            textoEstado,
            style = CapsLabelTextStyle.copy(fontWeight = FontWeight.Bold),
            color = if (esPositivo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background((if (esPositivo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary).copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )

        // Resultado de la apuesta — solo cuando es derivable (creador e
        // invitado jugaron en equipos opuestos del versus vinculado y ya está
        // confirmado). Mismo lenguaje de color primary/tertiary que ya usa
        // toda la app para victoria/derrota (Home, la tarjeta de la lista).
        if (estado.esGanador != null && estado.versus != null) {
            Spacer(Modifier.height(20.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background((if (estado.esGanador) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary).copy(alpha = 0.12f))
                    .border(2.dp, (if (estado.esGanador) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    if (estado.esGanador) "GANASTE ESTA APUESTA" else "PERDISTE ESTA APUESTA",
                    style = CapsLabelTextStyle.copy(fontWeight = FontWeight.Bold),
                    color = if (estado.esGanador) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Marcador del reto: ${estado.versus.sets_jugador1}-${estado.versus.sets_jugador2}",
                    style = NumericTextStyle.copy(fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = { onVerVersus(estado.versus.id) }) {
                    Icon(Icons.Filled.SportsTennis, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("VER EL RETO", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // "EL ACUERDO" — mismo patrón visual que InvitacionCompromisoScreen,
        // acá en modo solo-lectura (sin botones de aceptar/rechazar).
        Spacer(Modifier.height(20.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                "EL ACUERDO",
                style = CapsLabelTextStyle,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
                    .zIndex(1f),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.12f))
                    .border(2.dp, MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(12.dp))
                    .padding(20.dp),
            ) {
                Text(
                    "\"${c.descripcion}\"",
                    style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (estado.pagadoPorNombre != null) {
            Spacer(Modifier.height(16.dp))
            FilaDatoCompromiso("Comprobante subido por", estado.pagadoPorNombre)
        }
        if (estado.confirmadoPorNombre != null) {
            Spacer(Modifier.height(8.dp))
            FilaDatoCompromiso("Confirmado por", estado.confirmadoPorNombre)
        }
        Spacer(Modifier.height(8.dp))
        FilaDatoCompromiso("Última actualización", formatearFechaDetalleCompromiso(c.updated_at))

        if (c.comprobante_url != null) {
            Spacer(Modifier.height(20.dp))
            Text("COMPROBANTE", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                val foto = c.comprobante_url.urlCompletaFoto()
                if (foto != null) {
                    AsyncImage(model = foto, contentDescription = "Comprobante", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(8.dp))
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Este es un acuerdo informal entre ustedes dos, la app no procesa pagos ni garantiza el cumplimiento.",
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FilaDatoCompromiso(etiqueta: String, valor: String) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(valor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
