package com.example.aplicacion_fronton.network

import com.example.aplicacion_fronton.model.dto.PrediccionCreateDto
import com.example.aplicacion_fronton.model.dto.PrediccionDto
import com.example.aplicacion_fronton.model.dto.PredictorRankingEntryDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface PrediccionesService {
    @POST("predicciones")
    suspend fun crear(@Body datos: PrediccionCreateDto): Response<PrediccionDto>

    @GET("predicciones")
    suspend fun listarMisPredicciones(): Response<List<PrediccionDto>>

    @GET("predicciones/ranking")
    suspend fun rankingDePredictores(): Response<List<PredictorRankingEntryDto>>
}
