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
import androidx.compose.material.icons.filled.Person
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
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import kotlinx.coroutines.launch

private data class PaginaOnboarding(val icono: ImageVector, val titulo: String, val descripcion: String)

// Ordenadas como pasos reales (no una lista de features sueltas): lo primero
// que hace falta hacer, en el orden en que un jugador nuevo lo va a necesitar
// — completar perfil primero (si no, ranking/apuestas lo ubican mal), después
// retar, después reportar el resultado, y recién ahí lo social/las apuestas.
private val PAGINAS = listOf(
    PaginaOnboarding(
        Icons.Filled.Person,
        "Completá tu perfil",
        "Categoría de edad, mano hábil y club (o \"jugador libre\") — así el ranking y las apuestas te ubican bien desde el primer partido.",
    ),
    PaginaOnboarding(
        Icons.Filled.SportsTennis,
        "Buscá un rival y retalo",
        "Elegí a alguien de tu nivel en \"Buscar rivales\", mandale un reto y coordinen cancha y horario — el otro tiene que aceptarlo primero.",
    ),
    PaginaOnboarding(
        Icons.Filled.EmojiEvents,
        "Jugá y reportá el marcador",
        "Cuando termina el partido, cargá el resultado desde el detalle del reto. Tu Elo y tu división (Hierro a Platino) se actualizan solos.",
    ),
    PaginaOnboarding(
        Icons.Filled.Forum,
        "Sumate a la comunidad",
        "Subí fotos de tus partidos, dale like y comentá las publicaciones de otros jugadores en la pestaña Comunidad.",
    ),
    PaginaOnboarding(
        Icons.Filled.MonetizationOn,
        "Apostá con confianza",
        "Registrá un compromiso informal con tu rival antes de jugar — comprobante, visto bueno de ambos y todo queda con historial.",
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
            PaginaOnboardingContenido(numero = pagina + 1, total = PAGINAS.size, pagina = PAGINAS[pagina])
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
private fun PaginaOnboardingContenido(numero: Int, total: Int, pagina: PaginaOnboarding) {
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
            "PASO $numero DE $total",
            style = CapsLabelTextStyle,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(
            pagina.titulo,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp),
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
