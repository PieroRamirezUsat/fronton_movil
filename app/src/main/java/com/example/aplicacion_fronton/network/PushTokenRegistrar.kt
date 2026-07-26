package com.example.aplicacion_fronton.network

import com.example.aplicacion_fronton.model.dto.PushTokenRequestDto
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/** Un solo lugar para registrar/borrar el token de FCM en el backend — lo usan
 * tanto `HomeScreen` (al abrir con sesión activa) como `FrontonMessagingService`
 * (cuando el SDK rota el token) para no duplicar la lógica. */
object PushTokenRegistrar {
    suspend fun registrarTokenActual() {
        if (!RetrofitClient.tokenStore.haySesionActiva()) return
        val token = runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull() ?: return
        safeApiCall { RetrofitClient.usuariosService.actualizarPushToken(PushTokenRequestDto(token)) }
    }

    /** Se llama antes de limpiar la sesión local en logout — mejor esfuerzo,
     * nunca bloquea el logout si falla (sin conexión, token ya vencido, etc.). */
    suspend fun borrarTokenDelServidor() {
        runCatching {
            safeApiCall { RetrofitClient.usuariosService.actualizarPushToken(PushTokenRequestDto(null)) }
        }
    }
}
