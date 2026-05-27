package com.miempresa.sopaletras.domain.model

/**
 * Direcciones admitidas para colocar palabras en la sopa de letras.
 *
 * @property deltaFila Incremento aplicado a la fila por cada letra.
 * @property deltaColumna Incremento aplicado a la columna por cada letra.
 */
enum class Direccion(val deltaFila: Int, val deltaColumna: Int) {
    /** De izquierda a derecha. */
    HORIZONTAL_DERECHA(deltaFila = 0, deltaColumna = 1),

    /** De arriba hacia abajo. */
    VERTICAL_ABAJO(deltaFila = 1, deltaColumna = 0),

    /** Diagonal de arriba-izquierda hacia abajo-derecha. */
    DIAGONAL_DESCENDENTE(deltaFila = 1, deltaColumna = 1),

    /** Diagonal de abajo-izquierda hacia arriba-derecha. */
    DIAGONAL_ASCENDENTE(deltaFila = -1, deltaColumna = 1)
}
