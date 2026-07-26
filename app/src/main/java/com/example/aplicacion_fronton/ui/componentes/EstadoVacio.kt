package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Estado vacío genérico — antes existían 3 copias casi idénticas (Retos,
 * Apuestas, Buscar Versus) que solo diferían en ícono/texto, con el mismo
 * color de círculo en las 3. Eso hacía que dos secciones distintas de la app
 * se sintieran "superpuestas" al usuario — acá el color de círculo/ícono es
 * parametrizable justamente para que cada sección tenga su propia identidad.
 */
@Composable
fun EstadoVacio(
    icono: ImageVector,
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier,
    colorCirculo: Color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
    colorIcono: Color = MaterialTheme.colorScheme.primary,
    textoBoton: String? = null,
    iconoBoton: ImageVector = Icons.Filled.SportsTennis,
    colorBoton: Color = MaterialTheme.colorScheme.tertiary,
    onBoton: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(88.dp).clip(CircleShape).background(colorCirculo),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(titulo, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (textoBoton != null) {
            Spacer(Modifier.height(28.dp))
            BotonTactil(texto = textoBoton, icono = iconoBoton, onClick = onBoton, colorContenedor = colorBoton)
        }
    }
}
