package com.example.aplicacion_fronton.model.dto

import java.time.OffsetDateTime

/** true si `this` (un `fecha_hora` ISO-8601 de un versus, ej.
 * "2026-08-05T18:00:00Z") ya quedó en el pasado respecto a ahora — usado para
 * ocultar del lado del cliente acciones que el backend ya rechaza una vez que
 * el partido comenzó (crear un compromiso), como mejora de UX. La validación
 * real siempre vive en el backend, esto es solo para no dejar ver un botón
 * que va a fallar. `OffsetDateTime.parse` acepta tanto el sufijo "Z" como un
 * offset explícito ("+00:00"), los dos formatos válidos que puede devolver
 * el backend. */
fun String.yaComenzoElVersus(): Boolean = try {
    OffsetDateTime.parse(this).isBefore(OffsetDateTime.now())
} catch (e: Exception) {
    false
}
