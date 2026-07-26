package com.example.aplicacion_fronton.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.CumplimientoDto
import com.example.aplicacion_fronton.model.dto.ParejaHabitualRequestDto
import com.example.aplicacion_fronton.model.dto.RankingEntryDto
import com.example.aplicacion_fronton.model.dto.UsuarioDto
import com.example.aplicacion_fronton.model.dto.rivalIdPara
import com.example.aplicacion_fronton.model.dto.soyEquipoJugador1
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.PushTokenRegistrar
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import com.example.aplicacion_fronton.ui.retos.PartidoHistorialUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

sealed class PerfilState {
    data object Cargando : PerfilState()
    data class Exito(
        val usuario: UsuarioDto,
        val cumplimiento: CumplimientoDto?,
        val posicionRanking: Int?,
        val jugadores: List<RankingEntryDto> = emptyList(),
        val ultimosVersus: List<PartidoHistorialUi> = emptyList(),
    ) : PerfilState()
    data class Error(val mensaje: String) : PerfilState()
}

class PerfilViewModel : ViewModel() {
    private val _estado = MutableStateFlow<PerfilState>(PerfilState.Cargando)
    val estado: StateFlow<PerfilState> = _estado.asStateFlow()

    private val _subiendoFoto = MutableStateFlow(false)
    val subiendoFoto: StateFlow<Boolean> = _subiendoFoto.asStateFlow()

    private val _errorPareja = MutableStateFlow<String?>(null)
    val errorPareja: StateFlow<String?> = _errorPareja.asStateFlow()

    private val _sesionCerrada = MutableStateFlow(false)
    val sesionCerrada: StateFlow<Boolean> = _sesionCerrada.asStateFlow()

    init {
        cargar()
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            // Hay que borrar el token de push ANTES de limpiar el JWT local —
            // el endpoint necesita sesión válida para autenticar el pedido; si
            // se limpiara primero, la llamada saldría sin token y siempre
            // fallaría con 401 (mejor esfuerzo igual: si falla, no bloquea el
            // logout, el estado se marca cerrado de todas formas).
            PushTokenRegistrar.borrarTokenDelServidor()
            RetrofitClient.tokenStore.limpiar()
            _sesionCerrada.value = true
        }
    }

    fun subirFoto(parte: MultipartBody.Part) {
        viewModelScope.launch {
            _subiendoFoto.value = true
            val resultado = safeApiCall { RetrofitClient.usuariosService.subirFotoPerfil(parte) }
            if (resultado is ApiResult.Exito) {
                val actual = _estado.value
                if (actual is PerfilState.Exito) {
                    _estado.value = actual.copy(usuario = resultado.datos)
                }
            }
            // Si falla, se deja de mostrar el spinner y el usuario puede volver a
            // tocar la foto para reintentar — no hace falta un estado de error
            // dedicado para esto.
            _subiendoFoto.value = false
        }
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = PerfilState.Cargando

            val perfil = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            if (perfil is ApiResult.Error) {
                _estado.value = PerfilState.Error(perfil.mensaje)
                return@launch
            }
            val usuario = (perfil as ApiResult.Exito).datos

            // Cumplimiento y posición en el ranking son datos "mejor esfuerzo" —
            // si fallan, se muestra el resto del perfil igual, sin bloquear la
            // pantalla entera por un endpoint secundario.
            val cumplimiento = (safeApiCall { RetrofitClient.usuariosService.obtenerCumplimiento(usuario.id) } as? ApiResult.Exito)?.datos

            val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val listaJugadores = (ranking as? ApiResult.Exito)?.datos.orEmpty()
            val posicion = listaJugadores.firstOrNull { it.id == usuario.id }?.posicion

            val versusResultado = safeApiCall { RetrofitClient.versusService.listarMisVersus("confirmado") }
            val ultimosVersus = (versusResultado as? ApiResult.Exito)?.datos.orEmpty()
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
                .take(3)

            _estado.value = PerfilState.Exito(usuario, cumplimiento, posicion, listaJugadores, ultimosVersus)
        }
    }

    fun actualizarParejaHabitual(parejaId: Int?) {
        viewModelScope.launch {
            _errorPareja.value = null
            val resultado = safeApiCall { RetrofitClient.usuariosService.actualizarParejaHabitual(ParejaHabitualRequestDto(parejaId)) }
            when (resultado) {
                is ApiResult.Exito -> {
                    val actual = _estado.value
                    if (actual is PerfilState.Exito) {
                        _estado.value = actual.copy(usuario = resultado.datos)
                    }
                    // El emparejamiento es mutuo: este cambio puede haber liberado
                    // u ocupado a otros jugadores — se refresca la lista en
                    // silencio (sin pantalla de carga) para que el selector la
                    // próxima vez muestre quién sigue disponible de verdad.
                    val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
                    val listaActualizada = (ranking as? ApiResult.Exito)?.datos
                    if (listaActualizada != null) {
                        val actualizado = _estado.value
                        if (actualizado is PerfilState.Exito) {
                            _estado.value = actualizado.copy(jugadores = listaActualizada)
                        }
                    }
                }
                is ApiResult.Error -> _errorPareja.value = resultado.mensaje
            }
        }
    }

    fun limpiarErrorPareja() {
        _errorPareja.value = null
    }
}
