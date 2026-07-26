package com.example.aplicacion_fronton.network

import com.example.aplicacion_fronton.model.dto.CambiarPasswordRequestDto
import com.example.aplicacion_fronton.model.dto.ConfirmarResetRequestDto
import com.example.aplicacion_fronton.model.dto.ErrorResponseDto
import com.example.aplicacion_fronton.model.dto.GoogleCompletarRequestDto
import com.example.aplicacion_fronton.model.dto.GoogleIniciarRequestDto
import com.example.aplicacion_fronton.model.dto.GoogleIniciarResponseDto
import com.example.aplicacion_fronton.model.dto.LoginRequestDto
import com.example.aplicacion_fronton.model.dto.LoginResponseDto
import com.example.aplicacion_fronton.model.dto.SolicitarResetRequestDto
import com.example.aplicacion_fronton.model.dto.UsuarioCreateDto
import com.example.aplicacion_fronton.model.dto.UsuarioDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @POST("auth/register")
    suspend fun registrar(@Body datos: UsuarioCreateDto): Response<UsuarioDto>

    @POST("auth/login")
    suspend fun iniciarSesion(@Body datos: LoginRequestDto): Response<LoginResponseDto>

    @GET("auth/me")
    suspend fun obtenerMiPerfil(): Response<UsuarioDto>

    @POST("auth/cambiar-password")
    suspend fun cambiarPassword(@Body datos: CambiarPasswordRequestDto): Response<Map<String, Boolean>>

    @POST("auth/solicitar-reset")
    suspend fun solicitarReset(@Body datos: SolicitarResetRequestDto): Response<ErrorResponseDto>

    @POST("auth/confirmar-reset")
    suspend fun confirmarReset(@Body datos: ConfirmarResetRequestDto): Response<Map<String, Boolean>>

    @POST("auth/google/iniciar")
    suspend fun googleIniciar(@Body datos: GoogleIniciarRequestDto): Response<GoogleIniciarResponseDto>

    @POST("auth/google/completar")
    suspend fun googleCompletar(@Body datos: GoogleCompletarRequestDto): Response<LoginResponseDto>
}
