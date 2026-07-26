package com.example.aplicacion_fronton.ui.ranking

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

sealed class RankingState {
    data object Cargando : RankingState()
    data class Exito(val entradas: List<RankingEntryDto>, val miId: Int) : RankingState()
    data class Error(val mensaje: String) : RankingState()
}

class RankingViewModel : ViewModel() {
    private val _estado = MutableStateFlow<RankingState>(RankingState.Cargando)
    val estado: StateFlow<RankingState> = _estado.asStateFlow()

    var modalidad: String = "individual"
        private set
    var categoriaEdad: String? = null
        private set

    init {
        cargar()
    }

    fun cambiarFiltro(modalidad: String = this.modalidad, categoriaEdad: String? = this.categoriaEdad) {
        this.modalidad = modalidad
        this.categoriaEdad = categoriaEdad
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _estado.value = RankingState.Cargando

            val perfil = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            val miId = (perfil as? ApiResult.Exito)?.datos?.id ?: -1

            val resultado = safeApiCall {
                RetrofitClient.rankingService.obtenerRanking(modalidad, categoriaEdad)
            }
            _estado.value = when (resultado) {
                is ApiResult.Exito -> RankingState.Exito(resultado.datos, miId)
                is ApiResult.Error -> RankingState.Error(resultado.mensaje)
            }
        }
    }
}
