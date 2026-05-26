package com.miempresa.sopaletras.presentacion.ui.pantallas.sopaletras

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.miempresa.sopaletras.dominio.casosdeuso.ObtenerSopaLetrasUseCase
import com.miempresa.sopaletras.dominio.casosdeuso.ResultadoValidacion
import com.miempresa.sopaletras.dominio.casosdeuso.ValidarPalabraUseCase
import com.miempresa.sopaletras.dominio.modelos.Celda
import com.miempresa.sopaletras.dominio.modelos.Dificultad
import com.miempresa.sopaletras.dominio.modelos.Posicion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de coordinar la pantalla de Sopa de Letras.
 *
 * Recibe los casos de uso por inyección (Principio de Inversión de Dependencias).
 * Expone un único [StateFlow] inmutable hacia la UI siguiendo el patrón ViewState.
 */
class SopaLetrasViewModel(
    private val obtenerSopaLetrasUseCase: ObtenerSopaLetrasUseCase,
    private val validarPalabraUseCase: ValidarPalabraUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(SopaLetrasEstado())
    val estado: StateFlow<SopaLetrasEstado> = _estado.asStateFlow()
    private var temporizadorJob: Job? = null

    /**
     * Solicita una nueva sopa de letras según la dificultad indicada.
     * La obtención puede tardar al consultar el API remoto.
     */
    fun cargarSopaLetras(dificultad: Dificultad = Dificultad.FACIL) {
        viewModelScope.launch {
            _estado.update { it.copy(estaCargando = true, mensajeError = null) }

            obtenerSopaLetrasUseCase(dificultad)
                .onSuccess { nuevaSopa ->
                    _estado.update {
                        it.copy(
                            estaCargando = false,
                            sopaLetras = nuevaSopa,
                            celdasSeleccionadas = emptyList(),
                            palabrasEncontradas = emptyList(),
                            juegoCompletado = false,
                            juegoRendido = false,
                            segundosTranscurridos = 0,
                            errores = 0,
                            pistasUsadas = 0,
                            mensajeError = null
                        )
                    }
                    iniciarTemporizador()
                }
                .onFailure { error ->
                    _estado.update {
                        it.copy(
                            estaCargando = false,
                            mensajeError = error.message ?: "Error desconocido"
                        )
                    }
                }
        }
    }

    /**
     * Añade o quita una celda de la selección actual.
     * Se ignora la pulsación si la celda ya pertenece a una palabra encontrada.
     */
    fun alternarSeleccionCelda(celda: Celda) {
        if (celda.perteneceAPalabraEncontrada) return
        _estado.update { estadoActual ->
            val seleccionActualizada = if (estadoActual.celdasSeleccionadas.contains(celda)) {
                estadoActual.celdasSeleccionadas - celda
            } else {
                estadoActual.celdasSeleccionadas + celda
            }
            estadoActual.copy(celdasSeleccionadas = seleccionActualizada)
        }
    }

    fun reemplazarSeleccion(celdas: List<Celda>) {
        val seleccionValida = celdas
            .distinctBy { Posicion(it.posicion.fila, it.posicion.columna) }

        _estado.update { it.copy(celdasSeleccionadas = seleccionValida) }
    }

    fun confirmarSeleccionArrastrada() {
        confirmarSeleccion()
    }

    /**
     * Valida la selección actual del usuario contra las palabras objetivo.
     */
    fun confirmarSeleccion() {
        val estadoActual = _estado.value
        val sopa = estadoActual.sopaLetras ?: return

        val resultado = validarPalabraUseCase(
            celdasSeleccionadas = estadoActual.celdasSeleccionadas,
            palabrasObjetivo = sopa.palabras
        )

        when (resultado) {
            is ResultadoValidacion.PalabraValida -> manejarPalabraValida(resultado)
            ResultadoValidacion.PalabraInvalida -> {
                _estado.update { it.copy(errores = it.errores + 1) }
                limpiarSeleccion()
            }
        }
    }

    fun limpiarSeleccion() {
        _estado.update { it.copy(celdasSeleccionadas = emptyList()) }
    }

    fun usarPista() {
        val estadoActual = _estado.value
        val sopa = estadoActual.sopaLetras ?: return
        if (estadoActual.juegoCompletado || estadoActual.juegoRendido) return

        val palabraPendiente = sopa.palabras.firstOrNull { !it.estaEncontrada } ?: return
        val celdasPista = palabraPendiente.posiciones.mapNotNull { posicion ->
            sopa.matriz.obtenerCelda(posicion.fila, posicion.columna)
        }

        _estado.update {
            it.copy(
                celdasSeleccionadas = celdasPista,
                pistasUsadas = it.pistasUsadas + 1
            )
        }
    }

    fun rendirse() {
        val estadoActual = _estado.value
        val sopa = estadoActual.sopaLetras ?: return
        temporizadorJob?.cancel()

        val palabrasReveladas = sopa.palabras.map { it.copy(estaEncontrada = true) }
        val colorPorPosicion = mutableMapOf<Posicion, Int>()
        palabrasReveladas.forEach { palabra ->
            palabra.posiciones.forEach { posicion ->
                colorPorPosicion.putIfAbsent(posicion, palabra.colorIndice)
            }
        }
        val celdasReveladas = sopa.matriz.celdas.map { fila ->
            fila.map { celda ->
                val color = colorPorPosicion[celda.posicion]
                if (color != null) {
                    celda.copy(perteneceAPalabraEncontrada = true, colorIndice = color)
                } else {
                    celda
                }
            }
        }

        val sopaRevelada = sopa.copy(
            palabras = palabrasReveladas,
            matriz = sopa.matriz.copy(celdas = celdasReveladas)
        )

        _estado.update {
            it.copy(
                sopaLetras = sopaRevelada,
                celdasSeleccionadas = emptyList(),
                palabrasEncontradas = palabrasReveladas,
                juegoCompletado = true,
                juegoRendido = true
            )
        }
    }

    // ----- Funciones privadas auxiliares -----

    /**
     * Cuando se valida una palabra:
     *  - Se marca la palabra como encontrada conservando su [colorIndice].
     *  - Se marcan las celdas correspondientes, asignándoles ese color.
     *  - Si una celda ya fue marcada por una palabra anterior (cruce), conserva
     *    el color original para no perder esa referencia visual.
     */
    private fun manejarPalabraValida(resultado: ResultadoValidacion.PalabraValida) {
        _estado.update { estadoActual ->
            val sopa = estadoActual.sopaLetras ?: return@update estadoActual

            val palabrasActualizadas = sopa.palabras.map { palabra ->
                if (palabra.texto == resultado.palabra.texto) {
                    palabra.copy(estaEncontrada = true)
                } else {
                    palabra
                }
            }

            val posicionesEncontradas = resultado.palabra.posiciones.toSet()
            val indiceColor = resultado.palabra.colorIndice
            val celdasActualizadas = sopa.matriz.celdas.map { fila ->
                fila.map { celda ->
                    if (celda.posicion in posicionesEncontradas &&
                        !celda.perteneceAPalabraEncontrada
                    ) {
                        celda.copy(
                            perteneceAPalabraEncontrada = true,
                            colorIndice = indiceColor
                        )
                    } else {
                        celda
                    }
                }
            }
            val matrizActualizada = sopa.matriz.copy(celdas = celdasActualizadas)
            val sopaActualizada = sopa.copy(
                palabras = palabrasActualizadas,
                matriz = matrizActualizada
            )

            estadoActual.copy(
                sopaLetras = sopaActualizada,
                celdasSeleccionadas = emptyList(),
                palabrasEncontradas = palabrasActualizadas.filter { it.estaEncontrada },
                juegoCompletado = sopaActualizada.estaCompletada
            ).also {
                if (sopaActualizada.estaCompletada) temporizadorJob?.cancel()
            }
        }
    }

    private fun iniciarTemporizador() {
        temporizadorJob?.cancel()
        temporizadorJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _estado.update { estadoActual ->
                    if (estadoActual.juegoCompletado || estadoActual.juegoRendido || estadoActual.estaCargando) {
                        estadoActual
                    } else {
                        estadoActual.copy(segundosTranscurridos = estadoActual.segundosTranscurridos + 1)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        temporizadorJob?.cancel()
        super.onCleared()
    }

    /**
     * Factoría para instanciar el ViewModel inyectando manualmente sus dependencias.
     * En entregas futuras se sustituirá por inyección con Hilt o Koin.
     */
    class Factoria(
        private val obtenerSopaLetrasUseCase: ObtenerSopaLetrasUseCase,
        private val validarPalabraUseCase: ValidarPalabraUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SopaLetrasViewModel::class.java)) {
                "Clase de ViewModel desconocida: ${modelClass.name}"
            }
            return SopaLetrasViewModel(
                obtenerSopaLetrasUseCase = obtenerSopaLetrasUseCase,
                validarPalabraUseCase = validarPalabraUseCase
            ) as T
        }
    }
}
