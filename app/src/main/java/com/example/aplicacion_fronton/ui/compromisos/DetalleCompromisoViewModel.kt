package com.example.aplicacion_fronton.ui.compromisos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.CompromisoDto
import com.example.aplicacion_fronton.model.dto.VersusDto
import com.example.aplicacion_fronton.model.dto.esGanadorDelCompromiso
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DetalleCompromisoState {
    data object Cargando : DetalleCompromisoState()
    data class Exito(
        val compromiso: CompromisoDto,
        val miId: Int,
        val otroNombre: String,
        val otroFotoUrl: String?,
        val pagadoPorNombre: String?,
        val confirmadoPorNombre: String?,
        val versus: VersusDto?,
        /** null si no es derivable (ver [esGanadorDelCompromiso]) — solo se
         * muestra el bloque de resultado cuando esto no es null. */
        val esGanador: Boolean?,
    ) : DetalleCompromisoState()
    data class Error(val mensaje: String) : DetalleCompromisoState()
}

class DetalleCompromisoViewModel : ViewModel() {
    private val _estado = MutableStateFlow<DetalleCompromisoState>(DetalleCompromisoState.Cargando)
    val estado: StateFlow<DetalleCompromisoState> = _estado.asStateFlow()

    fun cargar(compromisoId: Int) {
        viewModelScope.launch {
            _estado.value = DetalleCompromisoState.Cargando

            val perfil = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            if (perfil is ApiResult.Error) {
                _estado.value = DetalleCompromisoState.Error(perfil.mensaje)
                return@launch
            }
            val mi = (perfil as ApiResult.Exito).datos

            val compromisoResultado = safeApiCall { RetrofitClient.compromisosService.obtenerCompromiso(compromisoId) }
            if (compromisoResultado is ApiResult.Error) {
                _estado.value = DetalleCompromisoState.Error(compromisoResultado.mensaje)
                return@launch
            }
            val c = (compromisoResultado as ApiResult.Exito).datos

            val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val jugadores = (ranking as? ApiResult.Exito)?.datos.orEmpty()
            fun nombreDe(id: Int?): String? = id?.let { idNoNull -> jugadores.firstOrNull { it.id == idNoNull }?.nombre ?: "Jugador #$idNoNull" }

            val otroId = if (c.creador_id == mi.id) c.invitado_id else c.creador_id
            val otro = jugadores.firstOrNull { it.id == otroId }

            val versusResultado = safeApiCall { RetrofitClient.versusService.obtenerVersus(c.versus_id) }
            val versus = (versusResultado as? ApiResult.Exito)?.datos

            _estado.value = DetalleCompromisoState.Exito(
                compromiso = c,
                miId = mi.id,
                otroNombre = otro?.nombre ?: "Jugador #$otroId",
                otroFotoUrl = otro?.foto_url,
                pagadoPorNombre = nombreDe(c.pagado_por),
                confirmadoPorNombre = nombreDe(c.confirmado_por),
                versus = versus,
                esGanador = esGanadorDelCompromiso(c, versus, mi.id),
            )
        }
    }
}
