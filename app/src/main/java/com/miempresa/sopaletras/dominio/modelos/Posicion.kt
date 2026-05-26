package com.miempresa.sopaletras.dominio.modelos

/**
 * Representa una coordenada (fila, columna) dentro de la matriz de la sopa de letras.
 * Se utiliza como valor inmutable para describir ubicaciones.
 */
data class Posicion(
    val fila: Int,
    val columna: Int
)
