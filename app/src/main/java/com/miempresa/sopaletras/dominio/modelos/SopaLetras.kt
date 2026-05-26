package com.miempresa.sopaletras.dominio.modelos

/**
 * Entidad raíz del dominio. Representa una partida completa de sopa de letras,
 * agrupando la matriz, las palabras objetivo y el nivel de dificultad.
 */
data class SopaLetras(
    val matriz: Matriz,
    val palabras: List<Palabra>,
    val dificultad: Dificultad
) {
    /**
     * Indica si todas las palabras ya fueron encontradas (juego completado).
     */
    val estaCompletada: Boolean
        get() = palabras.all { it.estaEncontrada }
}
