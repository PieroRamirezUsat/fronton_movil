package com.example.aplicacion_fronton.ui.compromisos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.CompromisoDto
import com.example.aplicacion_fronton.model.dto.EstadoCompromiso
import com.example.aplicacion_fronton.model.dto.esGanadorDelCompromiso
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AccionCompromiso { NINGUNA, ADJUNTAR, GANADOR_ESPERA, VERIFICAR, ESPERANDO }

data class CompromisoUi(
    val compromiso: CompromisoDto,
    val otroNombre: String,
    val otroFotoUrl: String?,
    val accion: AccionCompromiso,
    /** null si no es derivable (p.ej. apuesta entre dos espectadores, o el
     * versus vinculado todavía no está confirmado) — true/false solo cuando
     * creador e invitado del compromiso jugaron en equipos opuestos del
     * versus que le dio origen. */
    val esGanador: Boolean? = null,
)

sealed class HistorialCompromisosState {
    data object Cargando : HistorialCompromisosState()
    data class Exito(
        val recibidos: List<CompromisoUi>,
        val enviados: List<CompromisoUi>,
        val enCurso: List<CompromisoUi>,
        val saldados: List<CompromisoUi>,
    ) : HistorialCompromisosState()
    data class Error(val mensaje: String) : HistorialCompromisosState()
}

class HistorialCompromisosViewModel : ViewModel() {
    private val _estado = MutableStateFlow<HistorialCompromisosState>(HistorialCompromisosState.Cargando)
    val estado: StateFlow<HistorialCompromisosState> = _estado.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _estado.value = HistorialCompromisosState.Cargando

            val perfil = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            if (perfil is ApiResult.Error) {
                _estado.value = HistorialCompromisosState.Error(perfil.mensaje)
                return@launch
            }
            val miId = (perfil as ApiResult.Exito).datos.id

            val compromisosResultado = safeApiCall { RetrofitClient.compromisosService.listarMisCompromisos(null) }
            if (compromisosResultado is ApiResult.Error) {
                _estado.value = HistorialCompromisosState.Error(compromisosResultado.mensaje)
                return@launch
            }
            val lista = (compromisosResultado as ApiResult.Exito).datos

            val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val jugadores = (ranking as? ApiResult.Exito)?.datos.orEmpty()

            val versusResultado = safeApiCall { RetrofitClient.versusService.listarMisVersus(null) }
            val versusPorId = (versusResultado as? ApiResult.Exito)?.datos.orEmpty().associateBy { it.id }

            fun aUi(c: CompromisoDto): CompromisoUi {
                val otroId = if (c.creador_id == miId) c.invitado_id else c.creador_id
                val otro = jugadores.firstOrNull { it.id == otroId }
                val esGanador = esGanadorDelCompromiso(c, versusPorId[c.versus_id], miId)
                val accion = when {
                    c.estado != EstadoCompromiso.ACEPTADO && c.estado != EstadoCompromiso.DISPUTA -> AccionCompromiso.NINGUNA
                    c.comprobante_url == null -> if (esGanador == true) AccionCompromiso.GANADOR_ESPERA else AccionCompromiso.ADJUNTAR
                    c.pagado_por == miId -> AccionCompromiso.ESPERANDO
                    else -> AccionCompromiso.VERIFICAR
                }
                return CompromisoUi(c, otro?.nombre ?: "Jugador #$otroId", otro?.foto_url, accion, esGanador)
            }

            val recibidos = lista
                .filter { it.estado == EstadoCompromiso.PENDIENTE_INVITACION && it.invitado_id == miId }
                .map(::aUi)
            val enviados = lista
                .filter { it.estado == EstadoCompromiso.PENDIENTE_INVITACION && it.creador_id == miId }
                .map(::aUi)
            val enCurso = lista
                .filter { it.estado == EstadoCompromiso.ACEPTADO || it.estado == EstadoCompromiso.DISPUTA }
                .map(::aUi)
            val saldados = lista
                .filter { it.estado == EstadoCompromiso.SALDADO }
                .map(::aUi)

            _estado.value = HistorialCompromisosState.Exito(recibidos, enviados, enCurso, saldados)
        }
    }
}
