package com.example.aplicacion_fronton.model.dto

import com.google.gson.annotations.SerializedName

enum class EstadoCompromiso {
    @SerializedName("pendiente_invitacion") PENDIENTE_INVITACION,
    @SerializedName("aceptado") ACEPTADO,
    @SerializedName("rechazado") RECHAZADO,
    @SerializedName("saldado") SALDADO,
    @SerializedName("disputa") DISPUTA,
}

data class CompromisoCreateDto(
    val versus_id: Int,
    val invitado_id: Int,
    val descripcion: String,
    val fichas_cancha: Int? = null
)

data class CompromisoDto(
    val id: Int,
    val versus_id: Int,
    val creador_id: Int,
    val invitado_id: Int,
    val descripcion: String,
    val fichas_cancha: Int?,
    val estado: EstadoCompromiso,
    val comprobante_url: String?,
    val pagado_por: Int?,
    val confirmado_por: Int?,
    val created_at: String,
    val updated_at: String
)

data class SubirComprobanteDto(
    val comprobante_url: String
)
