package com.miempresa.sopaletras.domain.model

/**
 * Representa una celda individual dentro de la cuadrícula de la sopa de letras.
 *
 * @property posicion Coordenada de la celda en la matriz.
 * @property letra Carácter alfabético que se muestra en la celda.
 * @property estaSeleccionada Indica si el usuario está seleccionando esta celda actualmente.
 * @property perteneceAPalabraEncontrada Indica si esta celda forma parte de una palabra ya validada.
 * @property colorIndice Índice del color asignado cuando la celda fue encontrada como parte
 * de una palabra. Es `null` mientras no se haya encontrado. Si una celda es compartida por
 * varias palabras (cruce), conserva el color de la primera palabra encontrada.
 */
data class Celda(
    val posicion: Posicion,
    val letra: Char,
    val estaSeleccionada: Boolean = false,
    val perteneceAPalabraEncontrada: Boolean = false,
    val colorIndice: Int? = null
)
