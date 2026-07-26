package com.example.aplicacion_fronton.ui.compromisos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aplicacion_fronton.model.dto.CompromisoDto
import com.example.aplicacion_fronton.model.dto.EstadoCompromiso
import com.example.aplicacion_fronton.model.dto.esGanadorDelCompromiso
import com.example.aplicacion_fronton.network.ApiResult
import com.example.aplicacion_fronton.network.RetrofitClient
import com.example.aplicacion_fronton.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val mesesAbrevReporte = listOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")

private fun formatearFechaCortaReporte(iso: String): String = try {
    val partes = iso.substringBefore("T").split("-")
    "${partes[2]} ${mesesAbrevReporte[partes[1].toInt() - 1]}"
} catch (e: Exception) {
    iso
}

data class RachaApuestas(val cantidad: Int, val ganando: Boolean)

data class RivalFrecuenteUi(
    val otroId: Int,
    val nombre: String,
    val fotoUrl: String?,
    val totalApuestas: Int,
    /** (victorias, derrotas) sobre las apuestas saldadas y derivables contra
     * este rival — null si ninguna de las apuestas con él tiene un lado
     * ganador/perdedor claro todavía. */
    val resultado: Pair<Int, Int>?,
)

enum class EstadoApuestaUi { GANASTE, PERDISTE, SALDADO, PENDIENTE }

/** Una entrada de "ÚLTIMAS APUESTAS" — mezcla saldadas y pendientes (igual
 * que la plantilla real), mostrando la descripción real en vez de un número
 * de fichas. */
data class ApuestaUi(
    val descripcion: String,
    val otroNombre: String,
    val otroFotoUrl: String?,
    val fecha: String,
    val estado: EstadoApuestaUi,
)

sealed class ReporteApuestasState {
    data object Cargando : ReporteApuestasState()
    data class Exito(
        val hayDatos: Boolean,
        val saldadas: Int,
        val pendientes: Int,
        val victorias: Int,
        val derrotas: Int,
        val tasaVictoria: Int?,
        val racha: RachaApuestas?,
        /** Racha acumulada neta (+1 gané/-1 perdí, corrida) de las últimas
         * apuestas derivables, en orden cronológico — una por barra en
         * "Evolución de apuestas". Vacía si hay menos de 2 puntos. */
        val evolucion: List<Int>,
        val rivalesFrecuentes: List<RivalFrecuenteUi>,
        val ultimasApuestas: List<ApuestaUi>,
    ) : ReporteApuestasState()
    data class Error(val mensaje: String) : ReporteApuestasState()
}

class ReporteApuestasViewModel : ViewModel() {
    private val _estado = MutableStateFlow<ReporteApuestasState>(ReporteApuestasState.Cargando)
    val estado: StateFlow<ReporteApuestasState> = _estado.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _estado.value = ReporteApuestasState.Cargando

            val perfil = safeApiCall { RetrofitClient.authService.obtenerMiPerfil() }
            if (perfil is ApiResult.Error) {
                _estado.value = ReporteApuestasState.Error(perfil.mensaje)
                return@launch
            }
            val miId = (perfil as ApiResult.Exito).datos.id

            val compromisosResultado = safeApiCall { RetrofitClient.compromisosService.listarMisCompromisos(null) }
            if (compromisosResultado is ApiResult.Error) {
                _estado.value = ReporteApuestasState.Error(compromisosResultado.mensaje)
                return@launch
            }
            val lista = (compromisosResultado as ApiResult.Exito).datos

            val saldados = lista.filter { it.estado == EstadoCompromiso.SALDADO }
            // "Pendientes" = compromisos ya aceptados (o en disputa) que
            // todavía no se saldaron — mismo criterio que la pestaña "EN
            // CURSO" de HistorialCompromisosScreen.
            val pendientes = lista.filter { it.estado == EstadoCompromiso.ACEPTADO || it.estado == EstadoCompromiso.DISPUTA }

            if (saldados.isEmpty() && pendientes.isEmpty()) {
                _estado.value = ReporteApuestasState.Exito(
                    hayDatos = false,
                    saldadas = 0,
                    pendientes = 0,
                    victorias = 0,
                    derrotas = 0,
                    tasaVictoria = null,
                    racha = null,
                    evolucion = emptyList(),
                    rivalesFrecuentes = emptyList(),
                    ultimasApuestas = emptyList(),
                )
                return@launch
            }

            val versusResultado = safeApiCall { RetrofitClient.versusService.listarMisVersus(null) }
            val versusPorId = (versusResultado as? ApiResult.Exito)?.datos.orEmpty().associateBy { it.id }

            val ranking = safeApiCall { RetrofitClient.rankingService.obtenerRanking("individual", null) }
            val jugadores = (ranking as? ApiResult.Exito)?.datos.orEmpty()

            fun nombreDe(id: Int): String = jugadores.firstOrNull { it.id == id }?.nombre ?: "Jugador #$id"
            fun fotoDe(id: Int): String? = jugadores.firstOrNull { it.id == id }?.foto_url
            fun otroIdDe(c: CompromisoDto): Int = if (c.creador_id == miId) c.invitado_id else c.creador_id

            data class SaldadoDerivado(val c: CompromisoDto, val esGanador: Boolean?)

            val derivados = saldados.map { c -> SaldadoDerivado(c, esGanadorDelCompromiso(c, versusPorId[c.versus_id], miId)) }
            val conResultado = derivados.filter { it.esGanador != null }

            val tasaVictoria = if (conResultado.isEmpty()) null else (conResultado.count { it.esGanador == true } * 100 / conResultado.size)

            val cronoDesc = conResultado.sortedByDescending { it.c.updated_at }
            val racha = cronoDesc.firstOrNull()?.let { primero ->
                val ganando = primero.esGanador == true
                RachaApuestas(cronoDesc.takeWhile { it.esGanador == ganando }.size, ganando)
            }

            // Evolución: racha acumulada neta de las últimas 7 apuestas
            // derivables, en orden cronológico — un dato real (no inventado)
            // que muestra el momentum reciente, ya que sin fichas no hay
            // ningún "monto" que graficar.
            val cronoAsc = conResultado.sortedBy { it.c.updated_at }.takeLast(7)
            var acumulado = 0
            val evolucion = cronoAsc.map { d ->
                acumulado += if (d.esGanador == true) 1 else -1
                acumulado
            }

            val porRival = lista.groupBy(::otroIdDe)
            val rivalesFrecuentes = porRival.entries
                .sortedByDescending { it.value.size }
                .take(3)
                .map { (otroId, compromisosRival) ->
                    val derivadosRival = compromisosRival
                        .filter { it.estado == EstadoCompromiso.SALDADO }
                        .mapNotNull { c -> esGanadorDelCompromiso(c, versusPorId[c.versus_id], miId) }
                    val resultado = if (derivadosRival.isEmpty()) null else derivadosRival.count { it } to derivadosRival.count { !it }
                    RivalFrecuenteUi(otroId, nombreDe(otroId), fotoDe(otroId), compromisosRival.size, resultado)
                }

            // "Últimas apuestas" mezcla saldadas Y pendientes (igual que la
            // plantilla real) — antes solo mostraba saldadas.
            val ultimasSaldadas = derivados.map { d ->
                val otroId = otroIdDe(d.c)
                val estado = when (d.esGanador) {
                    true -> EstadoApuestaUi.GANASTE
                    false -> EstadoApuestaUi.PERDISTE
                    null -> EstadoApuestaUi.SALDADO
                }
                ApuestaUi(d.c.descripcion, nombreDe(otroId), fotoDe(otroId), formatearFechaCortaReporte(d.c.updated_at), estado) to d.c.updated_at
            }
            val ultimasPendientes = pendientes.map { c ->
                val otroId = otroIdDe(c)
                ApuestaUi(c.descripcion, nombreDe(otroId), fotoDe(otroId), formatearFechaCortaReporte(c.updated_at), EstadoApuestaUi.PENDIENTE) to c.updated_at
            }
            val ultimasApuestas = (ultimasSaldadas + ultimasPendientes)
                .sortedByDescending { it.second }
                .take(6)
                .map { it.first }

            _estado.value = ReporteApuestasState.Exito(
                hayDatos = true,
                saldadas = saldados.size,
                pendientes = pendientes.size,
                victorias = conResultado.count { it.esGanador == true },
                derrotas = conResultado.count { it.esGanador == false },
                tasaVictoria = tasaVictoria,
                racha = racha,
                evolucion = evolucion,
                rivalesFrecuentes = rivalesFrecuentes,
                ultimasApuestas = ultimasApuestas,
            )
        }
    }
}
