package com.example.aplicacion_fronton.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.SolicitarResetRequestDto
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OlvideContrasenaState {
    data object Idle : OlvideContrasenaState()
    data object Cargando : OlvideContrasenaState()
    data object Enviado : OlvideContrasenaState()
    data class Error(val mensaje: String) : OlvideContrasenaState()
}

class OlvideContrasenaViewModel : ViewModel() {
    private val _estado = MutableStateFlow<OlvideContrasenaState>(OlvideContrasenaState.Idle)
    val estado: StateFlow<OlvideContrasenaState> = _estado.asStateFlow()

    fun solicitar(correo: String) {
        if (correo.isBlank()) {
            _estado.value = OlvideContrasenaState.Error("Ingresa tu correo.")
            return
        }
        viewModelScope.launch {
            _estado.value = OlvideContrasenaState.Cargando
            // El backend responde el mismo mensaje genérico exista o no la
            // cuenta (no filtra si un correo está registrado) — acá solo se
            // trata como éxito de red, no hace falta leer el mensaje.
            val resultado = safeApiCall {
                RetrofitClient.authService.solicitarReset(SolicitarResetRequestDto(correo.trim()))
            }
            _estado.value = when (resultado) {
                is ApiResult.Exito -> OlvideContrasenaState.Enviado
                is ApiResult.Error -> OlvideContrasenaState.Error(resultado.mensaje)
            }
        }
    }
}
