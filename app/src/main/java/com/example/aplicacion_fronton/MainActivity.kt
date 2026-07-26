package com.example.aplicacion_fronton

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.aplicacion_fronton.network.PushIntentExtras
import com.example.aplicacion_fronton.network.PushNotificacionHolder
import com.example.aplicacion_fronton.ui.navigation.AppNavigation
import com.example.aplicacion_fronton.ui.theme.Aplicacion_FrontonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        guardarPushExtrasSiHay(intent)
        setContent {
            Aplicacion_FrontonTheme {
                AppNavigation()
            }
        }
    }

    // launchMode="singleTop" — si la app ya está abierta y se toca otra
    // notificación, llega acá en vez de crear una Activity nueva.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        guardarPushExtrasSiHay(intent)
    }

    private fun guardarPushExtrasSiHay(intent: Intent?) {
        PushIntentExtras.leer(intent)?.let { PushNotificacionHolder.guardar(it) }
    }
}
