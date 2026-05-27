package com.miempresa.sopaletras.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = NeonBlue,
    onPrimary = Color.White,
    secondary = NeonPink,
    onSecondary = Color.White,
    tertiary = SuccessGold,
    onTertiary = FocusTextPrimary,
    background = HighStimulusBackground,
    onBackground = FocusTextPrimary,
    surface = HighStimulusSurface,
    onSurface = FocusTextPrimary,
    surfaceVariant = HighStimulusSurfaceHigh,
    onSurfaceVariant = FocusTextSecondary,
    outline = HighStimulusStroke,
    outlineVariant = HighStimulusStroke,
    error = AlertRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFE3E8),
    onErrorContainer = Color(0xFF8A001B),
    tertiaryContainer = Color(0xFFFFF2B8),
    onTertiaryContainer = FocusTextPrimary
)

@Composable
fun TemaSopaLetras(
    temaOscuro: Boolean = false,
    colorDinamico: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = Tipografia,
        content = content
    )
}
