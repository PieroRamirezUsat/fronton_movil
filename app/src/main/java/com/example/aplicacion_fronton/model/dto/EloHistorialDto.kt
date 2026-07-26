package com.example.aplicacion_fronton.model.dto

data class EloHistorialPuntoDto(
    val versus_id: Int,
    val elo_antes: Int,
    val elo_despues: Int,
    val fecha: String
)
