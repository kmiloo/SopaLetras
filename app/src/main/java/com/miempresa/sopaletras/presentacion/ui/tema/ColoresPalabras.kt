package com.miempresa.sopaletras.presentacion.ui.tema

import androidx.compose.ui.graphics.Color

/**
 * Paleta de colores usada para resaltar palabras encontradas en la cuadrícula.
 *
 * Cada palabra recibe un color diferente al ser encontrada para que el jugador
 * pueda distinguirlas visualmente, especialmente cuando dos palabras se cruzan
 * (comparten letras).
 *
 * Todos los colores son lo suficientemente saturados y oscuros para que el
 * texto en blanco mantenga un buen contraste.
 */
val coloresPalabrasEncontradas: List<Color> = listOf(
    Color(0xFF2E7D32), // Verde
    Color(0xFF1565C0), // Azul
    Color(0xFFE65100), // Naranja
    Color(0xFFC2185B), // Rosa
    Color(0xFF6A1B9A), // Púrpura
    Color(0xFF00838F), // Cyan oscuro
    Color(0xFFD84315), // Rojo terracota
    Color(0xFF4527A0), // Índigo
    Color(0xFFAD1457), // Magenta
    Color(0xFF00695C), // Teal
    Color(0xFF5D4037), // Marrón
    Color(0xFF455A64)  // Azul-gris
)

/**
 * Devuelve el color asociado a un índice de palabra, ciclando la paleta
 * cuando el índice supera la cantidad de colores disponibles.
 */
fun obtenerColorPalabra(indice: Int): Color {
    val tamano = coloresPalabrasEncontradas.size
    return coloresPalabrasEncontradas[((indice % tamano) + tamano) % tamano]
}
