@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.aplicacion_fronton.ui.componentes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.aplicacion_fronton.ui.theme.CapsLabelTextStyle
import java.util.Calendar
import java.util.TimeZone

/** Los `DatePickerDialog`/`TimePickerDialog` clásicos de Android (`android.app.*`)
 * son vistas del sistema — usan el tema del dispositivo, no el `MaterialTheme`
 * de la app, así que se ven completamente ajenos a la paleta Frontón (de ahí
 * la queja de "esos calendarios y relojes" desentonando). Estos dos son 100%
 * Compose/Material3, heredan los colores ya definidos en `Theme.kt` (primary
 * teal, tertiary terracota, fondo crema) sin tener que reinventar un calendario
 * a mano. */

private object SoloFechasFuturas : SelectableDates {
    // M3 DatePicker trabaja siempre en milisegundos UTC de medianoche —
    // comparar contra "hoy" también en UTC evita que quede un día corrido
    // según la zona horaria del dispositivo.
    private val hoyUtcMedianoche: Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= hoyUtcMedianoche
}

/** `fechaInicial` en formato "yyyy-MM-dd" (o null) — devuelve lo mismo por
 * `onConfirmar`. */
@Composable
fun DialogoSelectorFecha(fechaInicial: String?, onConfirmar: (String) -> Unit, onCerrar: () -> Unit) {
    val millisIniciales = fechaInicial?.let {
        runCatching {
            val (y, m, d) = it.split("-").map(String::toInt)
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { set(y, m - 1, d, 0, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        }.getOrNull()
    }
    val estado = rememberDatePickerState(initialSelectedDateMillis = millisIniciales, selectableDates = SoloFechasFuturas)

    DatePickerDialog(
        onDismissRequest = onCerrar,
        confirmButton = {
            TextButton(
                onClick = {
                    estado.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = millis }
                        onConfirmar("%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)))
                    }
                },
                enabled = estado.selectedDateMillis != null,
            ) { Text("CONFIRMAR", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.primary) }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("CANCELAR", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        colors = DatePickerDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        DatePicker(
            state = estado,
            title = null,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.tertiary,
                todayContentColor = MaterialTheme.colorScheme.tertiary,
            ),
        )
    }
}

/** `horaInicial` en formato "HH:mm" (o null) — devuelve lo mismo por
 * `onConfirmar`. Sin `TimePickerDialog` listo en Material3 (a diferencia del
 * de fecha) — se arma con un `Dialog` + `Surface` propios, mismo lenguaje
 * visual (esquinas redondeadas, fondo crema) que el resto de diálogos de la
 * app. */
@Composable
fun DialogoSelectorHora(horaInicial: String?, onConfirmar: (String) -> Unit, onCerrar: () -> Unit) {
    val (horaInicialInt, minutoInicialInt) = horaInicial?.split(":")?.map(String::toInt)
        ?: Calendar.getInstance().let { listOf(it.get(Calendar.HOUR_OF_DAY), it.get(Calendar.MINUTE)) }
    val estado = rememberTimePickerState(initialHour = horaInicialInt, initialMinute = minutoInicialInt, is24Hour = true)

    Dialog(onDismissRequest = onCerrar) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text(
                    "SELECCIONAR HORA",
                    style = CapsLabelTextStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                )
                TimePicker(
                    state = estado,
                    colors = TimePickerDefaults.colors(
                        selectorColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
                androidx.compose.foundation.layout.Row(
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    TextButton(onClick = onCerrar) { Text("CANCELAR", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    TextButton(onClick = { onConfirmar("%02d:%02d".format(estado.hour, estado.minute)) }) {
                        Text("CONFIRMAR", style = CapsLabelTextStyle, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
