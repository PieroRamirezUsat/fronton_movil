package com.example.aplicacion_fronton.network

import com.example.aplicacion_fronton.model.dto.DenunciaCreateRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DenunciasService {
    @POST("denuncias")
    suspend fun crearDenuncia(@Body datos: DenunciaCreateRequest): Response<Map<String, Boolean>>
}
