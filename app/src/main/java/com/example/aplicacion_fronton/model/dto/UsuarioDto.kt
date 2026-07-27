package com.example.aplicacion_fronton.model.dto

import com.google.gson.annotations.SerializedName

// Nota: los campos de los DTO usan snake_case a propósito, para reflejar 1:1 el
// JSON que manda la API (FastAPI/Pydantic) — evita tener que anotar @SerializedName
// en cada campo, solo hace falta en los enums (Gson serializa enums por su nombre
// de constante Kotlin salvo que se anote).

enum class CategoriaEdad {
    @SerializedName("menores") MENORES,
    @SerializedName("libre") LIBRE,
    @SerializedName("mas_40") MAS_40,
    @SerializedName("mas_50") MAS_50,
    @SerializedName("mas_60") MAS_60,
}

enum class ManoHabil {
    @SerializedName("diestro") DIESTRO,
    @SerializedName("zurdo") ZURDO,
}

enum class Genero {
    @SerializedName("masculino") MASCULINO,
    @SerializedName("femenino") FEMENINO,
}

data class UsuarioDto(
    val id: Int,
    val nombre: String,
    val correo: String,
    val foto_url: String?,
    val club: String?,
    val categoria_edad: CategoriaEdad,
    val mano_habil: ManoHabil,
    // Nullable — cuentas creadas antes de que este campo existiera todavía
    // no lo tienen (se les pide completarlo, ver CompletarPerfilScreen).
    val genero: Genero?,
    val elo_individual: Int,
    val elo_dobles: Int,
    val fichas_cancha: Int,
    val pareja_habitual_id: Int?,
    val created_at: String
)

data class ParejaHabitualRequestDto(
    val pareja_habitual_id: Int?
)

data class PushTokenRequestDto(
    val token: String?
)

data class ActualizarPerfilRequestDto(
    val club: String?,
    val categoria_edad: CategoriaEdad
)

data class ActualizarGeneroRequestDto(
    val genero: Genero
)

data class UsuarioCreateDto(
    val nombre: String,
    val correo: String,
    val password: String,
    val club: String?,
    val categoria_edad: CategoriaEdad,
    val mano_habil: ManoHabil,
    val genero: Genero
)
