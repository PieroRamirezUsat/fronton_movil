package com.example.aplicacion_fronton.ui.compromisos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.CompromisoDto
import com.example.aplicacion_fronton.model.dto.esGanadorDelCompromiso
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class VerificarComprobanteEstado {
    data object Cargando : VerificarComprobanteEstado()
    data class Exito(val compromiso: CompromisoDto, val quienSubioNombre: String, val esGanador: Boolean?) : VerificarComprobanteEstado()
    data class Error(val mensaje: String) : VerificarComprobanteEstado()
}

sealed class VistoBuenoEstado {
    data object Ocioso : VistoBuenoEstado()
    data object Enviando : VistoBuenoEstado()
    data class Hecho(val confirmado: Boolean) : VistoBuenoEstado()
    data class Error(val mensaje: String) : VistoBuenoEstado()
}

class VerificarComprobanteViewModel : ViewModel() {
    private val _estado = MutableStateFlow<VerificarComprobanteEstado>(VerificarComprobanteEstado.Cargando)
    val estado: StateFlow<VerificarComprobanteEstado> = _estado.asStateFlow()

    private val _vistoBueno = MutableStateFlow<VistoBuenoEstado>(VistoBuenoEstado.Ocioso)
    val vistoBueno: StateFlow<VistoBuenoEstado> = _vistoBueno.asStateFlow()

    fun cargar(compromisoId: Int) {
        viewModelScope.launch {
            _estado.value = VerificarComprobanteEstado.Cargando

            val compromisoResultado = safeApiCall { RetrofitClient.compromisosService.obtenerCompromiso(compromisoId) }
            if (compromisoResultado is ApiResult.Error) {
                _estado.value = VerificarComprobanteEstado.Error(compromisoResultado.mensaje)
                return@launch
            }
            val compromiso = (compromisoResultado as ApiResult.Exito).datos

            val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val quienSubio = (ranking as? ApiResult.Exito)?.datos?.firstOrNull { it.id == compromiso.pagado_por }

            val perfilResultado = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            val versusResultado = safeApiCall { RetrofitClient.versusService.obtenerVersus(compromiso.versus_id) }
            val mi = (perfilResultado as? ApiResult.Exito)?.datos
            val versus = (versusResultado as? ApiResult.Exito)?.datos
            val esGanador = if (mi != null) esGanadorDelCompromiso(compromiso, versus, mi.id) else null

            _estado.value = VerificarComprobanteEstado.Exito(
                compromiso = compromiso,
                quienSubioNombre = quienSubio?.nombre ?: "Jugador #${compromiso.pagado_por}",
                esGanador = esGanador,
            )
        }
    }

    fun darVistoBueno(compromisoId: Int, confirmar: Boolean) {
        viewModelScope.launch {
            _vistoBueno.value = VistoBuenoEstado.Enviando
            val resultado = safeApiCall { RetrofitClient.compromisosService.darVistoBueno(compromisoId, confirmar) }
            _vistoBueno.value = when (resultado) {
                is ApiResult.Exito -> VistoBuenoEstado.Hecho(confirmar)
                is ApiResult.Error -> VistoBuenoEstado.Error(resultado.mensaje)
            }
        }
    }
}
