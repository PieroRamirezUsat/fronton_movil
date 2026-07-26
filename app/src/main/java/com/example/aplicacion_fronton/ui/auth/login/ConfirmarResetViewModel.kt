package com.example.aplicacion_fronton.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.ConfirmarResetRequestDto
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ConfirmarResetState {
    data object Idle : ConfirmarResetState()
    data object Cargando : ConfirmarResetState()
    data object Exito : ConfirmarResetState()
    data class Error(val mensaje: String) : ConfirmarResetState()
}

class ConfirmarResetViewModel : ViewModel() {
    private val _estado = MutableStateFlow<ConfirmarResetState>(ConfirmarResetState.Idle)
    val estado: StateFlow<ConfirmarResetState> = _estado.asStateFlow()

    fun confirmar(correo: String, codigo: String, passwordNueva: String) {
        viewModelScope.launch {
            _estado.value = ConfirmarResetState.Cargando
            val resultado = safeApiCall {
                RetrofitClient.authService.confirmarReset(ConfirmarResetRequestDto(correo, codigo, passwordNueva))
            }
            _estado.value = when (resultado) {
                is ApiResult.Exito -> ConfirmarResetState.Exito
                is ApiResult.Error -> ConfirmarResetState.Error(resultado.mensaje)
            }
        }
    }
}
