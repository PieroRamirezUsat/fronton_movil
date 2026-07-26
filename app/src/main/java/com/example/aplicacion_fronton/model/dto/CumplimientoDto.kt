package com.example.aplicacion_fronton.model.dto

import com.google.gson.annotations.SerializedName

enum class NivelCumplimiento {
    @SerializedName("cumple_siempre") CUMPLE_SIEMPRE,
    @SerializedName("cumple_casi_siempre") CUMPLE_CASI_SIEMPRE,
    @SerializedName("historial_incumplimientos") HISTORIAL_INCUMPLIMIENTOS,
}

data class CumplimientoDto(
    val usuario_id: Int,
    val saldados: Int,
    val incumplidos: Int,
    val nivel: NivelCumplimiento
)

data class CompromisoHistorialItemDto(
    val id: Int,
    val descripcion: String,
    val estado: EstadoCompromiso,
    val fecha: String,
    val contraparte_nombre: String
)
