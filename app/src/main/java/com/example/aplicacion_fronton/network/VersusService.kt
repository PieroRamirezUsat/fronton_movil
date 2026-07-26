package com.example.aplicacion_fronton.network

import com.example.aplicacion_fronton.model.dto.ReportarResultadoDto
import com.example.aplicacion_fronton.model.dto.ResultadoRespuestaDto
import com.example.aplicacion_fronton.model.dto.VersusCreateDto
import com.example.aplicacion_fronton.model.dto.VersusDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface VersusService {
    @POST("versus")
    suspend fun crear(@Body datos: VersusCreateDto): Response<VersusDto>

    @GET("versus")
    suspend fun listarMisVersus(@Query("estado") estado: String? = null): Response<List<VersusDto>>

    @GET("versus/{id}")
    suspend fun obtenerVersus(@Path("id") versusId: Int): Response<VersusDto>

    @GET("versus/publicos")
    suspend fun listarVersusPublicos(@Query("estado") estado: String? = null): Response<List<VersusDto>>

    @GET("versus/jugador/{usuarioId}")
    suspend fun listarVersusDeJugador(@Path("usuarioId") usuarioId: Int): Response<List<VersusDto>>

    @POST("versus/{id}/responder")
    suspend fun responder(@Path("id") versusId: Int, @Query("aceptar") aceptar: Boolean): Response<VersusDto>

    @POST("versus/{id}/resultado")
    suspend fun reportarResultado(
        @Path("id") versusId: Int,
        @Body datos: ReportarResultadoDto
    ): Response<ResultadoRespuestaDto>
}
