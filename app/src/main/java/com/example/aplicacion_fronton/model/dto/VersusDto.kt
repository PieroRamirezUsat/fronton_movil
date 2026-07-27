package com.example.aplicacion_fronton.model.dto

import com.google.gson.annotations.SerializedName

enum class Modalidad {
    @SerializedName("individual") INDIVIDUAL,
    @SerializedName("dobles") DOBLES,
}

enum class EstadoVersus {
    @SerializedName("pendiente") PENDIENTE,
    @SerializedName("aceptado") ACEPTADO,
    @SerializedName("rechazado") RECHAZADO,
    @SerializedName("jugado") JUGADO,
    @SerializedName("confirmado") CONFIRMADO,
    @SerializedName("disputa") DISPUTA,
    @SerializedName("cancelado") CANCELADO,
}

data class VersusCreateDto(
    val rival_id: Int,
    val modalidad: Modalidad,
    val pareja_id: Int? = null,
    val rival_pareja_id: Int? = null,
    val fecha_hora: String, // ISO-8601, ej. "2026-08-05T18:00:00Z"
    val cancha: String? = null,
    val latitud: Double? = null,
    val longitud: Double? = null,
    val nota: String? = null
)

data class VersusDto(
    val id: Int,
    val modalidad: Modalidad,
    val jugador1_id: Int,
    val jugador2_id: Int,
    val pareja1_id: Int?,
    val pareja2_id: Int?,
    val fecha_hora: String,
    val cancha: String?,
    val latitud: Double?,
    val longitud: Double?,
    val nota: String?,
    val estado: EstadoVersus,
    val creado_por: Int,
    val created_at: String,
    val sets_jugador1: Int? = null,
    val sets_jugador2: Int? = null,
    val mi_reporte_enviado: Boolean = false,
    val cancelacion_solicitada_por: Int? = null
)

data class ReportarResultadoDto(
    val sets_propios: Int,
    val sets_rival: Int,
    val inasistencia_rival: Boolean = false
)

data class EloDeltaDto(
    val antes: Int,
    val despues: Int
)

data class ResultadoRespuestaDto(
    val status: Boolean,
    val estado: String,
    val mensaje: String,
    val elo: Map<String, EloDeltaDto>? = null,
    val predicciones_resueltas: Int? = null
)

/** true si `miId` juega del lado de jugador1 (jugador1 o su pareja de dobles).
 * Un simple "jugador1_id == miId" deja afuera a la pareja de dobles — alguien
 * que solo aparece como pareja1_id (nunca fue quien creó/aceptó el reto)
 * también necesita ubicarse en el equipo correcto para ver el rival real. */
fun VersusDto.soyEquipoJugador1(miId: Int): Boolean = jugador1_id == miId || pareja1_id == miId

/** Id del "rival" a mostrar en tarjetas de resumen (headline jugador1 vs
 * jugador2, sin desglosar parejas). Antes se calculaba con
 * "if (jugador1_id == miId) jugador2_id else jugador1_id" en cada pantalla —
 * para alguien que es pareja1_id (no jugador1 ni jugador2), esa fórmula
 * devolvía a su propio compañero de equipo como si fuera el rival. */
fun VersusDto.rivalIdPara(miId: Int): Int = if (soyEquipoJugador1(miId)) jugador2_id else jugador1_id
