package com.example.aplicacion_fronton.ui.retos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.ReportarResultadoDto
import com.example.aplicacion_fronton.model.dto.VersusDto
import com.example.aplicacion_fronton.model.dto.rivalIdPara
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CargaPartidoState {
    data object Cargando : CargaPartidoState()
    data class Exito(
        val versus: VersusDto,
        val miId: Int,
        val miNombre: String,
        val miFotoUrl: String?,
        val rivalNombre: String,
        val rivalFotoUrl: String?,
    ) : CargaPartidoState()
    data class Error(val mensaje: String) : CargaPartidoState()
}

sealed class EnvioMarcadorState {
    data object Formulario : EnvioMarcadorState()
    data object Enviando : EnvioMarcadorState()
    data class Esperando(val mensaje: String) : EnvioMarcadorState()
    data class Disputa(val mensaje: String) : EnvioMarcadorState()
    data class Confirmado(val setsPropios: Int, val setsRival: Int, val eloAntes: Int, val eloDespues: Int) : EnvioMarcadorState()
    data class Error(val mensaje: String) : EnvioMarcadorState()
}

class ReportarMarcadorViewModel : ViewModel() {
    private val _carga = MutableStateFlow<CargaPartidoState>(CargaPartidoState.Cargando)
    val carga: StateFlow<CargaPartidoState> = _carga.asStateFlow()

    private val _envio = MutableStateFlow<EnvioMarcadorState>(EnvioMarcadorState.Formulario)
    val envio: StateFlow<EnvioMarcadorState> = _envio.asStateFlow()

    fun cargar(versusId: Int) {
        viewModelScope.launch {
            _carga.value = CargaPartidoState.Cargando

            val perfil = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            if (perfil is ApiResult.Error) {
                _carga.value = CargaPartidoState.Error(perfil.mensaje)
                return@launch
            }
            val mi = (perfil as ApiResult.Exito).datos

            val versusResultado = safeApiCall { RetrofitClient.versusService.obtenerVersus(versusId) }
            if (versusResultado is ApiResult.Error) {
                _carga.value = CargaPartidoState.Error(versusResultado.mensaje)
                return@launch
            }
            val versus = (versusResultado as ApiResult.Exito).datos

            // Reportar marcador es solo para los dos jugadores nombrados del
            // versus (jugador1_id/jugador2_id) — igual que valida el backend
            // en POST /versus/{id}/resultado (ni siquiera para pareja1/pareja2
            // en dobles). Sin este chequeo, CUALQUIER usuario autenticado que
            // llegara a esta pantalla (un espectador, por ejemplo) veía un
            // formulario de marcador totalmente funcional — con selectores
            // interactivos y botón de enviar — aunque el envío final terminara
            // rechazado recién al final por el backend (403). Bug real
            // reportado por el usuario, no una confusión de datos de prueba.
            val esParejaNoDesignada = versus.pareja1_id == mi.id || versus.pareja2_id == mi.id
            if (mi.id != versus.jugador1_id && mi.id != versus.jugador2_id) {
                _carga.value = CargaPartidoState.Error(
                    if (esParejaNoDesignada) {
                        // Distinto de "no sos parte de este versus": la pareja de
                        // dobles SÍ juega el partido, solo que el marcador lo
                        // reporta quien armó el reto (jugador1/jugador2) — decirle
                        // "no sos parte" acá sería directamente falso.
                        "En dobles, el marcador lo reporta quien armó el reto — pedile a tu compañero de equipo que lo haga."
                    } else {
                        "No sos parte de este reto — no podés reportar su marcador."
                    },
                )
                return@launch
            }

            val rivalId = versus.rivalIdPara(mi.id)
            val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val rival = (ranking as? ApiResult.Exito)?.datos?.firstOrNull { it.id == rivalId }

            _carga.value = CargaPartidoState.Exito(
                versus = versus,
                miId = mi.id,
                miNombre = mi.nombre,
                miFotoUrl = mi.foto_url,
                rivalNombre = rival?.nombre ?: "Jugador #$rivalId",
                rivalFotoUrl = rival?.foto_url,
            )
        }
    }

    /** Vuelve al formulario tras una disputa, para que se pueda corregir y
     * reenviar el marcador sin salir de la pantalla. */
    fun reintentar() {
        _envio.value = EnvioMarcadorState.Formulario
    }

    fun enviarMarcador(versusId: Int, setsPropios: Int, setsRival: Int, miId: Int) {
        viewModelScope.launch {
            _envio.value = EnvioMarcadorState.Enviando
            val resultado = safeApiCall {
                RetrofitClient.versusService.reportarResultado(versusId, ReportarResultadoDto(setsPropios, setsRival))
            }
            _envio.value = when (resultado) {
                is ApiResult.Error -> EnvioMarcadorState.Error(resultado.mensaje)
                is ApiResult.Exito -> {
                    val datos = resultado.datos
                    when (datos.estado) {
                        "confirmado" -> {
                            val miDelta = datos.elo?.get(miId.toString())
                            EnvioMarcadorState.Confirmado(
                                setsPropios,
                                setsRival,
                                miDelta?.antes ?: 0,
                                miDelta?.despues ?: 0,
                            )
                        }
                        "disputa" -> EnvioMarcadorState.Disputa(datos.mensaje)
                        else -> EnvioMarcadorState.Esperando(datos.mensaje)
                    }
                }
            }
        }
    }
}
