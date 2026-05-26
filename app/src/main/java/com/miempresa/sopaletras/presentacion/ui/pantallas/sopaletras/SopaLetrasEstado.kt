package com.miempresa.sopaletras.presentacion.ui.pantallas.sopaletras

import com.miempresa.sopaletras.dominio.modelos.Celda
import com.miempresa.sopaletras.dominio.modelos.Palabra
import com.miempresa.sopaletras.dominio.modelos.SopaLetras

/**
 * Representa el estado completo e inmutable de la pantalla de la sopa de letras.
 *
 * Patrón ViewState: toda la UI se reconstruye de manera predecible a partir
 * de este único objeto. Cualquier cambio de UI implica emitir una nueva copia.
 */
data class SopaLetrasEstado(
    val estaCargando: Boolean = false,
    val sopaLetras: SopaLetras? = null,
    val celdasSeleccionadas: List<Celda> = emptyList(),
    val palabrasEncontradas: List<Palabra> = emptyList(),
    val mensajeError: String? = null,
    val juegoCompletado: Boolean = false,
    val juegoRendido: Boolean = false,
    val segundosTranscurridos: Int = 0,
    val errores: Int = 0,
    val pistasUsadas: Int = 0
)
