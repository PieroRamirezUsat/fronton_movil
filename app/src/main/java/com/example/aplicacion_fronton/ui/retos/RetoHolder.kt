package com.example.aplicacion_fronton.ui.retos

import com.example.aplicacion_fronton.model.dto.CategoriaEdad

/** Mismo patrón que `PerfilJugadorHolder`/`GooglePendingAuth`: puente en memoria
 * para pasar los datos del rival (ya conocidos desde Ranking/Perfil del Jugador)
 * a la pantalla de Crear Reto sin tener que volver a pedirlos. */
object RetoHolder {
    data class Datos(
        val rivalId: Int,
        val rivalNombre: String,
        val rivalFotoUrl: String?,
        val rivalCategoria: CategoriaEdad,
        val rivalElo: Int,
    )

    private var datos: Datos? = null

    fun guardar(datos: Datos) {
        this.datos = datos
    }

    fun consumir(): Datos? {
        val actual = datos
        datos = null
        return actual
    }
}
