package com.example.aplicacion_fronton.network

import okhttp3.Interceptor
import okhttp3.Response

/** Agrega el JWT a toda petición salvo login/register, y avisa por el
 * [AuthEventBus] si el servidor responde 401 (sesión inválida o expirada). */
class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val esRutaPublica = request.url.encodedPath.let {
            it.endsWith("/auth/login") || it.endsWith("/auth/register")
        }
        if (esRutaPublica) return chain.proceed(request)

        val token = tokenStore.obtenerToken()
        val peticion = if (token != null) {
            request.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            request
        }

        val response = chain.proceed(peticion)
        if (response.code == 401) {
            AuthEventBus.notificarSesionExpirada()
        }
        return response
    }
}
