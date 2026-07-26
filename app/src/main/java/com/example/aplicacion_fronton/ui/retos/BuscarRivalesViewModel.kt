package com.example.aplicacion_fronton.ui.retos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.RankingEntryDto
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BuscarRivalesState {
    data object Cargando : BuscarRivalesState()
    data class Exito(val jugadores: List<RankingEntryDto>) : BuscarRivalesState()
    data class Error(val mensaje: String) : BuscarRivalesState()
}

class BuscarRivalesViewModel : ViewModel() {
    private val _estado = MutableStateFlow<BuscarRivalesState>(BuscarRivalesState.Cargando)
    val estado: StateFlow<BuscarRivalesState> = _estado.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = BuscarRivalesState.Cargando

            val perfil = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            if (perfil is ApiResult.Error) {
                _estado.value = BuscarRivalesState.Error(perfil.mensaje)
                return@launch
            }
            val miId = (perfil as ApiResult.Exito).datos.id

            val resultado = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            _estado.value = when (resultado) {
                is ApiResult.Exito -> BuscarRivalesState.Exito(resultado.datos.filter { it.id != miId })
                is ApiResult.Error -> BuscarRivalesState.Error(resultado.mensaje)
            }
        }
    }
}
