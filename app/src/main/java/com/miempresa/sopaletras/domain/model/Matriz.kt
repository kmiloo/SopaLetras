package com.miempresa.sopaletras.domain.model

/**
 * Representa la cuadrícula completa de letras de la sopa.
 * La matriz es inmutable; cualquier modificación debe generar una nueva instancia
 * para mantener el flujo unidireccional de datos.
 *
 * @property celdas Lista bidimensional de celdas (filas x columnas).
 * @property filas Número total de filas.
 * @property columnas Número total de columnas.
 */
data class Matriz(
    val celdas: List<List<Celda>>,
    val filas: Int,
    val columnas: Int
) {
    /**
     * Obtiene una celda de forma segura validando los límites de la matriz.
     */
    fun obtenerCelda(fila: Int, columna: Int): Celda? {
        if (fila !in 0 until filas) return null
        if (columna !in 0 until columnas) return null
        return celdas[fila][columna]
    }
}
