package com.miempresa.sopaletras.domain.model

/**
 * Representa una coordenada (fila, columna) dentro de la matriz de la sopa de letras.
 * Se utiliza como valor inmutable para describir ubicaciones.
 */
data class Posicion(
    val fila: Int,
    val columna: Int
)
