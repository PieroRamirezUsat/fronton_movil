package com.example.aplicacion_fronton.model.dto

data class LoginRequestDto(
    val correo: String,
    val password: String
)

data class LoginResponseDto(
    val status: Boolean,
    val token: String,
    val data: UsuarioDto
)

data class ErrorResponseDto(
    val status: Boolean,
    val message: String
)

data class GoogleIniciarRequestDto(
    val id_token: String
)

// nuevo=true -> falta completar categoría/mano/club (Google no los provee);
// token/data solo vienen cuando nuevo=false (la cuenta ya existía).
data class GoogleIniciarResponseDto(
    val status: Boolean,
    val nuevo: Boolean,
    val token: String?,
    val data: UsuarioDto?,
    val correo: String?,
    val nombre: String?,
    val foto_url: String?
)

data class GoogleCompletarRequestDto(
    val id_token: String,
    val club: String?,
    val categoria_edad: CategoriaEdad,
    val mano_habil: ManoHabil
)

data class CambiarPasswordRequestDto(
    val password_actual: String,
    val password_nueva: String
)

data class SolicitarResetRequestDto(
    val correo: String
)

data class ConfirmarResetRequestDto(
    val correo: String,
    val codigo: String,
    val password_nueva: String
)
