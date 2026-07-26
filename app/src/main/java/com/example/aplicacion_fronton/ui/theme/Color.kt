package com.example.aplicacion_fronton.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta clara — token por token igual a lo que se diseñó y revisó en Stitch
// (login, registro, home), con primary/tertiary corregidos a los hex de marca
// reales (#146259 / #AB472B) en vez de los que Stitch generó mal (#004941 / #782208),
// tal como se corrigió en el backend.

val LightPrimary = Color(0xFF146259)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFF146259)
val LightOnPrimaryContainer = Color(0xFF95DBCF)

val LightSecondary = Color(0xFF7E5700)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFFEC564)
val LightOnSecondaryContainer = Color(0xFF765100)

val LightTertiary = Color(0xFFAB472B)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFAB472B)
val LightOnTertiaryContainer = Color(0xFFFFFFFF)

val LightBackground = Color(0xFFFDF9EE)
val LightOnBackground = Color(0xFF1C1C15)
val LightSurface = Color(0xFFFDF9EE)
val LightOnSurface = Color(0xFF1C1C15)
val LightSurfaceVariant = Color(0xFFE6E2D7)
val LightOnSurfaceVariant = Color(0xFF3F4947)

val LightOutline = Color(0xFF6F7977)
val LightOutlineVariant = Color(0xFFBEC9C6)

val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF8F3E8)
val LightSurfaceContainer = Color(0xFFF2EEE2)
val LightSurfaceContainerHigh = Color(0xFFECE8DD)
val LightSurfaceContainerHighest = Color(0xFFE6E2D7)
val LightSurfaceBright = Color(0xFFFDF9EE)
val LightSurfaceDim = Color(0xFFDEDACF)

val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF93000A)

val LightInverseSurface = Color(0xFF323129)
val LightInverseOnSurface = Color(0xFFF5F0E5)
val LightInversePrimary = Color(0xFF8ED3C8)

// Oscuro — sin diseño propio en Stitch (todas las pantallas se diseñaron en claro);
// se mantiene un esquema oscuro coherente con la misma identidad de marca.
val DarkPrimary = Color(0xFF4BBCAC)
val DarkTertiary = Color(0xFFE08360)
val DarkGold = Color(0xFFDAB254)
val DarkBackground = Color(0xFF0F1E1B)
val DarkSurface = Color(0xFF142925)
