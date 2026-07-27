package com.example.aplicacion_fronton.network

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.aplicacion_fronton.BuildConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

sealed class GoogleAuthResultado {
    data class Exito(val idToken: String) : GoogleAuthResultado()
    data class Cancelado(val mensaje: String) : GoogleAuthResultado()
}

/** Envuelve Credential Manager — es la API vigente de Google para Sign-In en
 * Android (reemplaza a GoogleSignInClient, que está en camino a deprecarse).
 * Usa GetSignInWithGoogleOption, NO GetGoogleIdOption (One Tap): esta app
 * dispara el flujo desde un botón "Continuar con Google" tocado a mano, no
 * como sugerencia automática al abrir la pantalla — GetSignInWithGoogleOption
 * es la variante que Google documenta para ese caso, y a diferencia de One
 * Tap no le aplica al dispositivo el backoff silencioso que causaba
 * "16: [28439] the user has disabled the function..." tras un primer error
 * (p.ej. mientras el cliente OAuth de Android todavía no estaba registrado). */
object GoogleAuthService {
    suspend fun obtenerIdToken(context: Context): GoogleAuthResultado {
        val opcionGoogle = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID).build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(opcionGoogle)
            .build()

        return try {
            val resultado = CredentialManager.create(context).getCredential(context, request)
            val credencial = GoogleIdTokenCredential.createFrom(resultado.credential.data)
            GoogleAuthResultado.Exito(credencial.idToken)
        } catch (e: GetCredentialCancellationException) {
            GoogleAuthResultado.Cancelado("Acceso con Google cancelado.")
        } catch (e: GetCredentialException) {
            GoogleAuthResultado.Cancelado(e.message ?: "No se pudo completar el acceso con Google.")
        } catch (e: GoogleIdTokenParsingException) {
            GoogleAuthResultado.Cancelado("Respuesta de Google inválida.")
        }
    }
}
