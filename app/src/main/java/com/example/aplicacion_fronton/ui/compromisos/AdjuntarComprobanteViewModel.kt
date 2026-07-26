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
import okhttp3.MultipartBody

sealed class AdjuntarComprobanteEstado {
    data object Cargando : AdjuntarComprobanteEstado()
    data class Exito(val compromiso: CompromisoDto, val otroNombre: String) : AdjuntarComprobanteEstado()
    data class Error(val mensaje: String) : AdjuntarComprobanteEstado()
}

sealed class SubidaComprobanteEstado {
    data object Ocioso : SubidaComprobanteEstado()
    data object Subiendo : SubidaComprobanteEstado()
    data object Hecho : SubidaComprobanteEstado()
    data class Error(val mensaje: String) : SubidaComprobanteEstado()
}

class AdjuntarComprobanteViewModel : ViewModel() {
    private val _estado = MutableStateFlow<AdjuntarComprobanteEstado>(AdjuntarComprobanteEstado.Cargando)
    val estado: StateFlow<AdjuntarComprobanteEstado> = _estado.asStateFlow()

    private val _subida = MutableStateFlow<SubidaComprobanteEstado>(SubidaComprobanteEstado.Ocioso)
    val subida: StateFlow<SubidaComprobanteEstado> = _subida.asStateFlow()

    fun cargar(compromisoId: Int) {
        viewModelScope.launch {
            _estado.value = AdjuntarComprobanteEstado.Cargando

            val perfil = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            if (perfil is ApiResult.Error) {
                _estado.value = AdjuntarComprobanteEstado.Error(perfil.mensaje)
                return@launch
            }
            val mi = (perfil as ApiResult.Exito).datos

            val compromisoResultado = safeApiCall { RetrofitClient.compromisosService.obtenerCompromiso(compromisoId) }
            if (compromisoResultado is ApiResult.Error) {
                _estado.value = AdjuntarComprobanteEstado.Error(compromisoResultado.mensaje)
                return@launch
            }
            val compromiso = (compromisoResultado as ApiResult.Exito).datos

            val otroId = if (compromiso.creador_id == mi.id) compromiso.invitado_id else compromiso.creador_id
            val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val otro = (ranking as? ApiResult.Exito)?.datos?.firstOrNull { it.id == otroId }

            _estado.value = AdjuntarComprobanteEstado.Exito(compromiso, otro?.nombre ?: "Jugador #$otroId")
        }
    }

    fun subir(compromisoId: Int, parte: MultipartBody.Part) {
        viewModelScope.launch {
            _subida.value = SubidaComprobanteEstado.Subiendo
            val resultado = safeApiCall { RetrofitClient.compromisosService.subirComprobanteFoto(compromisoId, parte) }
            _subida.value = when (resultado) {
                is ApiResult.Exito -> SubidaComprobanteEstado.Hecho
                is ApiResult.Error -> SubidaComprobanteEstado.Error(resultado.mensaje)
            }
        }
    }
}
