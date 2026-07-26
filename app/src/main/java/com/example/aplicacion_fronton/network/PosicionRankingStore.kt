package com.example.aplicacion_fronton.network

import android.content.Context
import android.content.SharedPreferences

/** Última posición de ranking vista por usuario en este dispositivo — permite
 * detectar "subiste en el ranking" comparando contra la posición actual al
 * cargar Home, mismo patrón que `TokenStore`/`DuelosMostradosStore`. */
class PosicionRankingStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("fronton_prefs", Context.MODE_PRIVATE)

    fun obtenerPosicionGuardada(usuarioId: Int): Int? {
        val valor = prefs.getInt(clave(usuarioId), -1)
        return if (valor == -1) null else valor
    }

    fun guardarPosicion(usuarioId: Int, posicion: Int) {
        prefs.edit().putInt(clave(usuarioId), posicion).apply()
    }

    private fun clave(usuarioId: Int) = "$KEY_PREFIJO$usuarioId"

    companion object {
        private const val KEY_PREFIJO = "posicion_ranking_"
    }
}
