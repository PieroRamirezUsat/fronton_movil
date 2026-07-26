package com.example.aplicacion_fronton.network

import com.example.aplicacion_fronton.model.dto.ConteoNoLeidasDto
import com.example.aplicacion_fronton.model.dto.NotificacionDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificacionesService {
    @GET("notificaciones")
    suspend fun listarMisNotificaciones(): Response<List<NotificacionDto>>

    @GET("notificaciones/no-leidas/conteo")
    suspend fun contarNoLeidas(@Query("solo_apuestas") soloApuestas: Boolean = false): Response<ConteoNoLeidasDto>

    @POST("notificaciones/{id}/marcar-leida")
    suspend fun marcarLeida(@Path("id") notificacionId: Int): Response<NotificacionDto>

    @POST("notificaciones/marcar-todas-leidas")
    suspend fun marcarTodasLeidas(): Response<Map<String, Boolean>>
}
