package com.example.aplicacion_fronton.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aplicacion_fronton.network.PushNotificacionHolder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aplicacion_fronton.model.dto.NotificacionDto
import com.example.aplicacion_fronton.model.dto.TipoNotificacion
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.AuthEventBus
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import com.example.aplicacion_fronton.ui.ajustes.AjustesScreen
import com.example.aplicacion_fronton.ui.auth.login.ConfirmarResetScreen
import com.example.aplicacion_fronton.ui.auth.login.LoginScreen
import com.example.aplicacion_fronton.ui.auth.login.OlvideContrasenaScreen
import com.example.aplicacion_fronton.ui.auth.registro.RegistroScreen
import com.example.aplicacion_fronton.ui.compromisos.AdjuntarComprobanteScreen
import com.example.aplicacion_fronton.ui.compromisos.DetalleCompromisoScreen
import com.example.aplicacion_fronton.ui.compromisos.ReporteApuestasScreen
import com.example.aplicacion_fronton.ui.compromisos.InvitacionCompromisoScreen
import com.example.aplicacion_fronton.ui.compromisos.RegistrarCompromisoScreen
import com.example.aplicacion_fronton.ui.compromisos.VerificarComprobanteScreen
import com.example.aplicacion_fronton.ui.compromisos.VictoriaApuestaScreen
import com.example.aplicacion_fronton.ui.perfil.PerfilJugadorHolder
import com.example.aplicacion_fronton.ui.perfil.PerfilJugadorScreen
import com.example.aplicacion_fronton.ui.ranking.SubidaRankingScreen
import com.example.aplicacion_fronton.ui.retos.BuscarRivalesScreen
import com.example.aplicacion_fronton.ui.retos.BuscarVersusScreen
import com.example.aplicacion_fronton.ui.retos.CrearRetoScreen
import com.example.aplicacion_fronton.ui.retos.DetalleVersusScreen
import com.example.aplicacion_fronton.ui.retos.DueloRetoScreen
import com.example.aplicacion_fronton.ui.retos.HistorialVersusScreen
import com.example.aplicacion_fronton.ui.retos.ReportarMarcadorScreen
import com.example.aplicacion_fronton.ui.retos.RetoHolder
import com.example.aplicacion_fronton.ui.retos.VictoriaPartidoScreen
import com.example.aplicacion_fronton.ui.splash.SplashScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    // El FAB "Retar" de la barra inferior es el mismo en las 5 pantallas raíz
    // — un solo lambda en vez de repetir la misma navegación 5 veces.
    val irABuscarRivales: () -> Unit = { navController.navigate(Screen.BuscarRivales.route) }

    // 401 en cualquier request de cualquier pantalla -> vuelve a Login limpiando
    // el back stack, sin que cada pantalla tenga que manejarlo por su cuenta.
    LaunchedEffect(Unit) {
        AuthEventBus.sesionExpirada.collect {
            volverALogin(navController)
        }
    }

    // Tap a una notificación push — reactivo (StateFlow, no un simple
    // nullable) porque cubre tanto abrir la app de cero (MainActivity.onCreate
    // guarda el dato antes de que esto se componga por primera vez) como con
    // la app ya abierta (onNewIntent la actualiza y esto recompone solo).
    val pushPendiente by PushNotificacionHolder.pendiente.collectAsStateWithLifecycle()
    LaunchedEffect(pushPendiente) {
        val datos = pushPendiente ?: return@LaunchedEffect
        if (RetrofitClient.tokenStore.haySesionActiva()) {
            abrirNotificacion(navController, datos.tipo, datos.versusId, datos.compromisoId)
            PushNotificacionHolder.consumir()
        }
    }

    // Antes cada cambio de pantalla era un corte instantáneo (Navigation
    // Compose sin transición propia) — con toda la animación fina que ya
    // existe pantalla por pantalla (cascada en Home, confeti, etc.), un salto
    // seco entre ellas hacía que la app entera se sintiera a medio terminar.
    // Definido acá una sola vez, para las ~20 rutas del NavHost.
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { it / 4 } },
        exitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { -it / 6 } },
        popEnterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(220)) { -it / 4 } },
        popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(180)) { it / 6 } },
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onTerminado = {
                    scope.launch {
                        val destino = if (RetrofitClient.tokenStore.haySesionActiva()) destinoTrasIniciarSesion() else Screen.Login.route
                        navController.navigate(destino) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                },
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginExitoso = { scope.launch { irAHomeLimpiandoBackStack(navController) } },
                onIrARegistro = { navController.navigate(Screen.Registro.route) },
                onOlvideContrasena = { navController.navigate(Screen.OlvideContrasena.route) },
            )
        }
        composable(Screen.OlvideContrasena.route) {
            OlvideContrasenaScreen(
                onVolver = { navController.popBackStack() },
                onCodigoEnviado = { correo ->
                    navController.navigate(Screen.ConfirmarReset.ruta(correo))
                },
            )
        }
        composable(
            Screen.ConfirmarReset.route,
            arguments = listOf(navArgument("correo") { type = NavType.StringType }),
        ) { backStackEntry ->
            val correo = backStackEntry.arguments?.getString("correo") ?: ""
            ConfirmarResetScreen(
                correo = correo,
                onVolver = { navController.popBackStack() },
                onListo = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.Registro.route) {
            RegistroScreen(
                onRegistroExitoso = { scope.launch { irAHomeLimpiandoBackStack(navController) } },
                onVolverALogin = { navController.popBackStack() },
            )
        }
        composable(Screen.Tabs.route) { backStackEntry ->
            TabsShellScreen(
                backStackEntry = backStackEntry,
                onVersusSeleccionado = { versusId -> navController.navigate(Screen.DetalleVersus.ruta(versusId)) },
                onRetar = irABuscarRivales,
                onSubidaRanking = { posicionAnterior, posicionNueva ->
                    navController.navigate(Screen.SubidaRanking.ruta(posicionAnterior, posicionNueva))
                },
                onJugadorSeleccionado = { entrada, modalidad ->
                    PerfilJugadorHolder.guardar(
                        PerfilJugadorHolder.Datos(
                            usuarioId = entrada.id,
                            nombre = entrada.nombre,
                            fotoUrl = entrada.foto_url,
                            club = entrada.club,
                            categoriaEdad = entrada.categoria_edad,
                            elo = entrada.elo,
                            modalidad = modalidad,
                            posicionRanking = entrada.posicion,
                        ),
                    )
                    navController.navigate(Screen.PerfilJugador.route)
                },
                onVerDetalleReto = { versusId -> navController.navigate(Screen.DetalleVersus.ruta(versusId)) },
                onSesionCerrada = { volverALogin(navController) },
                onVerHistorial = { navController.navigate(Screen.HistorialVersus.route) },
                onAjustes = { navController.navigate(Screen.Ajustes.route) },
                onAbrirNotificacion = { notificacion ->
                    abrirNotificacion(navController, notificacion.tipo, notificacion.versus_id, notificacion.compromiso_id)
                },
                onBuscarRivalesNotif = { navController.navigate(Screen.BuscarRivales.route) },
                onVerInvitacion = { compromisoId -> navController.navigate(Screen.InvitacionCompromiso.ruta(compromisoId)) },
                onAdjuntar = { compromisoId -> navController.navigate(Screen.AdjuntarComprobante.ruta(compromisoId)) },
                onVerificar = { compromisoId -> navController.navigate(Screen.VerificarComprobante.ruta(compromisoId)) },
                onVerDetalleCompromiso = { compromisoId -> navController.navigate(Screen.DetalleCompromiso.ruta(compromisoId)) },
                onVerReporte = { navController.navigate(Screen.ReporteApuestas.route) },
                onBuscarRivalesApuestas = { navController.navigate(Screen.BuscarRivales.route) },
                onBuscarVersus = { navController.navigate(Screen.BuscarVersus.route) },
            )
        }
        composable(Screen.BuscarRivales.route) {
            BuscarRivalesScreen(
                onVolver = { navController.popBackStack() },
                onJugadorSeleccionado = { entrada ->
                    PerfilJugadorHolder.guardar(
                        PerfilJugadorHolder.Datos(
                            usuarioId = entrada.id,
                            nombre = entrada.nombre,
                            fotoUrl = entrada.foto_url,
                            club = entrada.club,
                            categoriaEdad = entrada.categoria_edad,
                            elo = entrada.elo,
                            modalidad = "individual",
                            posicionRanking = entrada.posicion,
                        ),
                    )
                    navController.navigate(Screen.PerfilJugador.route)
                },
                onRetar = { entrada ->
                    RetoHolder.guardar(
                        RetoHolder.Datos(
                            rivalId = entrada.id,
                            rivalNombre = entrada.nombre,
                            rivalFotoUrl = entrada.foto_url,
                            rivalCategoria = entrada.categoria_edad,
                            rivalElo = entrada.elo,
                        ),
                    )
                    navController.navigate(Screen.CrearReto.route)
                },
            )
        }
        composable(Screen.PerfilJugador.route) {
            val datos = remember { PerfilJugadorHolder.consumir() }
            if (datos == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                PerfilJugadorScreen(
                    datos = datos,
                    onVolver = { navController.popBackStack() },
                    onRetar = {
                        RetoHolder.guardar(
                            RetoHolder.Datos(
                                rivalId = datos.usuarioId,
                                rivalNombre = datos.nombre,
                                rivalFotoUrl = datos.fotoUrl,
                                rivalCategoria = datos.categoriaEdad,
                                rivalElo = datos.elo,
                            ),
                        )
                        navController.navigate(Screen.CrearReto.route)
                    },
                    onVerVersus = { versusId -> navController.navigate(Screen.DetalleVersus.ruta(versusId)) },
                )
            }
        }
        composable(Screen.CrearReto.route) {
            val rival = remember { RetoHolder.consumir() }
            if (rival == null) {
                LaunchedEffect(Unit) { navController.popBackStack() }
            } else {
                CrearRetoScreen(
                    rival = rival,
                    onRetoEnviado = { irATabDesdeOtroDestino(navController, ItemBarraInferior.RETOS) },
                    onVolver = { navController.popBackStack() },
                )
            }
        }
        composable(
            Screen.DetalleVersus.route,
            arguments = listOf(navArgument("versusId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val versusId = backStackEntry.arguments?.getInt("versusId") ?: 0
            DetalleVersusScreen(
                versusId = versusId,
                onVolver = { navController.popBackStack() },
                onReportarMarcador = { id ->
                    navController.navigate(Screen.ReportarMarcador.ruta(id))
                },
                onRegistrarCompromiso = { id ->
                    navController.navigate(Screen.RegistrarCompromiso.ruta(id))
                },
                onVerPerfil = { entrada ->
                    PerfilJugadorHolder.guardar(
                        PerfilJugadorHolder.Datos(
                            usuarioId = entrada.id,
                            nombre = entrada.nombre,
                            fotoUrl = entrada.foto_url,
                            club = entrada.club,
                            categoriaEdad = entrada.categoria_edad,
                            elo = entrada.elo,
                            modalidad = "individual",
                            posicionRanking = entrada.posicion,
                        ),
                    )
                    navController.navigate(Screen.PerfilJugador.route)
                },
            )
        }
        composable(
            Screen.ReportarMarcador.route,
            arguments = listOf(navArgument("versusId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val versusId = backStackEntry.arguments?.getInt("versusId") ?: 0
            ReportarMarcadorScreen(
                versusId = versusId,
                onVolver = { navController.popBackStack() },
                onVictoria = { eloAntes, eloDespues ->
                    navController.navigate(Screen.VictoriaPartido.ruta(versusId, eloAntes, eloDespues)) {
                        popUpTo(Screen.ReportarMarcador.route) { inclusive = true }
                    }
                },
            )
        }
        composable(Screen.HistorialVersus.route) {
            HistorialVersusScreen(
                onVolver = { navController.popBackStack() },
                onVerDetalle = { versusId -> navController.navigate(Screen.DetalleVersus.ruta(versusId)) },
                onBuscarRivales = { navController.navigate(Screen.BuscarRivales.route) },
            )
        }
        composable(Screen.Ajustes.route) {
            AjustesScreen(
                onVolver = { navController.popBackStack() },
                onCuentaEliminada = { volverALogin(navController) },
            )
        }
        composable(
            Screen.RegistrarCompromiso.route,
            arguments = listOf(navArgument("versusId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val versusId = backStackEntry.arguments?.getInt("versusId") ?: 0
            RegistrarCompromisoScreen(
                versusId = versusId,
                onVolver = { navController.popBackStack() },
                onCompromisoRegistrado = { navController.popBackStack() },
            )
        }
        composable(
            Screen.InvitacionCompromiso.route,
            arguments = listOf(navArgument("compromisoId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val compromisoId = backStackEntry.arguments?.getInt("compromisoId") ?: 0
            InvitacionCompromisoScreen(
                compromisoId = compromisoId,
                onCerrar = { navController.popBackStack() },
                onRespondido = { navController.popBackStack() },
            )
        }
        composable(
            Screen.AdjuntarComprobante.route,
            arguments = listOf(navArgument("compromisoId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val compromisoId = backStackEntry.arguments?.getInt("compromisoId") ?: 0
            AdjuntarComprobanteScreen(
                compromisoId = compromisoId,
                onVolver = { navController.popBackStack() },
                onEnviado = { navController.popBackStack() },
            )
        }
        composable(
            Screen.VerificarComprobante.route,
            arguments = listOf(navArgument("compromisoId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val compromisoId = backStackEntry.arguments?.getInt("compromisoId") ?: 0
            VerificarComprobanteScreen(
                compromisoId = compromisoId,
                onCerrar = { navController.popBackStack() },
                onVerificado = { navController.popBackStack() },
                onVictoria = { id ->
                    navController.navigate(Screen.VictoriaApuesta.ruta(id)) {
                        popUpTo(Screen.VerificarComprobante.route) { inclusive = true }
                    }
                },
            )
        }
        composable(
            Screen.DetalleCompromiso.route,
            arguments = listOf(navArgument("compromisoId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val compromisoId = backStackEntry.arguments?.getInt("compromisoId") ?: 0
            DetalleCompromisoScreen(
                compromisoId = compromisoId,
                onCerrar = { navController.popBackStack() },
                onVerVersus = { versusId -> navController.navigate(Screen.DetalleVersus.ruta(versusId)) },
            )
        }
        composable(Screen.ReporteApuestas.route) {
            ReporteApuestasScreen(onVolver = { navController.popBackStack() })
        }
        composable(Screen.BuscarVersus.route) {
            BuscarVersusScreen(
                onVolver = { navController.popBackStack() },
                onVerDetalle = { versusId -> navController.navigate(Screen.DetalleVersus.ruta(versusId)) },
                onRegistrarCompromiso = { versusId -> navController.navigate(Screen.RegistrarCompromiso.ruta(versusId)) },
            )
        }
        composable(
            Screen.DueloReto.route,
            arguments = listOf(navArgument("versusId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val versusId = backStackEntry.arguments?.getInt("versusId") ?: 0
            DueloRetoScreen(
                versusId = versusId,
                onVerReto = { id ->
                    // Primero se vuelve al shell de pestañas (en Inicio) y
                    // recién ahí se empuja Detalle encima — así "volver" desde
                    // el detalle cae en Inicio, no de nuevo en la animación
                    // que ya se vio.
                    irATabDesdeOtroDestino(navController, ItemBarraInferior.INICIO)
                    navController.navigate(Screen.DetalleVersus.ruta(id))
                },
                onContinuar = {
                    irATabDesdeOtroDestino(navController, ItemBarraInferior.INICIO)
                },
            )
        }
        composable(
            Screen.VictoriaPartido.route,
            arguments = listOf(
                navArgument("versusId") { type = NavType.IntType },
                navArgument("eloAntes") { type = NavType.IntType },
                navArgument("eloDespues") { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val versusId = backStackEntry.arguments?.getInt("versusId") ?: 0
            val eloAntes = backStackEntry.arguments?.getInt("eloAntes") ?: 0
            val eloDespues = backStackEntry.arguments?.getInt("eloDespues") ?: 0
            VictoriaPartidoScreen(
                versusId = versusId,
                eloAntes = eloAntes,
                eloDespues = eloDespues,
                onVerDetalle = { navController.popBackStack() },
                onIrAHome = { irATabDesdeOtroDestino(navController, ItemBarraInferior.INICIO) },
            )
        }
        composable(
            Screen.SubidaRanking.route,
            arguments = listOf(
                navArgument("posicionAnterior") { type = NavType.IntType },
                navArgument("posicionNueva") { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val posicionAnterior = backStackEntry.arguments?.getInt("posicionAnterior") ?: 0
            val posicionNueva = backStackEntry.arguments?.getInt("posicionNueva") ?: 0
            SubidaRankingScreen(
                posicionAnterior = posicionAnterior,
                posicionNueva = posicionNueva,
                onVerRanking = { irATabDesdeOtroDestino(navController, ItemBarraInferior.RANKING) },
                onIrAHome = { navController.popBackStack() },
            )
        }
        composable(
            Screen.VictoriaApuesta.route,
            arguments = listOf(navArgument("compromisoId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val compromisoId = backStackEntry.arguments?.getInt("compromisoId") ?: 0
            VictoriaApuestaScreen(
                compromisoId = compromisoId,
                onVerDetalle = {
                    navController.navigate(Screen.DetalleCompromiso.ruta(compromisoId)) {
                        popUpTo(Screen.VictoriaApuesta.route) { inclusive = true }
                    }
                },
                onIrAHome = { irATabDesdeOtroDestino(navController, ItemBarraInferior.INICIO) },
            )
        }
    }
}

/** Rutea cada notificación a la pantalla que le corresponde según su tipo —
 * las de versus llevan a su Detalle, las de compromiso a la pantalla puntual
 * del paso en el que quedó (invitación, verificar comprobante, etc.). Recibe
 * primitivos (no el `NotificacionDto` completo) para poder reusarse también
 * desde el tap a una notificación push, que solo trae tipo/versus_id/
 * compromiso_id como extras de un `Intent` — ver `PushNotificacionHolder`. */
private fun abrirNotificacion(navController: NavHostController, tipo: TipoNotificacion, versusId: Int?, compromisoId: Int?) {
    when (tipo) {
        TipoNotificacion.RETO_RECIBIDO,
        TipoNotificacion.RETO_ACEPTADO,
        TipoNotificacion.RETO_RECHAZADO,
        TipoNotificacion.MARCADOR_PENDIENTE,
        TipoNotificacion.RESULTADO_CONFIRMADO,
        TipoNotificacion.RESULTADO_DISPUTA,
        -> versusId?.let { navController.navigate(Screen.DetalleVersus.ruta(it)) }

        TipoNotificacion.COMPROMISO_RECIBIDO,
        TipoNotificacion.COMPROMISO_ACEPTADO,
        TipoNotificacion.COMPROMISO_RECHAZADO,
        -> compromisoId?.let { navController.navigate(Screen.InvitacionCompromiso.ruta(it)) }

        TipoNotificacion.COMPROMISO_COMPROBANTE_SUBIDO ->
            compromisoId?.let { navController.navigate(Screen.VerificarComprobante.ruta(it)) }

        TipoNotificacion.COMPROMISO_DISPUTA ->
            compromisoId?.let { navController.navigate(Screen.AdjuntarComprobante.ruta(it)) }

        // Saldado ya es un estado terminal — llevar a la pestaña genérica de
        // Apuestas dejaba al usuario a tener que buscar la tarjeta él mismo
        // entre SALDADOS; ahora que existe el detalle, se navega directo ahí.
        TipoNotificacion.COMPROMISO_SALDADO ->
            compromisoId?.let { navController.navigate(Screen.DetalleCompromiso.ruta(it)) }
                ?: irATabDesdeOtroDestino(navController, ItemBarraInferior.APUESTAS)
    }
}

private suspend fun irAHomeLimpiandoBackStack(navController: NavHostController) {
    navController.navigate(destinoTrasIniciarSesion()) {
        popUpTo(0)
    }
}

/** Home, salvo que haya un reto recibido sin ver todavía — en ese caso la
 * animación de duelo se interpone antes, una sola vez por reto (ver
 * `verificarDueloPendiente`). */
private suspend fun destinoTrasIniciarSesion(): String {
    val dueloVersusId = verificarDueloPendiente()
    return if (dueloVersusId != null) Screen.DueloReto.ruta(dueloVersusId) else Screen.Tabs.route
}

/** Busca el `reto_recibido` no leído más reciente que todavía no se mostró
 * como animación en este dispositivo, y lo marca como mostrado antes de
 * devolverlo — así no se repite en la próxima apertura de la app aunque
 * `leida` siga en false hasta que el usuario abra Notificaciones. */
private suspend fun verificarDueloPendiente(): Int? {
    val resultado = safeApiCall { RetrofitClient.notificacionesService.listarMisNotificaciones() }
    val notificaciones = (resultado as? ApiResult.Exito)?.datos.orEmpty()
    val pendiente = notificaciones
        .filter { it.tipo == TipoNotificacion.RETO_RECIBIDO && !it.leida && it.versus_id != null }
        .filter { !RetrofitClient.duelosMostradosStore.yaMostrado(it.id) }
        .maxByOrNull { it.created_at }
        ?: return null
    RetrofitClient.duelosMostradosStore.marcarMostrado(pendiente.id)
    return pendiente.versus_id
}

private fun volverALogin(navController: NavHostController) {
    navController.navigate(Screen.Login.route) {
        popUpTo(0)
    }
}

/** Salta a una pestaña del shell desde una pantalla empujada ENCIMA de él —
 * a diferencia del cambio de pestaña ya adentro del shell (que solo mueve el
 * Pager, sin tocar el NavHost), esto primero vuelve a la MISMA entrada "tabs"
 * del back stack — nunca se destruye, así que conserva ViewModels y la
 * página en la que ya estaba — y recién ahí dispara el salto puntual vía su
 * SavedStateHandle (ver CLAVE_TABS_PAGINA_OBJETIVO en TabsShellScreen.kt). */
private fun irATabDesdeOtroDestino(navController: NavHostController, item: ItemBarraInferior) {
    navController.getBackStackEntry(Screen.Tabs.route).savedStateHandle[CLAVE_TABS_PAGINA_OBJETIVO] = item.name
    navController.popBackStack(Screen.Tabs.route, inclusive = false)
}
