package com.example.aplicacion_fronton.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.aplicacion_fronton.MainActivity
import com.example.aplicacion_fronton.R
import com.example.aplicacion_fronton.model.dto.TipoNotificacion
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private const val CANAL_NOTIFICACIONES = "fronton_notificaciones"
private const val EXTRA_TIPO = "push_tipo"
private const val EXTRA_VERSUS_ID = "push_versus_id"
private const val EXTRA_COMPROMISO_ID = "push_compromiso_id"

/** Mensaje SOLO de datos desde el backend (ver `app/push.py`) — a propósito,
 * así este método corre siempre (primer o segundo plano, no solo cuando la
 * app está abierta) y la notificación se arma acá mismo con un `PendingIntent`
 * que lleva los mismos datos que ya usa `abrirNotificacion()` para el tap
 * in-app, en vez de dejar que Android muestre una notificación genérica sin
 * navegación al tocarla. */
class FrontonMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch { PushTokenRegistrar.registrarTokenActual() }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val datos = message.data
        val titulo = datos["titulo"] ?: return
        val mensaje = datos["mensaje"] ?: ""
        val tipo = datos["tipo"]?.let {
            runCatching { Gson().fromJson("\"$it\"", TipoNotificacion::class.java) }.getOrNull()
        }
        val versusId = datos["versus_id"]?.toIntOrNull()
        val compromisoId = datos["compromiso_id"]?.toIntOrNull()

        crearCanalSiHaceFalta()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (tipo != null) putExtra(EXTRA_TIPO, tipo.name)
            versusId?.let { putExtra(EXTRA_VERSUS_ID, it) }
            compromisoId?.let { putExtra(EXTRA_COMPROMISO_ID, it) }
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notificacion = NotificationCompat.Builder(this, CANAL_NOTIFICACIONES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notificacion)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun crearCanalSiHaceFalta() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CANAL_NOTIFICACIONES) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CANAL_NOTIFICACIONES,
                "Retos y compromisos",
                NotificationManager.IMPORTANCE_HIGH,
            ),
        )
    }
}

/** Extras leídos por `MainActivity` desde el `Intent` que abrió la Activity
 * (tap en la notificación) — separado del cuerpo de la clase de arriba para
 * que `MainActivity` no dependa de ningún detalle interno del servicio. */
object PushIntentExtras {
    fun leer(intent: Intent?): PushNotificacionHolder.Datos? {
        if (intent == null) return null
        val tipoStr = intent.getStringExtra(EXTRA_TIPO) ?: return null
        val tipo = runCatching { TipoNotificacion.valueOf(tipoStr) }.getOrNull() ?: return null
        val versusId = if (intent.hasExtra(EXTRA_VERSUS_ID)) intent.getIntExtra(EXTRA_VERSUS_ID, 0) else null
        val compromisoId = if (intent.hasExtra(EXTRA_COMPROMISO_ID)) intent.getIntExtra(EXTRA_COMPROMISO_ID, 0) else null
        return PushNotificacionHolder.Datos(tipo, versusId, compromisoId)
    }
}
