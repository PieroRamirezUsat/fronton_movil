package com.example.aplicacion_fronton.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Notifica a toda la app cuando el token expira o deja de ser válido (401 global),
 * para que la UI reaccione (cerrar sesión, volver al login) sin que cada pantalla
 * tenga que revisar el código de error por su cuenta. */
object AuthEventBus {
    private val _sesionExpirada = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sesionExpirada: SharedFlow<Unit> = _sesionExpirada.asSharedFlow()

    fun notificarSesionExpirada() {
        _sesionExpirada.tryEmit(Unit)
    }
}
