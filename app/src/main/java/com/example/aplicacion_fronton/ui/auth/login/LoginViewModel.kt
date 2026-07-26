package com.example.aplicacion_fronton.ui.auth.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.GoogleIniciarRequestDto
import com.example.aplicacion_fronton.model.dto.LoginRequestDto
import com.example.aplicacion_fronton.model.dto.UsuarioDto
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.GoogleAuthResultado
import com.example.aplicacion_fronton.network.GoogleAuthService
import com.example.aplicacion_fronton.network.GooglePendingAuth
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    data object Idle : LoginState()
    data object Cargando : LoginState()
    data class Exito(val usuario: UsuarioDto) : LoginState()
    data object NecesitaCompletarPerfilGoogle : LoginState()
    data class Error(val mensaje: String) : LoginState()
}

class LoginViewModel : ViewModel() {
    private val _estado = MutableStateFlow<LoginState>(LoginState.Idle)
    val estado: StateFlow<LoginState> = _estado.asStateFlow()

    fun iniciarSesion(correo: String, password: String) {
        if (correo.isBlank() || password.isBlank()) {
            _estado.value = LoginState.Error("Completa correo y contraseña.")
            return
        }
        viewModelScope.launch {
            _estado.value = LoginState.Cargando
            val resultado = safeApiCall {
                RetrofitClient.authService.iniciarSesion(LoginRequestDto(correo.trim(), password))
            }
            _estado.value = when (resultado) {
                is ApiResult.Exito -> {
                    RetrofitClient.tokenStore.guardarToken(resultado.datos.token)
                    LoginState.Exito(resultado.datos.data)
                }
                is ApiResult.Error -> LoginState.Error(resultado.mensaje)
            }
        }
    }

    fun continuarConGoogle(context: Context) {
        viewModelScope.launch {
            _estado.value = LoginState.Cargando

            when (val credencial = GoogleAuthService.obtenerIdToken(context)) {
                is GoogleAuthResultado.Cancelado -> {
                    _estado.value = LoginState.Error(credencial.mensaje)
                    return@launch
                }
                is GoogleAuthResultado.Exito -> {
                    val resultado = safeApiCall {
                        RetrofitClient.authService.googleIniciar(GoogleIniciarRequestDto(credencial.idToken))
                    }
                    _estado.value = when (resultado) {
                        is ApiResult.Error -> LoginState.Error(resultado.mensaje)
                        is ApiResult.Exito -> {
                            val datos = resultado.datos
                            if (datos.nuevo) {
                                GooglePendingAuth.guardar(
                                    GooglePendingAuth.Datos(
                                        idToken = credencial.idToken,
                                        correo = datos.correo.orEmpty(),
                                        nombre = datos.nombre.orEmpty(),
                                        fotoUrl = datos.foto_url,
                                    ),
                                )
                                LoginState.NecesitaCompletarPerfilGoogle
                            } else {
                                RetrofitClient.tokenStore.guardarToken(datos.token!!)
                                LoginState.Exito(datos.data!!)
                            }
                        }
                    }
                }
            }
        }
    }

    fun reiniciarEstado() {
        _estado.value = LoginState.Idle
    }
}
