package com.miempresa.sopaletras.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val coloresPalabrasEncontradas: List<Color> = listOf(
    Color(0xFF00E676),
    Color(0xFF00E5FF),
    Color(0xFFFFD600),
    Color(0xFFFF2BD6),
    Color(0xFF7C4DFF),
    Color(0xFFFF6D00),
    Color(0xFF64FFDA),
    Color(0xFFFF4081),
    Color(0xFF40C4FF),
    Color(0xFFFFEA00),
    Color(0xFF69F0AE),
    Color(0xFFE040FB)
)

val selectionNeonBrush = Brush.linearGradient(
    colors = listOf(SelectionGradientStart, SelectionGradientMid, SelectionGradientEnd)
)

val dangerBrush = Brush.linearGradient(
    colors = listOf(AlertRed, AlertOrange)
)

val victoryGoldBrush = Brush.linearGradient(
    colors = listOf(VictoryGoldLight, VictoryGold, AlertOrange)
)

fun obtenerColorPalabra(indice: Int): Color {
    val tamano = coloresPalabrasEncontradas.size
    return coloresPalabrasEncontradas[((indice % tamano) + tamano) % tamano]
}
