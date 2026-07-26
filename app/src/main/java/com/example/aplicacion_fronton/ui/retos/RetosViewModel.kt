package com.example.aplicacion_fronton.ui.retos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.EstadoVersus
import com.example.aplicacion_fronton.model.dto.VersusDto
import com.example.aplicacion_fronton.model.dto.rivalIdPara
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RetoUi(
    val versus: VersusDto,
    val rivalNombre: String,
    val rivalFoto: String?,
)

data class RetoEnCursoUi(
    val versus: VersusDto,
    val rivalNombre: String,
    val rivalFoto: String?,
    val miTurnoDeReportar: Boolean,
)

sealed class RetosState {
    data object Cargando : RetosState()
    data class Exito(val recibidos: List<RetoUi>, val enviados: List<RetoUi>, val enCurso: List<RetoEnCursoUi>) : RetosState()
    data class Error(val mensaje: String) : RetosState()
}

class RetosViewModel : ViewModel() {
    private val _estado = MutableStateFlow<RetosState>(RetosState.Cargando)
    val estado: StateFlow<RetosState> = _estado.asStateFlow()

    private val _idsRespondiendo = MutableStateFlow<Set<Int>>(emptySet())
    val idsRespondiendo: StateFlow<Set<Int>> = _idsRespondiendo.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = RetosState.Cargando

            val perfil = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            if (perfil is ApiResult.Error) {
                _estado.value = RetosState.Error(perfil.mensaje)
                return@launch
            }
            val miId = (perfil as ApiResult.Exito).datos.id

            // Sin filtro de estado: se trae todo lo mío y se agrupa en cliente.
            // Antes solo se pedía "pendiente", así que en cuanto un reto se
            // aceptaba (o alguien reportaba marcador y pasaba a "jugado")
            // desaparecía de esta pantalla sin dejar rastro — el usuario no
            // tenía forma de volver a encontrarlo para ver el detalle o
            // reportar su propio marcador.
            val versusResultado = safeApiCall { RetrofitClient.versusService.listarMisVersus(null) }
            if (versusResultado is ApiResult.Error) {
                _estado.value = RetosState.Error(versusResultado.mensaje)
                return@launch
            }
            val listaVersus = (versusResultado as ApiResult.Exito).datos

            // El ranking ya trae nombre/foto de todos los jugadores — reusarlo acá
            // evita necesitar un endpoint nuevo "GET /usuarios/{id}" solo para esto.
            val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val mapaJugadores = (ranking as? ApiResult.Exito)?.datos?.associateBy { it.id }.orEmpty()

            val pendientes = listaVersus.filter { it.estado == EstadoVersus.PENDIENTE }
            val recibidos = pendientes
                .filter { it.jugador2_id == miId }
                .map { v -> RetoUi(v, mapaJugadores[v.jugador1_id]?.nombre ?: "Jugador #${v.jugador1_id}", mapaJugadores[v.jugador1_id]?.foto_url) }
            val enviados = pendientes
                .filter { it.jugador2_id != miId }
                .map { v -> RetoUi(v, mapaJugadores[v.jugador2_id]?.nombre ?: "Jugador #${v.jugador2_id}", mapaJugadores[v.jugador2_id]?.foto_url) }

            val enCurso = listaVersus
                .filter { it.estado in listOf(EstadoVersus.ACEPTADO, EstadoVersus.JUGADO, EstadoVersus.DISPUTA) }
                .sortedBy { it.fecha_hora }
                .map { v ->
                    val rivalId = v.rivalIdPara(miId)
                    // El backend solo deja reportar a jugador1/jugador2 — la pareja
                    // de dobles (pareja1_id/pareja2_id) nunca puede, así que sin este
                    // chequeo se le mostraría "TU TURNO DE REPORTAR" a alguien que al
                    // tocarlo se encontraría con que no hay nada que pueda hacer.
                    val puedoReportarYo = v.jugador1_id == miId || v.jugador2_id == miId
                    RetoEnCursoUi(
                        versus = v,
                        rivalNombre = mapaJugadores[rivalId]?.nombre ?: "Jugador #$rivalId",
                        rivalFoto = mapaJugadores[rivalId]?.foto_url,
                        miTurnoDeReportar = puedoReportarYo && v.estado == EstadoVersus.JUGADO && !v.mi_reporte_enviado,
                    )
                }

            _estado.value = RetosState.Exito(recibidos, enviados, enCurso)
        }
    }

    fun responder(versusId: Int, aceptar: Boolean) {
        viewModelScope.launch {
            _idsRespondiendo.value = _idsRespondiendo.value + versusId
            val resultado = safeApiCall { RetrofitClient.versusService.responder(versusId, aceptar) }
            if (resultado is ApiResult.Exito) {
                cargar()
            }
            _idsRespondiendo.value = _idsRespondiendo.value - versusId
        }
    }
}
