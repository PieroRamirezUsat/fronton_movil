package com.example.aplicacion_fronton.network

import com.example.aplicacion_fronton.model.dto.RankingEntryDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RankingService {
    @GET("ranking")
    suspend fun obtenerRanking(
        @Query("modalidad") modalidad: String = "individual",
        @Query("categoria_edad") categoriaEdad: String? = null,
        @Query("genero") genero: String? = null
    ): Response<List<RankingEntryDto>>
}
