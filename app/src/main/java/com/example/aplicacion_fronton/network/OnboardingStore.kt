package com.example.aplicacion_fronton.network

import android.content.Context
import android.content.SharedPreferences

/** "Ya vio el tutorial de bienvenida" — puramente local (no hay concepto de
 * onboarding en el backend), mismo patrón que [DuelosMostradosStore]. Se
 * marca apenas se sale del onboarding (saltado o completado), nunca se
 * vuelve a mostrar solo, aunque la cuenta cambie de dispositivo. */
class OnboardingStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("fronton_prefs", Context.MODE_PRIVATE)

    fun yaVisto(): Boolean = prefs.getBoolean(KEY_VISTO, false)

    fun marcarVisto() {
        prefs.edit().putBoolean(KEY_VISTO, true).apply()
    }

    companion object {
        private const val KEY_VISTO = "onboarding_visto"
    }
}
