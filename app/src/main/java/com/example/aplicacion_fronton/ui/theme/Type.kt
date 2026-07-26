package com.example.aplicacion_fronton.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Stitch diseñó con Oswald (titulares condensados), Hanken Grotesk (cuerpo) y
 * JetBrains Mono (números tipo marcador). No empaquetamos esas fuentes (evita
 * depender de Downloadable Fonts en runtime, que necesita red + Play Services) —
 * en su lugar usamos las fuentes de sistema de Android más parecidas:
 * "sans-serif-condensed" para los titulares y "monospace" para los números.
 * El look de "marcador de cancha" se mantiene aunque el trazo exacto no sea Oswald.
 */
val OswaldFamily = FontFamily(Font(DeviceFontFamilyName("sans-serif-condensed")))
val HankenFamily = FontFamily.Default
val MonoFamily = FontFamily.Monospace

// Estilo reutilizable para cualquier número tipo marcador (Elo, fichas, sets) —
// no hay slot dedicado a esto en Typography de Material3.
val NumericTextStyle = TextStyle(
    fontFamily = MonoFamily,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.05.sp,
)

// Estilo para etiquetas en mayúsculas con tracking amplio (ej. "TELÉFONO O CORREO").
val CapsLabelTextStyle = TextStyle(
    fontFamily = OswaldFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    letterSpacing = 1.2.sp,
)

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = OswaldFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.02).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = OswaldFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = OswaldFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = OswaldFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = HankenFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = HankenFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = HankenFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = OswaldFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = 0.05.sp,
    ),
    labelMedium = CapsLabelTextStyle,
    labelSmall = TextStyle(
        fontFamily = OswaldFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 0.05.sp,
    ),
)
