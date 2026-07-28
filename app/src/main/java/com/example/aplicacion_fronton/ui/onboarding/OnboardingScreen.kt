package com.example.aplicacion_fronton.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import kotlinx.coroutines.launch

private data class PaginaOnboarding(val icono: ImageVector, val titulo: String, val descripcion: String)

private val PAGINAS = listOf(
    PaginaOnboarding(
        Icons.Filled.SportsTennis,
        "Retá a cualquier rival",
        "Buscá jugadores de tu categoría, mandá un reto y coordiná cancha y horario, todo desde la app.",
    ),
    PaginaOnboarding(
        Icons.Filled.EmojiEvents,
        "Subí en el ranking",
        "Cada partido que reportás mueve tu Elo. Seguí tu progreso y tu división, de Hierro a Platino.",
    ),
    PaginaOnboarding(
        Icons.Filled.Forum,
        "Sumate a la comunidad",
        "Compartí fotos de tus partidos y dale like y comentá las publicaciones de otros jugadores.",
    ),
    PaginaOnboarding(
        Icons.Filled.MonetizationOn,
        "Apostá con confianza",
        "Registrá compromisos informales con tus rivales — comprobante, visto bueno y todo con historial.",
    ),
)

/** Primera vez que se abre la app (ver [com.example.aplicacion_fronton.network.OnboardingStore]) —
 * 3-4 slides deslizables explicando lo básico antes de llegar a Login, para
 * que un jugador nuevo entienda qué puede hacer acá antes de crear cuenta. */
@Composable
fun OnboardingScreen(onTerminado: () -> Unit) {
    val estadoPager = rememberPagerState(pageCount = { PAGINAS.size })
    val scope = rememberCoroutineScope()
    val esUltima = estadoPager.currentPage == PAGINAS.lastIndex

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onTerminado) {
                Text("Saltar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        HorizontalPager(state = estadoPager, modifier = Modifier.weight(1f)) { pagina ->
            PaginaOnboardingContenido(PAGINAS[pagina])
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        ) {
            PAGINAS.indices.forEach { indice ->
                val activo = indice == estadoPager.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (activo) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (activo) MaterialTheme.colorScheme.tertiary
                            else MaterialTheme.colorScheme.outlineVariant,
                        ),
                )
            }
        }

        BotonTactil(
            texto = if (esUltima) "Empezar" else "Siguiente",
            onClick = {
                if (esUltima) {
                    onTerminado()
                } else {
                    scope.launch { estadoPager.animateScrollToPage(estadoPager.currentPage + 1) }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PaginaOnboardingContenido(pagina: PaginaOnboarding) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(PaddingValues(horizontal = 32.dp)),
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                pagina.icono,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                modifier = Modifier.size(48.dp),
            )
        }
        Text(
            pagina.titulo,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 32.dp),
        )
        Text(
            pagina.descripcion,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}
