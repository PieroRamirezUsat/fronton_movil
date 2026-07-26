package com.example.aplicacion_fronton.ui.perfil

import com.example.aplicacion_fronton.model.dto.CategoriaEdad

/**
 * Puente en memoria entre RankingScreen y PerfilJugadorScreen: el ranking ya
 * trae nombre/foto/club/categoría/elo actualizados de ese jugador, así que no
 * hace falta un endpoint nuevo "GET /usuarios/{id}" solo para mostrarlos —
 * solo el cumplimiento (que sí es genérico para cualquier id) se pide fresco
 * en la pantalla de destino. Mismo patrón que [[GooglePendingAuth]].
 */
object PerfilJugadorHolder {
    data class Datos(
        val usuarioId: Int,
        val nombre: String,
        val fotoUrl: String?,
        val club: String?,
        val categoriaEdad: CategoriaEdad,
        val elo: Int,
        val modalidad: String,
        val posicionRanking: Int,
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
