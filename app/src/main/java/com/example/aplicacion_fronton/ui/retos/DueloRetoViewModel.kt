package com.example.aplicacion_fronton.ui.retos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.Modalidad
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JugadorDuelo(val nombre: String, val fotoUrl: String?)

data class DueloUi(
    val equipoRojo: List<JugadorDuelo>,
    val equipoAzul: List<JugadorDuelo>,
    val esDobles: Boolean,
)

sealed class DueloState {
    data object Cargando : DueloState()
    data class Exito(val duelo: DueloUi) : DueloState()
    data class Error(val mensaje: String) : DueloState()
}

class DueloRetoViewModel : ViewModel() {
    private val _estado = MutableStateFlow<DueloState>(DueloState.Cargando)
    val estado: StateFlow<DueloState> = _estado.asStateFlow()

    fun cargar(versusId: Int) {
        viewModelScope.launch {
            _estado.value = DueloState.Cargando

            val perfilResultado = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            if (perfilResultado is ApiResult.Error) {
                _estado.value = DueloState.Error(perfilResultado.mensaje)
                return@launch
            }
            val mi = (perfilResultado as ApiResult.Exito).datos

            val versusResultado = safeApiCall { RetrofitClient.versusService.obtenerVersus(versusId) }
            if (versusResultado is ApiResult.Error) {
                _estado.value = DueloState.Error(versusResultado.mensaje)
                return@launch
            }
            val v = (versusResultado as ApiResult.Exito).datos

            // "individual" trae a todos los usuarios activos sin importar la
            // modalidad del versus (el backend no filtra por modalidad, solo
            // cambia qué columna de Elo usa para ordenar) — sirve igual para
            // resolver nombre/foto de cualquiera de los 4 posibles jugadores.
            val rankingResultado = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val jugadores = (rankingResultado as? ApiResult.Exito)?.datos.orEmpty()

            fun resolver(id: Int?): JugadorDuelo? {
                if (id == null) return null
                if (id == mi.id) return JugadorDuelo(mi.nombre, mi.foto_url)
                val encontrado = jugadores.firstOrNull { it.id == id }
                return JugadorDuelo(encontrado?.nombre ?: "Jugador #$id", encontrado?.foto_url)
            }

            // Rojo = equipo del retador (jugador1/pareja1, quien creó el reto).
            // Azul = equipo del retado (jugador2/pareja2) — quien recibe la
            // notificación siempre es jugador2_id, así que acá "yo" siempre cae
            // del lado azul.
            val equipoRojo = listOfNotNull(resolver(v.jugador1_id), resolver(v.pareja1_id))
            val equipoAzul = listOfNotNull(resolver(v.jugador2_id), resolver(v.pareja2_id))

            _estado.value = DueloState.Exito(DueloUi(equipoRojo, equipoAzul, v.modalidad == Modalidad.DOBLES))
        }
    }
}
