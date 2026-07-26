package com.example.aplicacion_fronton.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.EstadoVersus
import com.example.aplicacion_fronton.model.dto.Modalidad
import com.example.aplicacion_fronton.model.dto.RankingEntryDto
import com.example.aplicacion_fronton.model.dto.UsuarioDto
import com.example.aplicacion_fronton.model.dto.rivalIdPara
import com.example.aplicacion_fronton.model.dto.soyEquipoJugador1
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import com.example.aplicacion_fronton.ui.retos.PartidoHistorialUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProximoVersusUi(
    val versusId: Int,
    val rivalNombre: String,
    val rivalFotoUrl: String?,
    val fechaHora: String,
    val cancha: String?,
    val modalidad: Modalidad,
)

sealed class HomeState {
    data object Cargando : HomeState()
    data class Exito(
        val usuario: UsuarioDto,
        val proximoVersus: ProximoVersusUi?,
        val retosPendientes: Int = 0,
        val actividadReciente: List<PartidoHistorialUi> = emptyList(),
        val rankingPreview: List<RankingEntryDto> = emptyList(),
        /** (posición anterior, posición nueva) cuando se detectó una mejora de
         * posición desde la última vez que se cargó Home en este dispositivo —
         * null si no mejoró o si es la primera vez que se guarda una posición. */
        val subidaRankingDetectada: Pair<Int, Int>? = null,
    ) : HomeState()
    data class Error(val mensaje: String) : HomeState()
}

class HomeViewModel : ViewModel() {
    private val _estado = MutableStateFlow<HomeState>(HomeState.Cargando)
    val estado: StateFlow<HomeState> = _estado.asStateFlow()

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        viewModelScope.launch {
            _estado.value = HomeState.Cargando
            val perfilResultado = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            if (perfilResultado is ApiResult.Error) {
                _estado.value = HomeState.Error(perfilResultado.mensaje)
                return@launch
            }
            val usuario = (perfilResultado as ApiResult.Exito).datos

            // Sin filtro de estado: se necesita ver pendientes, aceptados/jugados
            // (para "Próximo versus") y confirmados (para "Actividad reciente") a
            // la vez, así que se trae todo una sola vez y se separa en cliente.
            val versusResultado = safeApiCall { RetrofitClient.versusService.listarMisVersus(null) }
            val todosMisVersus = (versusResultado as? ApiResult.Exito)?.datos.orEmpty()

            val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val listaJugadores = (ranking as? ApiResult.Exito)?.datos.orEmpty()

            // Se incluye "jugado" además de "aceptado": en cuanto una de las
            // partes reporta su marcador el versus pasa a "jugado" y, si acá
            // solo se pidiera "aceptado", la tarjeta desaparecería del Home
            // aunque el partido siga sin confirmarse — dejando al otro jugador
            // sin forma de encontrarlo para reportar el suyo.
            val proximo = todosMisVersus
                .filter { it.estado == EstadoVersus.ACEPTADO || it.estado == EstadoVersus.JUGADO }
                .minByOrNull { it.fecha_hora }

            val proximoUi = proximo?.let {
                val rivalId = it.rivalIdPara(usuario.id)
                val rival = listaJugadores.firstOrNull { entrada -> entrada.id == rivalId }
                ProximoVersusUi(
                    versusId = it.id,
                    rivalNombre = rival?.nombre ?: "Jugador #$rivalId",
                    rivalFotoUrl = rival?.foto_url,
                    fechaHora = it.fecha_hora,
                    cancha = it.cancha,
                    modalidad = it.modalidad,
                )
            }

            // Solo cuentan como "pendientes por responder" los retos donde soy el
            // retado (jugador2) — los que yo mismo envié y siguen sin respuesta
            // no son una acción mía pendiente, son del otro jugador.
            val retosPendientes = todosMisVersus.count {
                it.estado == EstadoVersus.PENDIENTE && it.jugador2_id == usuario.id
            }

            val actividadReciente = todosMisVersus
                .filter { it.estado == EstadoVersus.CONFIRMADO }
                .mapNotNull { v ->
                    val soyJugador1 = v.soyEquipoJugador1(usuario.id)
                    val misSets = if (soyJugador1) v.sets_jugador1 else v.sets_jugador2
                    val susSets = if (soyJugador1) v.sets_jugador2 else v.sets_jugador1
                    if (misSets == null || susSets == null) return@mapNotNull null
                    val rivalId = v.rivalIdPara(usuario.id)
                    val rival = listaJugadores.firstOrNull { it.id == rivalId }
                    PartidoHistorialUi(
                        versusId = v.id,
                        rivalNombre = rival?.nombre ?: "Jugador #$rivalId",
                        rivalFotoUrl = rival?.foto_url,
                        fechaHora = v.fecha_hora,
                        modalidad = v.modalidad,
                        misSets = misSets,
                        susSets = susSets,
                        gane = misSets > susSets,
                    )
                }
                .sortedByDescending { it.fechaHora }
                .take(2)

            val rankingPreview = listaJugadores.sortedBy { it.posicion }.take(3)

            // "Subiste en el ranking" se detecta comparando contra la última
            // posición vista en ESTE dispositivo (no hay historial server-side
            // de posiciones) — la primera vez que se guarda algo no hay nada
            // contra qué comparar, así que no dispara la animación.
            val miPosicionActual = listaJugadores.firstOrNull { it.id == usuario.id }?.posicion
            val posicionGuardada = RetrofitClient.posicionRankingStore.obtenerPosicionGuardada(usuario.id)
            val subidaDetectada = if (miPosicionActual != null && posicionGuardada != null && miPosicionActual < posicionGuardada) {
                posicionGuardada to miPosicionActual
            } else {
                null
            }
            if (miPosicionActual != null) {
                RetrofitClient.posicionRankingStore.guardarPosicion(usuario.id, miPosicionActual)
            }

            _estado.value = HomeState.Exito(usuario, proximoUi, retosPendientes, actividadReciente, rankingPreview, subidaDetectada)
        }
    }
}
