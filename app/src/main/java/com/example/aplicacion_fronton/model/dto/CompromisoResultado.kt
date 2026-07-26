package com.example.aplicacion_fronton.model.dto

/** Equipo (1 o 2) al que pertenece `id` dentro de `v`, o null si no juega —
 * soporta dobles (jugador1_id/pareja1_id vs jugador2_id/pareja2_id). */
private fun VersusDto.equipoDe(id: Int): Int? = when (id) {
    jugador1_id, pareja1_id -> 1
    jugador2_id, pareja2_id -> 2
    else -> null
}

/** true si el CREADOR del compromiso ganó la apuesta, false si perdió, null
 * si no es derivable — solo lo es cuando creador e invitado jugaron en
 * equipos opuestos del versus vinculado y ese versus ya está CONFIRMADO con
 * marcador (una apuesta entre dos espectadores, o sobre un versus todavía
 * sin confirmar, no tiene "ganador de la apuesta" todavía). */
fun ganoElCreadorDelCompromiso(compromiso: CompromisoDto, versus: VersusDto?): Boolean? {
    if (versus == null || versus.estado != EstadoVersus.CONFIRMADO) return null
    val sets1 = versus.sets_jugador1 ?: return null
    val sets2 = versus.sets_jugador2 ?: return null
    if (sets1 == sets2) return null
    val equipoCreador = versus.equipoDe(compromiso.creador_id) ?: return null
    val equipoInvitado = versus.equipoDe(compromiso.invitado_id) ?: return null
    if (equipoCreador == equipoInvitado) return null
    val equipo1Gano = sets1 > sets2
    return (equipoCreador == 1) == equipo1Gano
}

/** Desde el punto de vista de `miId` (siempre creador o invitado del
 * compromiso) — true si gané la apuesta, false si perdí, null si no es
 * derivable todavía. */
fun esGanadorDelCompromiso(compromiso: CompromisoDto, versus: VersusDto?, miId: Int): Boolean? =
    ganoElCreadorDelCompromiso(compromiso, versus)?.let { creadorGano ->
        if (compromiso.creador_id == miId) creadorGano else !creadorGano
    }
