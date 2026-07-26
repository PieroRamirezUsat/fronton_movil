package com.example.aplicacion_fronton.network

import com.example.aplicacion_fronton.model.dto.TipoNotificacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Puente entre el tap a una notificación push (capturado como extras de un
 * `Intent` en `MainActivity`, tanto en frío como en `onNewIntent` con la app
 * ya abierta) y `AppNavigation`, que es quien de verdad sabe navegar —
 * `StateFlow` en vez de un simple nullable var para que `AppNavigation`
 * reaccione sola vía `collectAsState()` en los dos casos, no solo al abrir
 * la app de cero (mismo problema que resolvería un `RetoHolder`/
 * `PerfilJugadorHolder`, pero esos se consumen una sola vez al entrar a una
 * pantalla ya empujada, no de forma reactiva). */
object PushNotificacionHolder {
    data class Datos(
        val tipo: TipoNotificacion,
        val versusId: Int?,
        val compromisoId: Int?,
    )

    private val _pendiente = MutableStateFlow<Datos?>(null)
    val pendiente: StateFlow<Datos?> = _pendiente.asStateFlow()

    fun guardar(datos: Datos) {
        _pendiente.value = datos
    }

    fun consumir() {
        _pendiente.value = null
    }
}
