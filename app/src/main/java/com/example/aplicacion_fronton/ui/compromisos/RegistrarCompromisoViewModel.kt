package com.example.aplicacion_fronton.ui.compromisos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.CompromisoCreateDto
import com.example.aplicacion_fronton.model.dto.RankingEntryDto
import com.example.aplicacion_fronton.model.dto.VersusDto
import com.example.aplicacion_fronton.model.dto.rivalIdPara
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CargaCompromisoState {
    data object Cargando : CargaCompromisoState()
    data class Exito(
        val versus: VersusDto,
        val miId: Int,
        val miNombre: String,
        val miFotoUrl: String?,
        // Cuando soy uno de los dos jugadores, el invitado se autocompleta con
        // el rival y no hay nada que elegir. Cuando soy espectador (no juego
        // este partido) no hay un "rival" natural — el invitado es con quién
        // aposté, y hay que elegirlo de una lista.
        val esEspectador: Boolean,
        val invitadoIdInicial: Int?,
        val invitadoNombreInicial: String?,
        val invitadoFotoInicial: String?,
        val incumplidosInvitado: Int,
        val candidatosInvitado: List<RankingEntryDto>,
        // Solo se usan cuando esEspectador — para mostrar de qué partido se
        // trata, ya que ninguno de los dos jugadores soy yo.
        val jugador1Nombre: String,
        val jugador1Foto: String?,
        val jugador2Nombre: String,
        val jugador2Foto: String?,
    ) : CargaCompromisoState()
    data class Error(val mensaje: String) : CargaCompromisoState()
}

sealed class GuardarCompromisoState {
    data object Ocioso : GuardarCompromisoState()
    data object Guardando : GuardarCompromisoState()
    data object Guardado : GuardarCompromisoState()
    data class Error(val mensaje: String) : GuardarCompromisoState()
}

class RegistrarCompromisoViewModel : ViewModel() {
    private val _carga = MutableStateFlow<CargaCompromisoState>(CargaCompromisoState.Cargando)
    val carga: StateFlow<CargaCompromisoState> = _carga.asStateFlow()

    private val _guardado = MutableStateFlow<GuardarCompromisoState>(GuardarCompromisoState.Ocioso)
    val guardado: StateFlow<GuardarCompromisoState> = _guardado.asStateFlow()

    fun cargar(versusId: Int) {
        viewModelScope.launch {
            _carga.value = CargaCompromisoState.Cargando

            val perfil = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            if (perfil is ApiResult.Error) {
                _carga.value = CargaCompromisoState.Error(perfil.mensaje)
                return@launch
            }
            val mi = (perfil as ApiResult.Exito).datos

            val versusResultado = safeApiCall { RetrofitClient.versusService.obtenerVersus(versusId) }
            if (versusResultado is ApiResult.Error) {
                _carga.value = CargaCompromisoState.Error(versusResultado.mensaje)
                return@launch
            }
            val versus = (versusResultado as ApiResult.Exito).datos

            val esParticipante = mi.id == versus.jugador1_id || mi.id == versus.jugador2_id ||
                mi.id == versus.pareja1_id || mi.id == versus.pareja2_id
            val esEspectador = !esParticipante

            val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val jugadores = (ranking as? ApiResult.Exito)?.datos.orEmpty()

            if (esEspectador) {
                val j1 = jugadores.firstOrNull { it.id == versus.jugador1_id }
                val j2 = jugadores.firstOrNull { it.id == versus.jugador2_id }
                _carga.value = CargaCompromisoState.Exito(
                    versus = versus,
                    miId = mi.id,
                    miNombre = mi.nombre,
                    miFotoUrl = mi.foto_url,
                    esEspectador = true,
                    invitadoIdInicial = null,
                    invitadoNombreInicial = null,
                    invitadoFotoInicial = null,
                    incumplidosInvitado = 0,
                    candidatosInvitado = jugadores.filter { it.id != mi.id },
                    jugador1Nombre = j1?.nombre ?: "Jugador #${versus.jugador1_id}",
                    jugador1Foto = j1?.foto_url,
                    jugador2Nombre = j2?.nombre ?: "Jugador #${versus.jugador2_id}",
                    jugador2Foto = j2?.foto_url,
                )
                return@launch
            }

            val rivalId = versus.rivalIdPara(mi.id)
            val rival = jugadores.firstOrNull { it.id == rivalId }

            val cumplimientoResultado = safeApiCall { RetrofitClient.usuariosService.obtenerCumplimiento(rivalId) }
            val incumplidos = (cumplimientoResultado as? ApiResult.Exito)?.datos?.incumplidos ?: 0

            _carga.value = CargaCompromisoState.Exito(
                versus = versus,
                miId = mi.id,
                miNombre = mi.nombre,
                miFotoUrl = mi.foto_url,
                esEspectador = false,
                invitadoIdInicial = rivalId,
                invitadoNombreInicial = rival?.nombre ?: "Jugador #$rivalId",
                invitadoFotoInicial = rival?.foto_url,
                incumplidosInvitado = incumplidos,
                candidatosInvitado = emptyList(),
                jugador1Nombre = "",
                jugador1Foto = null,
                jugador2Nombre = "",
                jugador2Foto = null,
            )
        }
    }

    // Las "fichas de cancha" (un marcador simbólico sin valor real) resultaron
    // no usarse en la práctica — lo que la gente apuesta ya se describe con
    // texto libre ("un ceviche", "una gaseosa", "S/10"), así que se dejó de
    // pedir un número aparte. El campo sigue existiendo en el backend/DTO
    // (compromisos viejos ya lo tienen), simplemente ya no se envía ninguno.
    fun guardar(versusId: Int, invitadoId: Int, descripcion: String) {
        viewModelScope.launch {
            _guardado.value = GuardarCompromisoState.Guardando
            val resultado = safeApiCall {
                RetrofitClient.compromisosService.crear(CompromisoCreateDto(versusId, invitadoId, descripcion, fichas_cancha = null))
            }
            _guardado.value = when (resultado) {
                is ApiResult.Exito -> GuardarCompromisoState.Guardado
                is ApiResult.Error -> GuardarCompromisoState.Error(resultado.mensaje)
            }
        }
    }
}
