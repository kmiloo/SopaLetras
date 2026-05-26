package com.miempresa.sopaletras.dominio.modelos

/**
 * Niveles de dificultad disponibles. Cada nivel define un tamaño de matriz
 * y la cantidad de palabras a buscar.
 */
enum class Dificultad(val filas: Int, val columnas: Int, val cantidadPalabras: Int) {
    FACIL(filas = 8, columnas = 8, cantidadPalabras = 5),
    MEDIO(filas = 10, columnas = 10, cantidadPalabras = 7),
    DIFICIL(filas = 12, columnas = 12, cantidadPalabras = 9)
}
