package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle

/**
 * Pill de filtro/segmento reutilizable (llena+primary si está activa,
 * surfaceContainerHigh si no) con burbuja de contador opcional — nace en
 * Apuestas (RECIBIDOS/ENVIADOS/EN CURSO/SALDADOS) y se comparte con
 * Notificaciones para que ambas pantallas hablen el mismo idioma visual.
 */
@Composable
fun PillFiltro(texto: String, contador: Int, activa: Boolean, onClick: () -> Unit) {
    val colorFondo by animateColorAsState(
        targetValue = if (activa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(200),
        label = "colorFondoPill",
    )
    val colorTexto by animateColorAsState(
        targetValue = if (activa) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(200),
        label = "colorTextoPill",
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colorFondo)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(texto, style = CapsLabelTextStyle, color = colorTexto)
        if (contador > 0) {
            Spacer(Modifier.width(6.dp))
            val colorBurbuja by animateColorAsState(
                targetValue = if (activa) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.tertiary,
                animationSpec = tween(200),
                label = "colorBurbujaPill",
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colorBurbuja)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(contador.toString(), style = CapsLabelTextStyle.copy(fontSize = 10.sp), color = Color.White)
            }
        }
    }
}
