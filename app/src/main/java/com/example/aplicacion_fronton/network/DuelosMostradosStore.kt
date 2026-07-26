package com.example.aplicacion_fronton.network

import android.content.Context
import android.content.SharedPreferences

/** Ids de notificación `reto_recibido` cuya animación de duelo ya se mostró en
 * este dispositivo — deliberadamente local y no server-side: `leida` en
 * `Notificacion` ya alimenta el contador de la campana en Home, y si el
 * duelo marcara eso como leído automáticamente el usuario perdería el aviso
 * normal sin haber abierto Notificaciones. "Ya vi la animación" es un
 * concepto puramente de este dispositivo, mismo patrón que `TokenStore`. */
class DuelosMostradosStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("fronton_prefs", Context.MODE_PRIVATE)

    fun yaMostrado(notificacionId: Int): Boolean =
        prefs.getStringSet(KEY_MOSTRADOS, emptySet())?.contains(notificacionId.toString()) == true

    fun marcarMostrado(notificacionId: Int) {
        val actuales = prefs.getStringSet(KEY_MOSTRADOS, emptySet())?.toMutableSet() ?: mutableSetOf()
        actuales.add(notificacionId.toString())
        prefs.edit().putStringSet(KEY_MOSTRADOS, actuales).apply()
    }

    companion object {
        private const val KEY_MOSTRADOS = "duelos_mostrados"
    }
}
