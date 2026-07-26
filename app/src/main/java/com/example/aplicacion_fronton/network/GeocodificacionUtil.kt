package com.example.aplicacion_fronton.network

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/** Convierte lat/lng en una dirección legible ("Av. Salaverry 123, Chiclayo")
 * para autocompletar el campo de texto de la cancha apenas se elige un punto
 * en el mapa — usa el `Geocoder` del propio Android (vía Play Services), no
 * hace falta habilitar ninguna API de Google Cloud nueva ni gastar cuota de
 * Maps. La llamada es de red/bloqueante, por eso corre en `Dispatchers.IO`.
 * Devuelve null si no se pudo resolver (sin Play Services, sin conexión,
 * punto en medio del mar, etc.) — el usuario siempre puede escribirla a mano. */
suspend fun direccionDesdeCoordenadas(context: Context, latitud: Double, longitud: Double): String? =
    withContext(Dispatchers.IO) {
        try {
            @Suppress("DEPRECATION") // La versión con callback async es API 33+; minSdk 24 todavía necesita esta.
            val direcciones = Geocoder(context, Locale.getDefault()).getFromLocation(latitud, longitud, 1)
            direcciones?.firstOrNull()?.getAddressLine(0)
        } catch (e: Exception) {
            null
        }
    }
