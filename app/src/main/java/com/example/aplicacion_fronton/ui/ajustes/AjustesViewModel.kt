package com.example.aplicacion_fronton.ui.ajustes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.ActualizarGeneroRequestDto
import com.example.aplicacion_fronton.model.dto.ActualizarPerfilRequestDto
import com.example.aplicacion_fronton.model.dto.CambiarPasswordRequestDto
import com.example.aplicacion_fronton.model.dto.CategoriaEdad
import com.example.aplicacion_fronton.model.dto.Genero
import com.example.aplicacion_fronton.model.dto.UsuarioDto
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AjustesState {
    data object Cargando : AjustesState()
    data class Exito(val usuario: UsuarioDto) : AjustesState()
    data class Error(val mensaje: String) : AjustesState()
}

sealed class GuardadoState {
    data object Ocioso : GuardadoState()
    data object Guardando : GuardadoState()
    data object Guardado : GuardadoState()
    data class Error(val mensaje: String) : GuardadoState()
}

sealed class EliminarCuentaState {
    data object Ocioso : EliminarCuentaState()
    data object Eliminando : EliminarCuentaState()
    data object Eliminada : EliminarCuentaState()
    data class Error(val mensaje: String) : EliminarCuentaState()
}

sealed class CambiarPasswordState {
    data object Ocioso : CambiarPasswordState()
    data object Guardando : CambiarPasswordState()
    data object Guardado : CambiarPasswordState()
    data class Error(val mensaje: String) : CambiarPasswordState()
}

class AjustesViewModel : ViewModel() {
    private val _estado = MutableStateFlow<AjustesState>(AjustesState.Cargando)
    val estado: StateFlow<AjustesState> = _estado.asStateFlow()

    private val _guardado = MutableStateFlow<GuardadoState>(GuardadoState.Ocioso)
    val guardado: StateFlow<GuardadoState> = _guardado.asStateFlow()

    private val _eliminarCuenta = MutableStateFlow<EliminarCuentaState>(EliminarCuentaState.Ocioso)
    val eliminarCuenta: StateFlow<EliminarCuentaState> = _eliminarCuenta.asStateFlow()

    private val _cambiarPassword = MutableStateFlow<CambiarPasswordState>(CambiarPasswordState.Ocioso)
    val cambiarPassword: StateFlow<CambiarPasswordState> = _cambiarPassword.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = AjustesState.Cargando
            val resultado = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            _estado.value = when (resultado) {
                is ApiResult.Exito -> AjustesState.Exito(resultado.datos)
                is ApiResult.Error -> AjustesState.Error(resultado.mensaje)
            }
        }
    }

    fun guardarPerfil(club: String?, categoriaEdad: CategoriaEdad, genero: Genero) {
        viewModelScope.launch {
            _guardado.value = GuardadoState.Guardando
            val resultado = safeApiCall {
                RetrofitClient.usuariosService.actualizarPerfil(ActualizarPerfilRequestDto(club, categoriaEdad))
            }
            when (resultado) {
                is ApiResult.Exito -> {
                    val resultadoGenero = safeApiCall {
                        RetrofitClient.usuariosService.actualizarGenero(ActualizarGeneroRequestDto(genero))
                    }
                    when (resultadoGenero) {
                        is ApiResult.Exito -> {
                            _estado.value = AjustesState.Exito(resultadoGenero.datos)
                            _guardado.value = GuardadoState.Guardado
                        }
                        is ApiResult.Error -> _guardado.value = GuardadoState.Error(resultadoGenero.mensaje)
                    }
                }
                is ApiResult.Error -> _guardado.value = GuardadoState.Error(resultado.mensaje)
            }
        }
    }

    fun limpiarGuardado() {
        _guardado.value = GuardadoState.Ocioso
    }

    fun cambiarPassword(actual: String, nueva: String) {
        viewModelScope.launch {
            _cambiarPassword.value = CambiarPasswordState.Guardando
            val resultado = safeApiCall {
                RetrofitClient.authService.cambiarPassword(CambiarPasswordRequestDto(actual, nueva))
            }
            _cambiarPassword.value = when (resultado) {
                is ApiResult.Exito -> CambiarPasswordState.Guardado
                is ApiResult.Error -> CambiarPasswordState.Error(resultado.mensaje)
            }
        }
    }

    fun limpiarCambiarPassword() {
        _cambiarPassword.value = CambiarPasswordState.Ocioso
    }

    fun eliminarCuenta() {
        viewModelScope.launch {
            _eliminarCuenta.value = EliminarCuentaState.Eliminando
            val resultado = safeApiCall { RetrofitClient.usuariosService.eliminarCuenta() }
            _eliminarCuenta.value = when (resultado) {
                is ApiResult.Exito -> {
                    RetrofitClient.tokenStore.limpiar()
                    EliminarCuentaState.Eliminada
                }
                is ApiResult.Error -> EliminarCuentaState.Error(resultado.mensaje)
            }
        }
    }
}
