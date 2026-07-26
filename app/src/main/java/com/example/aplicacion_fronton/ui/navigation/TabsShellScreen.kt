package com.example.aplicacion_fronton.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.example.aplicacion_fronton.model.dto.NotificacionDto
import com.example.aplicacion_fronton.model.dto.RankingEntryDto
import com.example.aplicacion_fronton.ui.compromisos.HistorialCompromisosScreen
import com.example.aplicacion_fronton.ui.home.HomeScreen
import com.example.aplicacion_fronton.ui.notificaciones.NotificacionesScreen
import com.example.aplicacion_fronton.ui.perfil.PerfilScreen
import com.example.aplicacion_fronton.ui.ranking.RankingScreen
import com.example.aplicacion_fronton.ui.retos.RetosScreen
import kotlinx.coroutines.launch

/** Clave del SavedStateHandle de la propia entrada "tabs" del back stack —
 * usada por pantallas empujadas ENCIMA del shell (crear reto, ganar un
 * partido/apuesta, notificación sin id, subida de ranking) para pedirle al
 * shell que aterrice en una pestaña puntual al volver. Ver
 * `irATabDesdeOtroDestino` en AppNavigation.kt. */
internal const val CLAVE_TABS_PAGINA_OBJETIVO = "tabs_pagina_objetivo"

/** Único Scaffold+BottomNavBar persistente para las 6 pestañas raíz — antes
 * cada una tenía su propia instancia y cambiar de pestaña era literalmente
 * navegar a otro destino del NavHost (sin gesto de swipe posible). Ahora es
 * un HorizontalPager de 6 páginas: tocar la barra anima el scroll del pager,
 * y deslizar funciona directo por ser, ni más ni menos, un scroll horizontal. */
@Composable
fun TabsShellScreen(
    backStackEntry: NavBackStackEntry,
    onVersusSeleccionado: (Int) -> Unit,
    onRetar: () -> Unit,
    onSubidaRanking: (posicionAnterior: Int, posicionNueva: Int) -> Unit,
    onJugadorSeleccionado: (RankingEntryDto, String) -> Unit,
    onVerDetalleReto: (Int) -> Unit,
    onSesionCerrada: () -> Unit,
    onVerHistorial: () -> Unit,
    onAjustes: () -> Unit,
    onAbrirNotificacion: (NotificacionDto) -> Unit,
    onBuscarRivalesNotif: () -> Unit,
    onVerInvitacion: (Int) -> Unit,
    onAdjuntar: (Int) -> Unit,
    onVerificar: (Int) -> Unit,
    onVerDetalleCompromiso: (Int) -> Unit,
    onVerReporte: () -> Unit,
    onBuscarRivalesApuestas: () -> Unit,
    onBuscarVersus: () -> Unit,
) {
    val paginas = ItemBarraInferior.entries
    val pagerState = rememberPagerState(initialPage = ItemBarraInferior.INICIO.ordinal) { paginas.size }
    val scope = rememberCoroutineScope()

    // Salto pedido desde una pantalla empujada ENCIMA del shell — se consume
    // una sola vez (se limpia después) para no re-disparar en la próxima
    // recomposición.
    val paginaObjetivo by backStackEntry.savedStateHandle
        .getStateFlow<String?>(CLAVE_TABS_PAGINA_OBJETIVO, null)
        .collectAsStateWithLifecycle()
    LaunchedEffect(paginaObjetivo) {
        val item = paginaObjetivo?.let { runCatching { ItemBarraInferior.valueOf(it) }.getOrNull() }
        if (item != null) {
            pagerState.scrollToPage(item.ordinal)
            backStackEntry.savedStateHandle[CLAVE_TABS_PAGINA_OBJETIVO] = null
        }
    }

    // Mismo comportamiento que el viejo stack [Home, pestañaActual]: alejarse
    // de Inicio y apretar atrás vuelve a Inicio antes de salir de la app.
    BackHandler(enabled = pagerState.currentPage != ItemBarraInferior.INICIO.ordinal) {
        scope.launch { pagerState.animateScrollToPage(ItemBarraInferior.INICIO.ordinal) }
    }

    val irATab: (ItemBarraInferior) -> Unit = { item ->
        scope.launch { pagerState.animateScrollToPage(item.ordinal) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(seleccionado = paginas[pagerState.currentPage], onSeleccionar = irATab, onRetar = onRetar)
        },
    ) { paddingInterno ->
        HorizontalPager(
            state = pagerState,
            // Default explícito a propósito: las páginas fuera de pantalla se
            // descomponen del todo (no solo se ocultan) — es lo que hace que
            // el refresco al volver a una pestaña siga funcionando igual que
            // antes con el back stack de dos niveles. No subir este número.
            beyondViewportPageCount = 0,
            modifier = Modifier.fillMaxSize(),
        ) { pagina ->
            when (paginas[pagina]) {
                ItemBarraInferior.INICIO -> HomeScreen(
                    onSeleccionarTab = irATab,
                    onVersusSeleccionado = onVersusSeleccionado,
                    onRetar = onRetar,
                    onSubidaRanking = onSubidaRanking,
                    paddingInterno = paddingInterno,
                )
                ItemBarraInferior.RANKING -> RankingScreen(
                    onSeleccionarTab = irATab,
                    onJugadorSeleccionado = onJugadorSeleccionado,
                    onRetar = onRetar,
                    paddingInterno = paddingInterno,
                )
                ItemBarraInferior.RETOS -> RetosScreen(
                    onSeleccionarTab = irATab,
                    onVerDetalle = onVerDetalleReto,
                    onRetar = onRetar,
                    paddingInterno = paddingInterno,
                )
                ItemBarraInferior.APUESTAS -> HistorialCompromisosScreen(
                    onSeleccionarTab = irATab,
                    onVerInvitacion = onVerInvitacion,
                    onAdjuntar = onAdjuntar,
                    onVerificar = onVerificar,
                    onVerDetalle = onVerDetalleCompromiso,
                    onVerReporte = onVerReporte,
                    onBuscarRivales = onBuscarRivalesApuestas,
                    onBuscarVersus = onBuscarVersus,
                    onRetar = onRetar,
                    paddingInterno = paddingInterno,
                )
                ItemBarraInferior.NOTIFICACIONES -> NotificacionesScreen(
                    onSeleccionarTab = irATab,
                    onAbrirNotificacion = onAbrirNotificacion,
                    onBuscarRivales = onBuscarRivalesNotif,
                    onRetar = onRetar,
                    paddingInterno = paddingInterno,
                )
                ItemBarraInferior.PERFIL -> PerfilScreen(
                    onSeleccionarTab = irATab,
                    onSesionCerrada = onSesionCerrada,
                    onVerHistorial = onVerHistorial,
                    onAjustes = onAjustes,
                    onRetar = onRetar,
                    paddingInterno = paddingInterno,
                )
            }
        }
    }
}
