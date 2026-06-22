package com.miempresa.sopaletras.presentation.viewmodel

import com.miempresa.sopaletras.domain.model.Celda
import com.miempresa.sopaletras.domain.model.Palabra
import com.miempresa.sopaletras.domain.model.PuntajeRanking
import com.miempresa.sopaletras.domain.model.SopaLetras
import com.miempresa.sopaletras.domain.model.UsuarioSesion

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
    val pistasUsadas: Int = 0,
    val ultimaSeleccionInvalida: List<Celda> = emptyList(),
    val eventoSeleccionInvalida: Int = 0,
    val usuarioSesion: UsuarioSesion? = null,
    val authCargando: Boolean = false,
    val authMensaje: String? = null,
    val guardadoPuntajeMensaje: String? = null,
    val top10: List<PuntajeRanking> = emptyList()
)
