package com.example.aplicacion_fronton.network

import android.content.Context
import android.net.Uri
import com.example.aplicacion_fronton.BuildConfig
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Lee los bytes de una foto elegida por el usuario y arma la parte multipart
 * para subirla — vive fuera del ViewModel a propósito, porque leer un `Uri`
 * necesita un `Context` y los ViewModel de esta app no lo tienen. */
fun uriAParteMultipart(context: Context, uri: Uri, nombreCampo: String = "archivo"): MultipartBody.Part {
    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: error("No se pudo leer la imagen seleccionada.")
    val cuerpo = bytes.toRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(nombreCampo, "foto.jpg", cuerpo)
}

/** `foto_url` puede venir absoluta (la trae Google así) o relativa (subida
 * propia al backend, ej. "/static/fotos_perfil/6.jpg?v=..."). Coil necesita
 * la URL completa en ambos casos. */
fun String?.urlCompletaFoto(): String? {
    if (this == null) return null
    if (startsWith("http")) return this
    return BuildConfig.BASE_URL.trimEnd('/') + this
}
