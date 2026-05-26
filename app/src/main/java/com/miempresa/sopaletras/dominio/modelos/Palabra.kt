package com.miempresa.sopaletras.dominio.modelos

/**
 * Representa una palabra que el jugador debe encontrar en la sopa de letras.
 *
 * @property texto Texto literal de la palabra (siempre en mayúsculas para comparaciones).
 * @property posiciones Coordenadas de cada letra de la palabra dentro de la matriz.
 * @property estaEncontrada Indica si la palabra ya fue encontrada por el jugador.
 * @property colorIndice Índice asignado para distinguir visualmente esta palabra de las demás.
 * Se usa para elegir un color de la paleta de palabras encontradas.
 */
data class Palabra(
    val texto: String,
    val posiciones: List<Posicion> = emptyList(),
    val estaEncontrada: Boolean = false,
    val colorIndice: Int = 0
)
