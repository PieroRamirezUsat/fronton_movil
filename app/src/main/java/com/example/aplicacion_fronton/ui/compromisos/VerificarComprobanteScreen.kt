package com.example.aplicacion_fronton.ui.compromisos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.network.urlCompletaFoto
import com.example.aplicacion_fronton.ui.componentes.BotonCerrar
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle

@Composable
fun VerificarComprobanteScreen(
    compromisoId: Int,
    onCerrar: () -> Unit,
    onVerificado: () -> Unit,
    onVictoria: (compromisoId: Int) -> Unit,
    viewModel: VerificarComprobanteViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val vistoBueno by viewModel.vistoBueno.collectAsStateWithLifecycle()

    LaunchedEffect(compromisoId) { viewModel.cargar(compromisoId) }
    LaunchedEffect(vistoBueno) {
        val actual = vistoBueno
        if (actual is VistoBuenoEstado.Hecho) {
            val esGanador = (estado as? VerificarComprobanteEstado.Exito)?.esGanador
            if (actual.confirmado && esGanador == true) {
                onVictoria(compromisoId)
            } else {
                onVerificado()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("VERIFICAR PAGO", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    BotonCerrar(onClick = onCerrar)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest, thickness = 2.dp)
            }
        },
    ) { padding ->
        when (val actual = estado) {
            is VerificarComprobanteEstado.Cargando -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is VerificarComprobanteEstado.Error -> Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text(actual.mensaje, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
            }
            is VerificarComprobanteEstado.Exito -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 16.dp),
            ) {
                val c = actual.compromiso
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(16.dp),
                ) {
                    Text("APUESTA DEL PARTIDO", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.height(4.dp))
                    Text(c.descripcion, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("De: ${actual.quienSubioNombre}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(20.dp))
                Text("COMPROBANTE DE TRANSFERENCIA", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    val foto = c.comprobante_url.urlCompletaFoto()
                    if (foto != null) {
                        AsyncImage(model = foto, contentDescription = "Comprobante", contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(8.dp))
                    } else {
                        Text("Sin comprobante todavía.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (vistoBueno is VistoBuenoEstado.Error) {
                    Spacer(Modifier.height(12.dp))
                    Text((vistoBueno as VistoBuenoEstado.Error).mensaje, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }

                Spacer(Modifier.height(16.dp))
                val enviando = vistoBueno is VistoBuenoEstado.Enviando
                BotonTactil(
                    texto = "Confirmar recibido",
                    icono = Icons.Filled.CheckCircle,
                    cargando = enviando,
                    enabled = !enviando,
                    onClick = { viewModel.darVistoBueno(compromisoId, true) },
                    colorContenedor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                BotonTactil(
                    texto = "No lo recibí",
                    icono = Icons.Filled.Report,
                    cargando = false,
                    enabled = !enviando,
                    onClick = { viewModel.darVistoBueno(compromisoId, false) },
                    colorContenedor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
