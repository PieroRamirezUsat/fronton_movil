package com.example.aplicacion_fronton.ui.auth.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.CategoriaEdad
import com.example.aplicacion_fronton.model.dto.Genero
import com.example.aplicacion_fronton.model.dto.LoginRequestDto
import com.example.aplicacion_fronton.model.dto.ManoHabil
import com.example.aplicacion_fronton.model.dto.UsuarioCreateDto
import com.example.aplicacion_fronton.model.dto.UsuarioDto
import com.example.aplicacion_fronton.model.dto.GoogleCompletarRequestDto
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

sealed class RegistroState {
    data object Idle : RegistroState()
    data object Cargando : RegistroState()
    data class Exito(val usuario: UsuarioDto) : RegistroState()
    data class Error(val mensaje: String) : RegistroState()
}

class RegistroViewModel : ViewModel() {
    private val _estado = MutableStateFlow<RegistroState>(RegistroState.Idle)
    val estado: StateFlow<RegistroState> = _estado.asStateFlow()

    fun registrar(
        nombre: String,
        correo: String,
        password: String,
        jugadorLibre: Boolean,
        club: String,
        categoriaEdad: CategoriaEdad,
        manoHabil: ManoHabil,
        genero: Genero,
        fotoParte: MultipartBody.Part? = null,
    ) {
        if (nombre.isBlank() || correo.isBlank() || password.length < 8) {
            _estado.value = RegistroState.Error("Revisa tus datos: la contraseña necesita al menos 8 caracteres.")
            return
        }
        viewModelScope.launch {
            _estado.value = RegistroState.Cargando

            val datos = UsuarioCreateDto(
                nombre = nombre.trim(),
                correo = correo.trim(),
                password = password,
                club = if (jugadorLibre) null else club.trim().ifBlank { null },
                categoria_edad = categoriaEdad,
                mano_habil = manoHabil,
                genero = genero,
            )
            val registro = safeApiCall { RetrofitClient.authService.registrar(datos) }
            if (registro is ApiResult.Error) {
                _estado.value = RegistroState.Error(registro.mensaje)
                return@launch
            }

            // Registrar no devuelve token — iniciamos sesión automáticamente para
            // no obligar al usuario a escribir sus datos dos veces seguidas.
            val login = safeApiCall {
                RetrofitClient.authService.iniciarSesion(LoginRequestDto(correo.trim(), password))
            }
            if (login is ApiResult.Error) {
                _estado.value = RegistroState.Error(login.mensaje)
                return@launch
            }
            val usuarioLogueado = (login as ApiResult.Exito).datos
            RetrofitClient.tokenStore.guardarToken(usuarioLogueado.token)

            // Si eligió una foto local, se sube recién ahora que ya hay token —
            // si falla, no se pierde la cuenta recién creada, solo se queda sin foto.
            val usuarioFinal = if (fotoParte != null) {
                val subida = safeApiCall { RetrofitClient.usuariosService.subirFotoPerfil(fotoParte) }
                (subida as? ApiResult.Exito)?.datos ?: usuarioLogueado.data
            } else {
                usuarioLogueado.data
            }
            _estado.value = RegistroState.Exito(usuarioFinal)
        }
    }

    fun registrarConGoogle(
        idToken: String,
        jugadorLibre: Boolean,
        club: String,
        categoriaEdad: CategoriaEdad,
        manoHabil: ManoHabil,
        genero: Genero,
    ) {
        viewModelScope.launch {
            _estado.value = RegistroState.Cargando
            val datos = GoogleCompletarRequestDto(
                id_token = idToken,
                club = if (jugadorLibre) null else club.trim().ifBlank { null },
                categoria_edad = categoriaEdad,
                mano_habil = manoHabil,
                genero = genero,
            )
            val resultado = safeApiCall { RetrofitClient.authService.googleCompletar(datos) }
            _estado.value = when (resultado) {
                is ApiResult.Exito -> {
                    RetrofitClient.tokenStore.guardarToken(resultado.datos.token)
                    RegistroState.Exito(resultado.datos.data)
                }
                is ApiResult.Error -> RegistroState.Error(resultado.mensaje)
            }
        }
    }

    fun reiniciarEstado() {
        _estado.value = RegistroState.Idle
    }
}
