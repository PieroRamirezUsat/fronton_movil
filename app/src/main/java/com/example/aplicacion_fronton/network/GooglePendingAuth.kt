package com.example.aplicacion_fronton.network

/**
 * Puente en memoria entre LoginScreen y RegistroScreen para el caso "cuenta de
 * Google nueva": el idToken ya está validado por el backend en /auth/google/iniciar,
 * pero falta categoría/mano/club (Google no los provee), así que el registro
 * termina en el wizard normal. No se persiste a disco — vive solo mientras dura
 * el flujo de registro en curso.
 */
object GooglePendingAuth {
    data class Datos(
        val idToken: String,
        val correo: String,
        val nombre: String,
        val fotoUrl: String?,
    )

    private var datos: Datos? = null

    fun guardar(datos: Datos) {
        this.datos = datos
    }

    /** Lee y limpia de una sola vez — un registro manual posterior no debe heredar estos datos. */
    fun consumir(): Datos? {
        val actual = datos
        datos = null
        return actual
    }
}
