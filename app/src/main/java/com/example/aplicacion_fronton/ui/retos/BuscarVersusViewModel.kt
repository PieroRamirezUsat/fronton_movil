package com.example.aplicacion_fronton.ui.retos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.VersusDto
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VersusPublicoUi(
    val versus: VersusDto,
    val jugador1Nombre: String,
    val jugador1Foto: String?,
    val jugador2Nombre: String,
    val jugador2Foto: String?,
)

sealed class BuscarVersusState {
    data object Cargando : BuscarVersusState()
    data class Exito(val versus: List<VersusPublicoUi>) : BuscarVersusState()
    data class Error(val mensaje: String) : BuscarVersusState()
}

class BuscarVersusViewModel : ViewModel() {
    private val _estado = MutableStateFlow<BuscarVersusState>(BuscarVersusState.Cargando)
    val estado: StateFlow<BuscarVersusState> = _estado.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _estado.value = BuscarVersusState.Cargando

            val versusResultado = safeApiCall { RetrofitClient.versusService.listarVersusPublicos(null) }
            if (versusResultado is ApiResult.Error) {
                _estado.value = BuscarVersusState.Error(versusResultado.mensaje)
                return@launch
            }
            val lista = (versusResultado as ApiResult.Exito).datos

            val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val jugadores = (ranking as? ApiResult.Exito)?.datos.orEmpty()

            val ui = lista.map { v ->
                val j1 = jugadores.firstOrNull { it.id == v.jugador1_id }
                val j2 = jugadores.firstOrNull { it.id == v.jugador2_id }
                VersusPublicoUi(
                    versus = v,
                    jugador1Nombre = j1?.nombre ?: "Jugador #${v.jugador1_id}",
                    jugador1Foto = j1?.foto_url,
                    jugador2Nombre = j2?.nombre ?: "Jugador #${v.jugador2_id}",
                    jugador2Foto = j2?.foto_url,
                )
            }

            _estado.value = BuscarVersusState.Exito(ui)
        }
    }
}
