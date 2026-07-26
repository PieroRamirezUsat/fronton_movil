package com.example.aplicacion_fronton.network

import com.example.aplicacion_fronton.model.dto.CompromisoCreateDto
import com.example.aplicacion_fronton.model.dto.CompromisoDto
import com.example.aplicacion_fronton.model.dto.SubirComprobanteDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface CompromisosService {
    @POST("compromisos")
    suspend fun crear(@Body datos: CompromisoCreateDto): Response<CompromisoDto>

    @GET("compromisos")
    suspend fun listarMisCompromisos(@Query("estado") estado: String? = null): Response<List<CompromisoDto>>

    @GET("compromisos/{id}")
    suspend fun obtenerCompromiso(@Path("id") compromisoId: Int): Response<CompromisoDto>

    @POST("compromisos/{id}/responder")
    suspend fun responder(@Path("id") compromisoId: Int, @Query("aceptar") aceptar: Boolean): Response<CompromisoDto>

    @POST("compromisos/{id}/comprobante")
    suspend fun subirComprobante(
        @Path("id") compromisoId: Int,
        @Body datos: SubirComprobanteDto
    ): Response<CompromisoDto>

    @Multipart
    @POST("compromisos/{id}/comprobante-foto")
    suspend fun subirComprobanteFoto(
        @Path("id") compromisoId: Int,
        @Part archivo: MultipartBody.Part
    ): Response<CompromisoDto>

    @POST("compromisos/{id}/visto-bueno")
    suspend fun darVistoBueno(
        @Path("id") compromisoId: Int,
        @Query("confirmar") confirmar: Boolean
    ): Response<CompromisoDto>
}
