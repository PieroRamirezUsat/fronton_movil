package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aplicacion_fronton.network.PushBannerHolder
import kotlinx.coroutines.delay

/** Banner que aparece dentro de la propia app cuando llega un push con la app
 * en primer plano — antes de esto no había NINGUNA reacción visual hasta que
 * el usuario abría la pestaña Notificaciones. Se autodescarta a los pocos
 * segundos; tocarlo navega igual que el tap a la notificación del sistema.
 *
 * Se guarda el último dato no-nulo aparte del `StateFlow` en sí: si se
 * renderizara directo sobre el valor nulleable, al descartarse el contenido
 * desaparecería de golpe ANTES de que termine la animación de salida
 * (`AnimatedVisibility` sigue mostrando el último `content` que tuvo mientras
 * anima `exit`, pero ese contenido ya sería nulo). */
@Composable
fun BannerNotificacionFlotante(onAbrir: (PushBannerHolder.Datos) -> Unit, modifier: Modifier = Modifier) {
    val actual by PushBannerHolder.actual.collectAsStateWithLifecycle()
    var ultimoNoNulo by remember { mutableStateOf<PushBannerHolder.Datos?>(null) }

    LaunchedEffect(actual) {
        if (actual != null) {
            ultimoNoNulo = actual
            delay(4000)
            PushBannerHolder.descartar()
        }
    }

    AnimatedVisibility(
        visible = actual != null,
        enter = slideInVertically(tween(280)) { -it } + fadeIn(tween(280)),
        exit = slideOutVertically(tween(200)) { -it } + fadeOut(tween(200)),
        modifier = modifier,
    ) {
        val datos = ultimoNoNulo ?: return@AnimatedVisibility
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable {
                    PushBannerHolder.descartar()
                    onAbrir(datos)
                }
                .padding(16.dp),
        ) {
            Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(datos.titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                Text(datos.mensaje, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f), maxLines = 2)
            }
        }
    }
}
