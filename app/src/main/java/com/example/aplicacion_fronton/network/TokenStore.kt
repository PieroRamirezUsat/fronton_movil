package com.example.aplicacion_fronton.network

import android.content.Context
import android.content.SharedPreferences

/** Persistencia del JWT y del id de usuario logueado — un solo lugar, no repetido
 * por pantalla. */
class TokenStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("fronton_prefs", Context.MODE_PRIVATE)

    fun guardarToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun obtenerToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun haySesionActiva(): Boolean = obtenerToken() != null

    fun limpiar() {
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    companion object {
        private const val KEY_TOKEN = "jwt_token"
    }
}
