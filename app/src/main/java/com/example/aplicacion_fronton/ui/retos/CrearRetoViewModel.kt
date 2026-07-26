package com.example.aplicacion_fronton.ui.retos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.CompromisoCreateDto
import com.example.aplicacion_fronton.model.dto.RankingEntryDto
import com.example.aplicacion_fronton.model.dto.UsuarioDto
import com.example.aplicacion_fronton.model.dto.VersusCreateDto
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CrearRetoState {
    data object Idle : CrearRetoState()
    data object Cargando : CrearRetoState()
    data class Exito(val advertenciaCompromiso: String? = null) : CrearRetoState()
    data class Error(val mensaje: String) : CrearRetoState()
}

class CrearRetoViewModel : ViewModel() {
    private val _estado = MutableStateFlow<CrearRetoState>(CrearRetoState.Idle)
    val estado: StateFlow<CrearRetoState> = _estado.asStateFlow()

    private val _jugadores = MutableStateFlow<List<RankingEntryDto>>(emptyList())
    val jugadores: StateFlow<List<RankingEntryDto>> = _jugadores.asStateFlow()

    private val _miUsuario = MutableStateFlow<UsuarioDto?>(null)
    val miUsuario: StateFlow<UsuarioDto?> = _miUsuario.asStateFlow()

    private val _incumplidosRival = MutableStateFlow(0)
    val incumplidosRival: StateFlow<Int> = _incumplidosRival.asStateFlow()

    init {
        viewModelScope.launch {
            val resultado = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            if (resultado is ApiResult.Exito) _jugadores.value = resultado.datos
        }
        viewModelScope.launch {
            val perfil = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            if (perfil is ApiResult.Exito) _miUsuario.value = perfil.datos
        }
    }

    fun cargarCumplimientoRival(rivalId: Int) {
        viewModelScope.launch {
            val resultado = safeApiCall { RetrofitClient.usuariosService.obtenerCumplimiento(rivalId) }
            if (resultado is ApiResult.Exito) _incumplidosRival.value = resultado.datos.incumplidos
        }
    }

    fun enviarReto(datos: VersusCreateDto, apuesta: String?, invitadoIdApuesta: Int) {
        viewModelScope.launch {
            _estado.value = CrearRetoState.Cargando
            val resultado = safeApiCall { RetrofitClient.versusService.crear(datos) }
            when (resultado) {
                is ApiResult.Error -> _estado.value = CrearRetoState.Error(resultado.mensaje)
                is ApiResult.Exito -> {
                    var advertencia: String? = null
                    if (!apuesta.isNullOrBlank()) {
                        val compromisoResultado = safeApiCall {
                            RetrofitClient.compromisosService.crear(
                                CompromisoCreateDto(resultado.datos.id, invitadoIdApuesta, apuesta.trim(), null),
                            )
                        }
                        if (compromisoResultado is ApiResult.Error) {
                            advertencia = "El reto se creó, pero no se pudo registrar el compromiso: " +
                                "${compromisoResultado.mensaje}. Puedes intentarlo de nuevo desde el detalle del reto."
                        }
                    }
                    _estado.value = CrearRetoState.Exito(advertencia)
                }
            }
        }
    }

    fun reiniciarEstado() {
        _estado.value = CrearRetoState.Idle
    }
}
