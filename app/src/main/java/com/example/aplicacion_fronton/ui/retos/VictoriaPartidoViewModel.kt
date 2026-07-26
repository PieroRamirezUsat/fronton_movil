package com.example.aplicacion_fronton.ui.retos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.rivalIdPara
import com.example.aplicacion_fronton.model.dto.soyEquipoJugador1
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VictoriaPartidoUi(
    val misSets: Int,
    val susSets: Int,
    val rivalNombre: String,
    val rivalFotoUrl: String?,
)

sealed class VictoriaPartidoState {
    data object Cargando : VictoriaPartidoState()
    data class Exito(val datos: VictoriaPartidoUi) : VictoriaPartidoState()
    data class Error(val mensaje: String) : VictoriaPartidoState()
}

class VictoriaPartidoViewModel : ViewModel() {
    private val _estado = MutableStateFlow<VictoriaPartidoState>(VictoriaPartidoState.Cargando)
    val estado: StateFlow<VictoriaPartidoState> = _estado.asStateFlow()

    fun cargar(versusId: Int) {
        viewModelScope.launch {
            _estado.value = VictoriaPartidoState.Cargando

            val perfilResultado = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            if (perfilResultado is ApiResult.Error) {
                _estado.value = VictoriaPartidoState.Error(perfilResultado.mensaje)
                return@launch
            }
            val mi = (perfilResultado as ApiResult.Exito).datos

            val versusResultado = safeApiCall { RetrofitClient.versusService.obtenerVersus(versusId) }
            if (versusResultado is ApiResult.Error) {
                _estado.value = VictoriaPartidoState.Error(versusResultado.mensaje)
                return@launch
            }
            val v = (versusResultado as ApiResult.Exito).datos

            val rivalId = v.rivalIdPara(mi.id)
            val rankingResultado = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val rival = (rankingResultado as? ApiResult.Exito)?.datos?.firstOrNull { it.id == rivalId }

            val misSets = if (v.soyEquipoJugador1(mi.id)) v.sets_jugador1 else v.sets_jugador2
            val susSets = if (v.soyEquipoJugador1(mi.id)) v.sets_jugador2 else v.sets_jugador1

            _estado.value = VictoriaPartidoState.Exito(
                VictoriaPartidoUi(
                    misSets = misSets ?: 0,
                    susSets = susSets ?: 0,
                    rivalNombre = rival?.nombre ?: "Jugador #$rivalId",
                    rivalFotoUrl = rival?.foto_url,
                ),
            )
        }
    }
}
