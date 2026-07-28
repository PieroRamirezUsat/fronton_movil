package com.example.aplicacion_fronton.network

import android.content.Context
import android.content.SharedPreferences

/** Última división de Elo (hierro/bronce/plata/oro/platino) vista por usuario
 * en este dispositivo — mismo patrón exacto que `PosicionRankingStore`, para
 * detectar "subiste de división" comparando contra la división actual al
 * cargar Home. */
class DivisionRankingStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("fronton_prefs", Context.MODE_PRIVATE)

    fun obtenerDivisionGuardada(usuarioId: Int): String? = prefs.getString(clave(usuarioId), null)

    fun guardarDivision(usuarioId: Int, division: String) {
        prefs.edit().putString(clave(usuarioId), division).apply()
    }

    private fun clave(usuarioId: Int) = "$KEY_PREFIJO$usuarioId"

    companion object {
        private const val KEY_PREFIJO = "division_ranking_"
    }
}
