package com.example.aplicacion_fronton.ui.retos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.Modalidad
import com.example.aplicacion_fronton.model.dto.rivalIdPara
import com.example.aplicacion_fronton.model.dto.soyEquipoJugador1
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

private fun parsearFechaAEpoch(iso: String): Long = try {
    val fecha = iso.substringBefore("T")
    val hora = iso.substringAfter("T")
    val partesFecha = fecha.split("-")
    val partesHora = hora.split(":")
    Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(partesFecha[0].toInt(), partesFecha[1].toInt() - 1, partesFecha[2].toInt(), partesHora[0].toInt(), partesHora[1].toInt(), 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
} catch (e: Exception) {
    0L
}

data class PartidoHistorialUi(
    val versusId: Int,
    val rivalNombre: String,
    val rivalFotoUrl: String?,
    val fechaHora: String,
    val modalidad: Modalidad,
    val misSets: Int,
    val susSets: Int,
    val gane: Boolean,
)

sealed class HistorialState {
    data object Cargando : HistorialState()
    data class Exito(
        val eloActual: Int,
        val partidos: List<PartidoHistorialUi>,
        val serieElo: List<Int> = emptyList(),
        val tendencia30Dias: Int? = null,
    ) : HistorialState()
    data class Error(val mensaje: String) : HistorialState()
}

class HistorialVersusViewModel : ViewModel() {
    private val _estado = MutableStateFlow<HistorialState>(HistorialState.Cargando)
    val estado: StateFlow<HistorialState> = _estado.asStateFlow()

    private val _filtro = MutableStateFlow("")
    val filtro: StateFlow<String> = _filtro.asStateFlow()

    init {
        cargar()
    }

    fun cambiarFiltro(texto: String) {
        _filtro.value = texto
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = HistorialState.Cargando

            val perfil = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            if (perfil is ApiResult.Error) {
                _estado.value = HistorialState.Error(perfil.mensaje)
                return@launch
            }
            val mi = (perfil as ApiResult.Exito).datos

            val versusResultado = safeApiCall { RetrofitClient.versusService.listarMisVersus("confirmado") }
            if (versusResultado is ApiResult.Error) {
                _estado.value = HistorialState.Error(versusResultado.mensaje)
                return@launch
            }
            val lista = (versusResultado as ApiResult.Exito).datos

            val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val jugadores = (ranking as? ApiResult.Exito)?.datos.orEmpty()

            val partidos = lista.mapNotNull { v ->
                val soyJugador1 = v.soyEquipoJugador1(mi.id)
                val misSets = if (soyJugador1) v.sets_jugador1 else v.sets_jugador2
                val susSets = if (soyJugador1) v.sets_jugador2 else v.sets_jugador1
                if (misSets == null || susSets == null) return@mapNotNull null

                val rivalId = v.rivalIdPara(mi.id)
                val rival = jugadores.firstOrNull { it.id == rivalId }

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
            }.sortedByDescending { it.fechaHora }

            // El gráfico se limita a individual, igual que "ELO ACTUAL" — mezclar
            // individual y dobles en una sola línea no tendría sentido (son
            // escalas de Elo separadas). El backend ya filtra por modalidad.
            val eloHistorialResultado = safeApiCall { RetrofitClient.usuariosService.obtenerEloHistorial("individual") }
            val puntos = (eloHistorialResultado as? ApiResult.Exito)?.datos.orEmpty()

            val serieElo = if (puntos.isEmpty()) {
                emptyList()
            } else {
                listOf(puntos.first().elo_antes) + puntos.map { it.elo_despues }
            }

            val haceUnMes = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
            val tendencia30Dias = puntos
                .firstOrNull { parsearFechaAEpoch(it.fecha) >= haceUnMes }
                ?.let { mi.elo_individual - it.elo_antes }

            _estado.value = HistorialState.Exito(mi.elo_individual, partidos, serieElo, tendencia30Dias)
        }
    }
}
