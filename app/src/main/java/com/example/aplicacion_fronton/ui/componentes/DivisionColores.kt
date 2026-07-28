package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle

/** División estilo LoL (hierro/bronce/plata/oro/platino) — calculada por el
 * backend a partir del elo. Compartido entre `RankingScreen` (badge en cada
 * fila) y `SubidaDivisionScreen` (celebración de ascenso). */
val ORDEN_DIVISIONES = listOf("hierro", "bronce", "plata", "oro", "platino")

fun colorDivision(division: String): Color = when (division) {
    "hierro" -> Color(0xFF6B7280)
    "bronce" -> Color(0xFFC97B4A)
    "plata" -> Color(0xFFB0B7C3)
    "oro" -> Color(0xFFFFD54A)
    "platino" -> Color(0xFF4FD8D8)
    else -> Color(0xFF6B7280)
}

@Composable
fun InsigniaDivision(division: String) {
    val color = colorDivision(division)
    Text(
        division.uppercase(),
        style = CapsLabelTextStyle.copy(fontSize = 9.sp),
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, color, RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}
