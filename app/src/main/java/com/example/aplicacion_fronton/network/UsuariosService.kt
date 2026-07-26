package com.example.aplicacion_fronton.network

import com.example.aplicacion_fronton.model.dto.ActualizarPerfilRequestDto
import com.example.aplicacion_fronton.model.dto.CompromisoHistorialItemDto
import com.example.aplicacion_fronton.model.dto.CumplimientoDto
import com.example.aplicacion_fronton.model.dto.EloHistorialPuntoDto
import com.example.aplicacion_fronton.model.dto.ParejaHabitualRequestDto
import com.example.aplicacion_fronton.model.dto.PushTokenRequestDto
import com.example.aplicacion_fronton.model.dto.UsuarioDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface UsuariosService {
    @GET("usuarios/{id}/cumplimiento")
    suspend fun obtenerCumplimiento(@Path("id") usuarioId: Int): Response<CumplimientoDto>

    @GET("usuarios/{id}/cumplimiento/historial")
    suspend fun obtenerHistorialCumplimiento(@Path("id") usuarioId: Int): Response<List<CompromisoHistorialItemDto>>

    @Multipart
    @POST("usuarios/me/foto")
    suspend fun subirFotoPerfil(@Part archivo: MultipartBody.Part): Response<UsuarioDto>

    @PATCH("usuarios/me/pareja-habitual")
    suspend fun actualizarParejaHabitual(@Body datos: ParejaHabitualRequestDto): Response<UsuarioDto>

    // Se llama tras registrar el token de FCM en el dispositivo (login/registro/
    // arranque con sesión activa) y con token=null en logout — ver checklist de
    // notificaciones push, todavía pendiente de agregar el SDK de Firebase.
    @PATCH("usuarios/me/push-token")
    suspend fun actualizarPushToken(@Body datos: PushTokenRequestDto): Response<UsuarioDto>

    @PATCH("usuarios/me")
    suspend fun actualizarPerfil(@Body datos: ActualizarPerfilRequestDto): Response<UsuarioDto>

    @DELETE("usuarios/me")
    suspend fun eliminarCuenta(): Response<Map<String, Boolean>>

    @GET("usuarios/me/elo-historial")
    suspend fun obtenerEloHistorial(@Query("modalidad") modalidad: String = "individual"): Response<List<EloHistorialPuntoDto>>
}
