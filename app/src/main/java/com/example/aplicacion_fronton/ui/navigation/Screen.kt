package com.example.aplicacion_fronton.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Registro : Screen("registro")
    data object OlvideContrasena : Screen("olvide_contrasena")
    data object ConfirmarReset : Screen("confirmar_reset/{correo}") {
        fun ruta(correo: String) = "confirmar_reset/${Uri.encode(correo)}"
    }
    data object Tabs : Screen("tabs")
    data object CompletarPerfil : Screen("completar_perfil")
    data object PerfilJugador : Screen("perfil_jugador")
    data object CrearReto : Screen("crear_reto")
    data object DetalleVersus : Screen("detalle_versus/{versusId}") {
        fun ruta(versusId: Int) = "detalle_versus/$versusId"
    }
    data object ReportarMarcador : Screen("reportar_marcador/{versusId}") {
        fun ruta(versusId: Int) = "reportar_marcador/$versusId"
    }
    data object HistorialVersus : Screen("historial_versus")
    data object BuscarRivales : Screen("buscar_rivales")
    data object Ajustes : Screen("ajustes")
    data object RegistrarCompromiso : Screen("registrar_compromiso/{versusId}") {
        fun ruta(versusId: Int) = "registrar_compromiso/$versusId"
    }
    data object InvitacionCompromiso : Screen("invitacion_compromiso/{compromisoId}") {
        fun ruta(compromisoId: Int) = "invitacion_compromiso/$compromisoId"
    }
    data object AdjuntarComprobante : Screen("adjuntar_comprobante/{compromisoId}") {
        fun ruta(compromisoId: Int) = "adjuntar_comprobante/$compromisoId"
    }
    data object VerificarComprobante : Screen("verificar_comprobante/{compromisoId}") {
        fun ruta(compromisoId: Int) = "verificar_comprobante/$compromisoId"
    }
    data object DetalleCompromiso : Screen("detalle_compromiso/{compromisoId}") {
        fun ruta(compromisoId: Int) = "detalle_compromiso/$compromisoId"
    }
    data object ReporteApuestas : Screen("reporte_apuestas")
    data object BuscarVersus : Screen("buscar_versus")
    data object DueloReto : Screen("duelo_reto/{versusId}") {
        fun ruta(versusId: Int) = "duelo_reto/$versusId"
    }
    data object VictoriaPartido : Screen("victoria_partido/{versusId}/{eloAntes}/{eloDespues}") {
        fun ruta(versusId: Int, eloAntes: Int, eloDespues: Int) = "victoria_partido/$versusId/$eloAntes/$eloDespues"
    }
    data object VictoriaApuesta : Screen("victoria_apuesta/{compromisoId}") {
        fun ruta(compromisoId: Int) = "victoria_apuesta/$compromisoId"
    }
    data object SubidaRanking : Screen("subida_ranking/{posicionAnterior}/{posicionNueva}") {
        fun ruta(posicionAnterior: Int, posicionNueva: Int) = "subida_ranking/$posicionAnterior/$posicionNueva"
    }
}
