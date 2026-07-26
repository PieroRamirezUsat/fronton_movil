package com.example.aplicacion_fronton.ui.compromisos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.CompromisoDto
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class InvitacionState {
    data object Cargando : InvitacionState()
    data class Exito(
        val compromiso: CompromisoDto,
        val miId: Int,
        val otroNombre: String,
        val otroFotoUrl: String?,
        val fechaVersus: String,
        val canchaVersus: String?,
    ) : InvitacionState()
    data class Error(val mensaje: String) : InvitacionState()
}

sealed class RespuestaCompromisoState {
    data object Ocioso : RespuestaCompromisoState()
    data object Enviando : RespuestaCompromisoState()
    data class Hecho(val aceptado: Boolean) : RespuestaCompromisoState()
    data class Error(val mensaje: String) : RespuestaCompromisoState()
}

class InvitacionCompromisoViewModel : ViewModel() {
    private val _estado = MutableStateFlow<InvitacionState>(InvitacionState.Cargando)
    val estado: StateFlow<InvitacionState> = _estado.asStateFlow()

    private val _respuesta = MutableStateFlow<RespuestaCompromisoState>(RespuestaCompromisoState.Ocioso)
    val respuesta: StateFlow<RespuestaCompromisoState> = _respuesta.asStateFlow()

    fun cargar(compromisoId: Int) {
        viewModelScope.launch {
            _estado.value = InvitacionState.Cargando

            val perfil = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            if (perfil is ApiResult.Error) {
                _estado.value = InvitacionState.Error(perfil.mensaje)
                return@launch
            }
            val mi = (perfil as ApiResult.Exito).datos

            val compromisoResultado = safeApiCall { RetrofitClient.compromisosService.obtenerCompromiso(compromisoId) }
            if (compromisoResultado is ApiResult.Error) {
                _estado.value = InvitacionState.Error(compromisoResultado.mensaje)
                return@launch
            }
            val compromiso = (compromisoResultado as ApiResult.Exito).datos

            val otroId = if (compromiso.creador_id == mi.id) compromiso.invitado_id else compromiso.creador_id
            val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val otro = (ranking as? ApiResult.Exito)?.datos?.firstOrNull { it.id == otroId }

            val versusResultado = safeApiCall { RetrofitClient.versusService.obtenerVersus(compromiso.versus_id) }
            val versus = (versusResultado as? ApiResult.Exito)?.datos

            _estado.value = InvitacionState.Exito(
                compromiso = compromiso,
                miId = mi.id,
                otroNombre = otro?.nombre ?: "Jugador #$otroId",
                otroFotoUrl = otro?.foto_url,
                fechaVersus = versus?.fecha_hora ?: compromiso.created_at,
                canchaVersus = versus?.cancha,
            )
        }
    }

    fun responder(compromisoId: Int, aceptar: Boolean) {
        viewModelScope.launch {
            _respuesta.value = RespuestaCompromisoState.Enviando
            val resultado = safeApiCall { RetrofitClient.compromisosService.responder(compromisoId, aceptar) }
            _respuesta.value = when (resultado) {
                is ApiResult.Exito -> RespuestaCompromisoState.Hecho(aceptar)
                is ApiResult.Error -> RespuestaCompromisoState.Error(resultado.mensaje)
            }
        }
    }
}
