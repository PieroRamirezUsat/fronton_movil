package com.example.aplicacion_fronton.ui.notificaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.NotificacionDto
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NotificacionesState {
    data object Cargando : NotificacionesState()
    data class Exito(val notificaciones: List<NotificacionDto>) : NotificacionesState()
    data class Error(val mensaje: String) : NotificacionesState()
}

class NotificacionesViewModel : ViewModel() {
    private val _estado = MutableStateFlow<NotificacionesState>(NotificacionesState.Cargando)
    val estado: StateFlow<NotificacionesState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = NotificacionesState.Cargando
            val resultado = safeApiCall { RetrofitClient.notificacionesService.listarMisNotificaciones() }
            _estado.value = when (resultado) {
                is ApiResult.Exito -> NotificacionesState.Exito(resultado.datos)
                is ApiResult.Error -> NotificacionesState.Error(resultado.mensaje)
            }
        }
    }

    fun marcarLeida(notificacionId: Int) {
        val actual = _estado.value
        if (actual !is NotificacionesState.Exito) return
        val notificacion = actual.notificaciones.firstOrNull { it.id == notificacionId }
        if (notificacion == null || notificacion.leida) return

        // Optimista: se marca leída en pantalla al toque, sin esperar la red.
        _estado.value = actual.copy(
            notificaciones = actual.notificaciones.map { if (it.id == notificacionId) it.copy(leida = true) else it },
        )
        viewModelScope.launch {
            safeApiCall { RetrofitClient.notificacionesService.marcarLeida(notificacionId) }
        }
    }

    /** Vacía la sección "NUEVAS" de un toque — antes la única forma de que una
     * notificación dejara de estar destacada era abrirla una por una, y con
     * varios avisos acumulados la pantalla se llenaba de tarjetas resaltadas. */
    fun marcarTodasLeidas() {
        val actual = _estado.value
        if (actual !is NotificacionesState.Exito) return
        _estado.value = actual.copy(notificaciones = actual.notificaciones.map { it.copy(leida = true) })
        viewModelScope.launch {
            safeApiCall { RetrofitClient.notificacionesService.marcarTodasLeidas() }
        }
    }
}
