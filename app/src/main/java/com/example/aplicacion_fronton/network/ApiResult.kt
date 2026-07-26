package com.example.aplicacion_fronton.network

import com.example.aplicacion_fronton.model.dto.ErrorResponseDto
import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException

sealed class ApiResult<out T> {
    data class Exito<T>(val datos: T) : ApiResult<T>()
    data class Error(val mensaje: String) : ApiResult<Nothing>()
}

private val gson = Gson()

/**
 * Ejecuta una llamada de Retrofit y SIEMPRE devuelve un mensaje humano legible en
 * caso de error — nunca deja que un código HTTP crudo llegue a la UI sin haber
 * leído el cuerpo real del error.
 *
 * Esto existe por un bug real de TutorMath: el manejo de HttpException en Retrofit
 * no leía el body del error y mostraba "Error del servidor (401)" en vez del
 * mensaje que la API sí mandaba. Escribirlo una sola vez acá evita repetir ese
 * bug en cada ViewModel.
 */
suspend fun <T> safeApiCall(llamada: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val respuesta = llamada()
        val cuerpo = respuesta.body()
        if (respuesta.isSuccessful && cuerpo != null) {
            ApiResult.Exito(cuerpo)
        } else {
            ApiResult.Error(leerMensajeDeError(respuesta))
        }
    } catch (e: IOException) {
        ApiResult.Error("No se pudo conectar al servidor. Revisa tu conexión.")
    } catch (e: Exception) {
        ApiResult.Error("Ocurrió un error inesperado.")
    }
}

private fun <T> leerMensajeDeError(respuesta: Response<T>): String {
    val cuerpoError = respuesta.errorBody()?.string() ?: return "Ocurrió un error inesperado."
    return try {
        gson.fromJson(cuerpoError, ErrorResponseDto::class.java)?.message ?: "Ocurrió un error inesperado."
    } catch (e: Exception) {
        "Ocurrió un error inesperado."
    }
}
