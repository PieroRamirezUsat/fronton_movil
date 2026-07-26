package com.example.aplicacion_fronton.model.dto

data class RankingEntryDto(
    val posicion: Int,
    val id: Int,
    val nombre: String,
    val foto_url: String?,
    val club: String?,
    val categoria_edad: CategoriaEdad,
    val elo: Int,
    val pareja_habitual_id: Int?
)
