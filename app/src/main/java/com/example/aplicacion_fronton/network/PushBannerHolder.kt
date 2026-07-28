package com.example.aplicacion_fronton.network

import com.example.aplicacion_fronton.model.dto.TipoNotificacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Puente entre `FrontonMessagingService.onMessageReceived` (que corre SIEMPRE,
 * con la app en primer o segundo plano) y un banner in-app — a diferencia de
 * `PushNotificacionHolder` (que solo se alimenta desde el TAP a la
 * notificación del sistema), esto se alimenta apenas llega el push, para
 * mostrar un aviso dentro de la propia UI mientras la app ya está abierta,
 * sin esperar a que el usuario mire la barra de notificaciones. */
object PushBannerHolder {
    data class Datos(
        val titulo: String,
        val mensaje: String,
        val tipo: TipoNotificacion?,
        val versusId: Int?,
        val compromisoId: Int?,
    )

    private val _actual = MutableStateFlow<Datos?>(null)
    val actual: StateFlow<Datos?> = _actual.asStateFlow()

    fun mostrar(datos: Datos) {
        _actual.value = datos
    }

    fun descartar() {
        _actual.value = null
    }
}
