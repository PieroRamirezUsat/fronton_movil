package com.example.aplicacion_fronton.model.dto

data class RankingEntryDto(
    val posicion: Int,
    val id: Int,
    val nombre: String,
    val foto_url: String?,
    val club: String?,
    val categoria_edad: CategoriaEdad,
    val elo: Int,
    // División estilo LoL (hierro/bronce/plata/oro/platino), calculada en el
    // backend a partir del elo — ver divisionColorYIcono() en RankingScreen.
    val division: String,
    val genero: Genero?,
    val pareja_habitual_id: Int?
)
