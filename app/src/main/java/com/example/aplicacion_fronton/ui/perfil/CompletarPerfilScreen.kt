package com.example.aplicacion_fronton.ui.perfil

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aplicacion_fronton.model.dto.ActualizarGeneroRequestDto
import com.example.aplicacion_fronton.model.dto.Genero
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import kotlinx.coroutines.launch

/** Onboarding bloqueante para cuentas creadas antes de que el campo de
 * género existiera — sin flecha atrás ni forma de saltarlo (mismo criterio
 * que Splash), aparece una sola vez por `destinoTrasIniciarSesion()`. */
@Composable
fun CompletarPerfilScreen(onGeneroGuardado: () -> Unit) {
    var genero by remember { mutableStateOf(Genero.MASCULINO) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .safeDrawingPadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "UN ÚLTIMO DATO",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            "Agregamos el ranking por género — completá esto para poder seguir usando Frontón.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        )
        Text("GÉNERO", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                .padding(4.dp),
        ) {
            listOf(Genero.MASCULINO to "Masculino", Genero.FEMENINO to "Femenino").forEach { (valor, etiqueta) ->
                val activo = genero == valor
                Text(
                    etiqueta,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activo) MaterialTheme.colorScheme.surfaceBright else Color.Transparent)
                        .clickable { genero = valor }
                        .padding(vertical = 14.dp),
                )
            }
        }

        AnimatedVisibility(visible = error != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Text(
                error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        BotonTactil(
            texto = "Continuar",
            icono = Icons.Filled.CheckCircle,
            cargando = cargando,
            onClick = {
                cargando = true
                error = null
                scope.launch {
                    val resultado = safeApiCall {
                        RetrofitClient.usuariosService.actualizarGenero(ActualizarGeneroRequestDto(genero))
                    }
                    cargando = false
                    when (resultado) {
                        is ApiResult.Exito -> onGeneroGuardado()
                        is ApiResult.Error -> error = resultado.mensaje
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
        )
    }
}
