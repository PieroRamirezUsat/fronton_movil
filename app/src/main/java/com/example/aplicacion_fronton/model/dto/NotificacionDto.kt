package com.example.aplicacion_fronton.model.dto

import com.google.gson.annotations.SerializedName

enum class TipoNotificacion {
    @SerializedName("reto_recibido") RETO_RECIBIDO,
    @SerializedName("reto_aceptado") RETO_ACEPTADO,
    @SerializedName("reto_rechazado") RETO_RECHAZADO,
    @SerializedName("marcador_pendiente") MARCADOR_PENDIENTE,
    @SerializedName("resultado_confirmado") RESULTADO_CONFIRMADO,
    @SerializedName("resultado_disputa") RESULTADO_DISPUTA,
    @SerializedName("compromiso_recibido") COMPROMISO_RECIBIDO,
    @SerializedName("compromiso_aceptado") COMPROMISO_ACEPTADO,
    @SerializedName("compromiso_rechazado") COMPROMISO_RECHAZADO,
    @SerializedName("compromiso_comprobante_subido") COMPROMISO_COMPROBANTE_SUBIDO,
    @SerializedName("compromiso_saldado") COMPROMISO_SALDADO,
    @SerializedName("compromiso_disputa") COMPROMISO_DISPUTA,
}

data class NotificacionDto(
    val id: Int,
    val tipo: TipoNotificacion,
    val titulo: String,
    val mensaje: String,
    val versus_id: Int?,
    val compromiso_id: Int?,
    val leida: Boolean,
    val created_at: String
)

data class ConteoNoLeidasDto(
    val conteo: Int
)
