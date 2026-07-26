package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Botón de "Reintentar" de los estados de error — antes era un `Button` M3
 * plano repetido igual en ~6 pantallas, sin ningún feedback táctil propio ni
 * relación visual con el resto de CTAs de la app.
 */
@Composable
fun BotonReintentar(onClick: () -> Unit, modifier: Modifier = Modifier) {
    // Altura por defecto de BotonTactil (52dp) a propósito -- el texto interno
    // usa un tamaño de letra fijo (headlineSmall) sin importar la altura que
    // se le pida, así que forzarla más chica aprieta el texto/ícono (mismo
    // bug ya encontrado y corregido en el botón de Buscar Versus).
    BotonTactil(
        texto = "Reintentar",
        icono = Icons.Filled.Refresh,
        onClick = onClick,
        colorContenedor = MaterialTheme.colorScheme.tertiary,
        modifier = modifier.padding(top = 16.dp),
    )
}
