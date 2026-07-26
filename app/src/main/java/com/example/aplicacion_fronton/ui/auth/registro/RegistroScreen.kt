package com.example.aplicacion_fronton.ui.auth.registro

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.aplicacion_fronton.model.dto.CategoriaEdad
import com.example.aplicacion_fronton.model.dto.ManoHabil
import com.example.aplicacion_fronton.model.dto.UsuarioDto
import com.example.aplicacion_fronton.network.GooglePendingAuth
import com.example.aplicacion_fronton.network.uriAParteMultipart
import com.example.aplicacion_fronton.ui.auth.login.camposSinCaja
import com.example.aplicacion_fronton.ui.componentes.BotonTactil
import com.example.aplicacion_fronton.ui.componentes.ConfettiOverlay
import com.example.aplicacion_fronton.ui.componentes.ResplandorRadial
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val nombresPaso = listOf("IDENTIFICACIÓN", "TU PERFIL", "TU CLUB", "DETALLES PRO")
private val etiquetasCategoria = mapOf(
    CategoriaEdad.MENORES to "Menores",
    CategoriaEdad.LIBRE to "Libre",
    CategoriaEdad.MAS_40 to "+40",
    CategoriaEdad.MAS_50 to "+50",
    CategoriaEdad.MAS_60 to "+60",
)

@Composable
fun RegistroScreen(
    onRegistroExitoso: (UsuarioDto) -> Unit,
    onVolverALogin: () -> Unit,
    viewModel: RegistroViewModel = viewModel(),
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // Si venimos de "Continuar con Google" en Login, ya tenemos correo/nombre/foto
    // verificados por el backend — se salta el paso de identificación (no hace
    // falta contraseña) y se arranca directo en Tu Perfil.
    val datosGoogle = remember { GooglePendingAuth.consumir() }

    val pagerState = rememberPagerState(
        initialPage = if (datosGoogle != null) 1 else 0,
        pageCount = { 4 },
    )
    val scope = rememberCoroutineScope()

    var correo by remember { mutableStateOf(datosGoogle?.correo ?: "") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf(datosGoogle?.nombre ?: "") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var jugadorLibre by remember { mutableStateOf(false) }
    var club by remember { mutableStateOf("") }
    var categoriaEdad by remember { mutableStateOf(CategoriaEdad.LIBRE) }
    var manoHabil by remember { mutableStateOf(ManoHabil.DIESTRO) }
    var errorPaso1 by remember { mutableStateOf<String?>(null) }

    val selectorFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) fotoUri = uri }

    // Antes esto saltaba a Home en silencio, sin ningún momento de celebración
    // pese a ser el primer logro real del usuario en la app — ahora se
    // muestra un check dibujado + confeti un instante antes de navegar.
    var mostrarExito by remember { mutableStateOf(false) }
    LaunchedEffect(estado) {
        val actual = estado
        if (actual is RegistroState.Exito) {
            mostrarExito = true
            delay(1400)
            onRegistroExitoso(actual.usuario)
        }
    }

    fun irAPaso(paso: Int) = scope.launch { pagerState.animateScrollToPage(paso) }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        // Header — un botón de salida claro (no un ícono de perfil que sugiera
        // sesión iniciada, cuando en este flujo todavía no existe la cuenta).
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            IconButton(onClick = onVolverALogin) {
                Icon(Icons.Filled.Close, contentDescription = "Salir del registro", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                "PALETA FRONTÓN",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.size(48.dp))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 2.dp)

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Paso ${pagerState.currentPage + 1} de 4",
                    style = CapsLabelTextStyle,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    nombresPaso[pagerState.currentPage],
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(8.dp))
            val progresoAnimado by animateFloatAsState(
                targetValue = (pagerState.currentPage + 1) / 4f,
                animationSpec = tween(350),
                label = "progresoRegistro",
            )
            LinearProgressIndicator(
                progress = { progresoAnimado },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50)),
            )
            Spacer(Modifier.height(32.dp))
        }

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { pagina ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                when (pagina) {
                    0 -> PasoIdentificacion(
                        correo = correo,
                        onCorreoChange = { correo = it },
                        password = password,
                        onPasswordChange = { password = it },
                        confirmarPassword = confirmarPassword,
                        onConfirmarPasswordChange = { confirmarPassword = it },
                        error = errorPaso1,
                        onContinuar = {
                            errorPaso1 = when {
                                correo.isBlank() -> "Ingresa tu correo electrónico."
                                password.length < 8 -> "La contraseña necesita al menos 8 caracteres."
                                confirmarPassword != password -> "Las contraseñas no coinciden."
                                else -> null
                            }
                            if (errorPaso1 == null) irAPaso(1)
                        },
                    )
                    1 -> PasoPerfil(
                        nombre = nombre,
                        onNombreChange = { nombre = it },
                        fotoUri = fotoUri,
                        fotoGoogleUrl = datosGoogle?.fotoUrl,
                        correoGoogle = datosGoogle?.correo,
                        onElegirFoto = {
                            selectorFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        onAtras = { if (datosGoogle == null) irAPaso(0) else onVolverALogin() },
                        onContinuar = { irAPaso(2) },
                    )
                    2 -> PasoClub(
                        club = club,
                        onClubChange = { club = it },
                        jugadorLibre = jugadorLibre,
                        onJugadorLibreChange = { jugadorLibre = it },
                        onAtras = { irAPaso(1) },
                        onContinuar = { irAPaso(3) },
                    )
                    3 -> PasoDetalles(
                        categoriaEdad = categoriaEdad,
                        onCategoriaChange = { categoriaEdad = it },
                        manoHabil = manoHabil,
                        onManoChange = { manoHabil = it },
                        cargando = estado is RegistroState.Cargando,
                        error = (estado as? RegistroState.Error)?.mensaje,
                        onAtras = { irAPaso(2) },
                        onCrearPerfil = {
                            if (datosGoogle != null) {
                                viewModel.registrarConGoogle(datosGoogle.idToken, jugadorLibre, club, categoriaEdad, manoHabil)
                            } else {
                                val fotoParte = fotoUri?.let { uriAParteMultipart(contexto, it) }
                                viewModel.registrar(nombre, correo, password, jugadorLibre, club, categoriaEdad, manoHabil, fotoParte)
                            }
                        },
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        Text(
            "Ya tengo cuenta, iniciar sesión",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clickable(onClick = onVolverALogin),
        )
    }

    AnimatedVisibility(
        visible = mostrarExito,
        enter = fadeIn(tween(250)),
        modifier = Modifier.fillMaxSize(),
    ) {
        PantallaExitoRegistro()
    }
    }
}

/** Check dibujado (stroke animado) + confeti — reemplaza el salto silencioso
 * a Home que había antes. */
@Composable
private fun PantallaExitoRegistro() {
    val progresoCheck = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progresoCheck.animateTo(1f, tween(500, easing = LinearOutSlowInEasing))
    }
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          // Resplandor detrás del check — antes este era el único momento de
          // éxito de la app sobre un fondo `primary` completamente liso, sin
          // nada del vocabulario de "resplandor" que ya existe en Splash/Duelo.
          Box(contentAlignment = Alignment.Center) {
            ResplandorRadial(color = Color.White, tamaño = 220.dp, alpha = 0.22f)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(56.dp)) {
                    val ancho = size.width
                    val alto = size.height
                    val inicio = Offset(ancho * 0.2f, alto * 0.52f)
                    val medio = Offset(ancho * 0.42f, alto * 0.72f)
                    val fin = Offset(ancho * 0.82f, alto * 0.28f)
                    val progreso = progresoCheck.value
                    val punto = if (progreso <= 0.4f) {
                        lerp(inicio, medio, (progreso / 0.4f).coerceIn(0f, 1f))
                    } else {
                        medio
                    }
                    drawLine(Color.White, inicio, punto, strokeWidth = 8f, cap = StrokeCap.Round)
                    if (progreso > 0.4f) {
                        val puntoFinal = lerp(medio, fin, ((progreso - 0.4f) / 0.6f).coerceIn(0f, 1f))
                        drawLine(Color.White, medio, puntoFinal, strokeWidth = 8f, cap = StrokeCap.Round)
                    }
                }
            }
          }
            Spacer(Modifier.height(24.dp))
            Text("¡CUENTA CREADA!", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Bienvenido a Paleta Frontón", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
        }
        ConfettiOverlay(disparar = true, modifier = Modifier.padding(top = 120.dp))
    }
}

@Composable
private fun PasoIdentificacion(
    correo: String,
    onCorreoChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmarPassword: String,
    onConfirmarPasswordChange: (String) -> Unit,
    error: String?,
    onContinuar: () -> Unit,
) {
    var mostrarPassword by remember { mutableStateOf(false) }
    var mostrarConfirmar by remember { mutableStateOf(false) }

    Text("COMENCEMOS", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
    Text(
        "Ingresa tu correo y crea tu contraseña para empezar tu carrera profesional en Frontón.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
    )
    TextField(
        value = correo,
        onValueChange = onCorreoChange,
        placeholder = { Text("Correo electrónico") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        colors = camposSinCaja(),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    TextField(
        value = password,
        onValueChange = onPasswordChange,
        placeholder = { Text("Contraseña (mínimo 8 caracteres)") },
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = { mostrarPassword = !mostrarPassword }) {
                Icon(if (mostrarPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
            }
        },
        visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
        colors = camposSinCaja(),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    TextField(
        value = confirmarPassword,
        onValueChange = onConfirmarPasswordChange,
        placeholder = { Text("Confirma tu contraseña") },
        singleLine = true,
        trailingIcon = {
            IconButton(onClick = { mostrarConfirmar = !mostrarConfirmar }) {
                Icon(if (mostrarConfirmar) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null)
            }
        },
        visualTransformation = if (mostrarConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
        colors = camposSinCaja(),
        modifier = Modifier.fillMaxWidth(),
    )
    AnimatedVisibility(visible = error != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
        Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
    }
    BotonTactil(
        texto = "Continuar",
        icono = Icons.AutoMirrored.Filled.ArrowForward,
        onClick = onContinuar,
        modifier = Modifier.padding(top = 24.dp),
    )
}

@Composable
private fun ColumnScope.PasoPerfil(
    nombre: String,
    onNombreChange: (String) -> Unit,
    fotoUri: Uri?,
    fotoGoogleUrl: String?,
    correoGoogle: String?,
    onElegirFoto: () -> Unit,
    onAtras: () -> Unit,
    onContinuar: () -> Unit,
) {
    Text("TU PERFIL", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
    Text(
        "Personaliza cómo te verán tus rivales en la cancha.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
    )

    if (correoGoogle != null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(correoGoogle, style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(16.dp))
    }

    Box(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(bottom = 24.dp)
            .size(112.dp),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable(onClick = onElegirFoto),
            contentAlignment = Alignment.Center,
        ) {
            val modeloFoto = fotoUri ?: fotoGoogleUrl
            if (modeloFoto != null) {
                AsyncImage(
                    model = modeloFoto,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize().clip(CircleShape),
                )
            } else {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(40.dp))
            }
        }
        // Badge tipo FAB — la única señal de que se puede tocar la foto, sin
        // texto debajo (el usuario lo pidió explícitamente: solo la imagen).
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary)
                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                .clickable(onClick = onElegirFoto),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.PhotoCamera,
                contentDescription = "Cambiar foto de perfil",
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }
    TextField(
        value = nombre,
        onValueChange = onNombreChange,
        placeholder = { Text("Nombre completo") },
        singleLine = true,
        colors = camposSinCaja(),
        modifier = Modifier.fillMaxWidth(),
    )
    FilaAtrasContinuar(onAtras = onAtras, onContinuar = onContinuar, modifier = Modifier.padding(top = 24.dp))
}

@Composable
private fun PasoClub(
    club: String,
    onClubChange: (String) -> Unit,
    jugadorLibre: Boolean,
    onJugadorLibreChange: (Boolean) -> Unit,
    onAtras: () -> Unit,
    onContinuar: () -> Unit,
) {
    Text("TU CLUB", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
    Text(
        "Representa a tu club o juega como independiente.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
    )
    Text(
        "BÚSQUEDA DE CLUB",
        style = CapsLabelTextStyle,
        color = if (jugadorLibre) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary,
    )
    TextField(
        value = club,
        onValueChange = onClubChange,
        enabled = !jugadorLibre,
        placeholder = { Text(if (jugadorLibre) "Jugando como libre..." else "Escribe el nombre de tu club (opcional)") },
        singleLine = true,
        colors = camposSinCaja(),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(24.dp))
    Text(
        "O TAMBIÉN",
        style = CapsLabelTextStyle,
        color = MaterialTheme.colorScheme.outlineVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    val colorActivo = MaterialTheme.colorScheme.primary
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                2.dp,
                if (jugadorLibre) colorActivo else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(16.dp),
            )
            .background(if (jugadorLibre) colorActivo.copy(alpha = 0.1f) else Color.Transparent)
            .clickable { onJugadorLibreChange(!jugadorLibre) }
            .padding(24.dp),
    ) {
        Icon(
            Icons.Filled.PersonPin,
            contentDescription = null,
            tint = if (jugadorLibre) colorActivo else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp),
        )
        Text(
            "Soy jugador libre",
            style = MaterialTheme.typography.headlineSmall,
            color = if (jugadorLibre) colorActivo else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
    FilaAtrasContinuar(onAtras = onAtras, onContinuar = onContinuar, modifier = Modifier.padding(top = 24.dp))
}

@Composable
private fun PasoDetalles(
    categoriaEdad: CategoriaEdad,
    onCategoriaChange: (CategoriaEdad) -> Unit,
    manoHabil: ManoHabil,
    onManoChange: (ManoHabil) -> Unit,
    cargando: Boolean,
    error: String?,
    onAtras: () -> Unit,
    onCrearPerfil: () -> Unit,
) {
    Text("DETALLES PRO", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
    Text(
        "Define tu categoría y estilo de juego.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
    )
    Text("CATEGORÍA DE EDAD", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(8.dp))
    FlowRowChips(categoriaEdad, onCategoriaChange)

    Spacer(Modifier.height(24.dp))
    Text("MANO DOMINANTE", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(8.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(4.dp),
    ) {
        listOf(ManoHabil.DIESTRO to "Diestro", ManoHabil.ZURDO to "Zurdo").forEach { (valor, etiqueta) ->
            val activo = manoHabil == valor
            Text(
                etiqueta,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                color = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (activo) MaterialTheme.colorScheme.surfaceBright else Color.Transparent)
                    .clickable { onManoChange(valor) }
                    .padding(vertical = 14.dp),
            )
        }
    }

    AnimatedVisibility(visible = error != null, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
        Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 16.dp))
    }

    BotonTactil(
        texto = "Crear mi perfil",
        icono = Icons.Filled.CheckCircle,
        onClick = onCrearPerfil,
        cargando = cargando,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp),
    )
    Text(
        "Volver al paso anterior",
        style = CapsLabelTextStyle,
        color = MaterialTheme.colorScheme.outline,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .clickable(onClick = onAtras),
    )
}

@Composable
private fun FlowRowChips(seleccionado: CategoriaEdad, onSeleccionar: (CategoriaEdad) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        etiquetasCategoria.entries.take(3).forEach { (valor, etiqueta) -> ChipCategoria(etiqueta, seleccionado == valor) { onSeleccionar(valor) } }
    }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        etiquetasCategoria.entries.drop(3).forEach { (valor, etiqueta) -> ChipCategoria(etiqueta, seleccionado == valor) { onSeleccionar(valor) } }
    }
}

@Composable
private fun ChipCategoria(etiqueta: String, activo: Boolean, onClick: () -> Unit) {
    Text(
        etiqueta,
        style = CapsLabelTextStyle,
        color = if (activo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (activo) MaterialTheme.colorScheme.primary else Color.Transparent)
            .border(
                2.dp,
                if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(50),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun FilaAtrasContinuar(onAtras: () -> Unit, onContinuar: () -> Unit, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onAtras,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).height(56.dp),
        ) { Text("ATRÁS", style = CapsLabelTextStyle) }
        BotonTactil(
            texto = "Continuar",
            icono = Icons.AutoMirrored.Filled.ArrowForward,
            onClick = onContinuar,
            modifier = Modifier.weight(1f),
        )
    }
}
