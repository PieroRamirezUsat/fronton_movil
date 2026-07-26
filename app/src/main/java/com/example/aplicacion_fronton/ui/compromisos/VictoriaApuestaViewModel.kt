package com.example.aplicacion_fronton.ui.compromisos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VictoriaApuestaUi(
    val descripcion: String,
    val rivalNombre: String,
    val rivalFotoUrl: String?,
)

sealed class VictoriaApuestaState {
    data object Cargando : VictoriaApuestaState()
    data class Exito(val datos: VictoriaApuestaUi) : VictoriaApuestaState()
    data class Error(val mensaje: String) : VictoriaApuestaState()
}

class VictoriaApuestaViewModel : ViewModel() {
    private val _estado = MutableStateFlow<VictoriaApuestaState>(VictoriaApuestaState.Cargando)
    val estado: StateFlow<VictoriaApuestaState> = _estado.asStateFlow()

    fun cargar(compromisoId: Int) {
        viewModelScope.launch {
            _estado.value = VictoriaApuestaState.Cargando

            val perfilResultado = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            if (perfilResultado is ApiResult.Error) {
                _estado.value = VictoriaApuestaState.Error(perfilResultado.mensaje)
                return@launch
            }
            val mi = (perfilResultado as ApiResult.Exito).datos

            val compromisoResultado = safeApiCall { RetrofitClient.compromisosService.obtenerCompromiso(compromisoId) }
            if (compromisoResultado is ApiResult.Error) {
                _estado.value = VictoriaApuestaState.Error(compromisoResultado.mensaje)
                return@launch
            }
            val c = (compromisoResultado as ApiResult.Exito).datos

            val rivalId = if (c.creador_id == mi.id) c.invitado_id else c.creador_id
            val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val rival = (ranking as? ApiResult.Exito)?.datos?.firstOrNull { it.id == rivalId }

            _estado.value = VictoriaApuestaState.Exito(
                VictoriaApuestaUi(
                    descripcion = c.descripcion,
                    rivalNombre = rival?.nombre ?: "Jugador #$rivalId",
                    rivalFotoUrl = rival?.foto_url,
                ),
            )
        }
    }
}
