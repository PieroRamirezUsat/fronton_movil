package com.example.aplicacion_fronton.model.dto

import com.google.gson.annotations.SerializedName

enum class EstadoPrediccion {
    @SerializedName("pendiente") PENDIENTE,
    @SerializedName("acertada") ACERTADA,
    @SerializedName("fallada") FALLADA,
}

data class PrediccionCreateDto(
    val versus_id: Int,
    val jugador_predicho_id: Int,
    val fichas_apostadas: Int
)

data class PrediccionDto(
    val id: Int,
    val versus_id: Int,
    val usuario_id: Int,
    val jugador_predicho_id: Int,
    val fichas_apostadas: Int,
    val estado: EstadoPrediccion,
    val created_at: String
)

data class PredictorRankingEntryDto(
    val posicion: Int,
    val usuario_id: Int,
    val nombre: String,
    val fichas_cancha: Int
)
