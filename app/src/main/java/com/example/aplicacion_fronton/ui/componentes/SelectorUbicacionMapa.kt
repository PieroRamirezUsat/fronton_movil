package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

/** Coordenadas por defecto — el usuario prueba la app en Chiclayo, así que el
 * mapa arranca centrado ahí en vez de en un punto genérico que después
 * tendría que buscar cada vez que crea un reto. */
const val LATITUD_CHICLAYO_DEFECTO = -6.7714
const val LONGITUD_CHICLAYO_DEFECTO = -79.8409

/** Selector de ubicación a pantalla completa (`Dialog`, no embebido en el
 * formulario) — un mapa interactivo adentro de un `Column` con scroll
 * compite por el gesto de arrastrar con el scroll del formulario y se
 * vuelve muy difícil de manejar (reportado por el usuario); a pantalla
 * completa el mapa es el único contenido, sin nada con quien pelearse el
 * gesto, y es además el patrón estándar de cualquier selector de ubicación
 * en apps móviles (el propio Google Maps elige un lugar así). Sin
 * geolocalización del dispositivo a propósito (no se pidió) — arranca en
 * `latitudInicial`/`longitudInicial` y el usuario mueve el pin tocando. */
@Composable
fun SelectorUbicacionDialog(
    latitudInicial: Double,
    longitudInicial: Double,
    onConfirmar: (latitud: Double, longitud: Double) -> Unit,
    onCerrar: () -> Unit,
) {
    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        var latitud by remember { mutableStateOf(latitudInicial) }
        var longitud by remember { mutableStateOf(longitudInicial) }
        val estadoCamara = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(LatLng(latitudInicial, longitudInicial), 15f)
        }
        val estadoMarcador = rememberMarkerState(position = LatLng(latitudInicial, longitudInicial))

        Box(Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = estadoCamara,
                onMapClick = { latLng ->
                    latitud = latLng.latitude
                    longitud = latLng.longitude
                    estadoMarcador.position = latLng
                },
            ) {
                Marker(state = estadoMarcador)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                IconButton(onClick = onCerrar) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    "Tocá el mapa para marcar el lugar",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            BotonTactil(
                texto = "Confirmar ubicación",
                icono = Icons.Filled.CheckCircle,
                onClick = { onConfirmar(latitud, longitud) },
                colorContenedor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(20.dp),
            )
        }
    }
}
