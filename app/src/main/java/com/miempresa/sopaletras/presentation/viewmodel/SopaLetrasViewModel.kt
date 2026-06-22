package com.miempresa.sopaletras.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.miempresa.sopaletras.domain.usecase.ObtenerSopaLetrasUseCase
import com.miempresa.sopaletras.domain.usecase.ResultadoValidacion
import com.miempresa.sopaletras.domain.usecase.ValidarPalabraUseCase
import com.miempresa.sopaletras.domain.model.Celda
import com.miempresa.sopaletras.domain.model.Dificultad
import com.miempresa.sopaletras.domain.model.Posicion
import com.miempresa.sopaletras.domain.repository.SesionRepositorio
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
    private val validarPalabraUseCase: ValidarPalabraUseCase,
    private val sesionRepositorio: SesionRepositorio
) : ViewModel() {

    private val _estado = MutableStateFlow(SopaLetrasEstado())
    val estado: StateFlow<SopaLetrasEstado> = _estado.asStateFlow()
    private var temporizadorJob: Job? = null
    private var puntajeGuardadoParaPartida = false

    init {
        _estado.update { it.copy(usuarioSesion = sesionRepositorio.usuarioActual) }
        if (sesionRepositorio.haySesionActiva) cargarTop10()
    }

    fun registrar(username: String, email: String, password: String) {
        ejecutarAuth {
            sesionRepositorio.registrar(username.trim(), email.trim(), password)
        }
    }

    fun login(email: String, password: String) {
        ejecutarAuth {
            sesionRepositorio.login(email.trim(), password)
        }
    }

    fun cerrarSesion() {
        sesionRepositorio.cerrarSesion()
        _estado.update {
            it.copy(
                usuarioSesion = null,
                authMensaje = "Sesion cerrada. Puedes jugar sin guardar puntajes.",
                guardadoPuntajeMensaje = null,
                top10 = emptyList()
            )
        }
    }

    fun cargarTop10() {
        if (!sesionRepositorio.haySesionActiva) return

        viewModelScope.launch {
            sesionRepositorio.obtenerTop10()
                .onSuccess { ranking ->
                    _estado.update { it.copy(top10 = ranking, authMensaje = null) }
                }
                .onFailure { error ->
                    _estado.update { it.copy(authMensaje = error.message ?: "No se pudo cargar el ranking") }
                }
        }
    }

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
                            ultimaSeleccionInvalida = emptyList(),
                            eventoSeleccionInvalida = 0,
                            mensajeError = null,
                            guardadoPuntajeMensaje = null
                        )
                    }
                    puntajeGuardadoParaPartida = false
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
                _estado.update {
                    it.copy(
                        errores = it.errores + 1,
                        celdasSeleccionadas = emptyList(),
                        ultimaSeleccionInvalida = estadoActual.celdasSeleccionadas,
                        eventoSeleccionInvalida = it.eventoSeleccionInvalida + 1
                    )
                }
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
                juegoRendido = true,
                guardadoPuntajeMensaje = "Partida rendida: no se guarda puntaje."
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
                if (sopaActualizada.estaCompletada) {
                    temporizadorJob?.cancel()
                    guardarPuntajeSiCorresponde(estadoActual)
                }
            }
        }
    }

    private fun ejecutarAuth(
        operacion: suspend () -> Result<com.miempresa.sopaletras.domain.model.UsuarioSesion>
    ) {
        viewModelScope.launch {
            _estado.update { it.copy(authCargando = true, authMensaje = null) }

            operacion()
                .onSuccess { usuario ->
                    _estado.update {
                        it.copy(
                            usuarioSesion = usuario,
                            authCargando = false,
                            authMensaje = "Sesion iniciada como ${usuario.username}"
                        )
                    }
                    cargarTop10()
                }
                .onFailure { error ->
                    _estado.update {
                        it.copy(
                            authCargando = false,
                            authMensaje = error.message ?: "No se pudo iniciar sesion"
                        )
                    }
                }
        }
    }

    private fun guardarPuntajeSiCorresponde(estadoPartida: SopaLetrasEstado) {
        if (puntajeGuardadoParaPartida) return
        puntajeGuardadoParaPartida = true

        val sopa = estadoPartida.sopaLetras ?: return
        if (!sesionRepositorio.haySesionActiva) {
            _estado.update { it.copy(guardadoPuntajeMensaje = "Puntaje local. Inicia sesion para guardarlo.") }
            return
        }

        val score = calcularPuntaje(
            dificultad = sopa.dificultad,
            segundosTranscurridos = estadoPartida.segundosTranscurridos,
            errores = estadoPartida.errores,
            pistas = estadoPartida.pistasUsadas,
            palabras = sopa.palabras.size
        )

        viewModelScope.launch {
            sesionRepositorio.guardarPuntaje(
                score = score,
                durationSeconds = estadoPartida.segundosTranscurridos.coerceAtLeast(1)
            )
                .onSuccess {
                    _estado.update { it.copy(guardadoPuntajeMensaje = "Puntaje guardado: $score") }
                    cargarTop10()
                }
                .onFailure { error ->
                    _estado.update {
                        it.copy(guardadoPuntajeMensaje = error.message ?: "No se pudo guardar el puntaje")
                    }
                }
        }
    }

    private fun calcularPuntaje(
        dificultad: Dificultad,
        segundosTranscurridos: Int,
        errores: Int,
        pistas: Int,
        palabras: Int
    ): Int {
        val totalSeconds = dificultad.totalSeconds()
        val remainingSeconds = (totalSeconds - segundosTranscurridos).coerceAtLeast(0)
        val speedBonus = remainingSeconds * 8
        val base = palabras * 500 + totalSeconds
        val penalties = errores * 120 + pistas * 80
        return (base + speedBonus - penalties).coerceAtLeast(0)
    }

    private fun Dificultad.totalSeconds(): Int = when (this) {
        Dificultad.FACIL -> 90
        Dificultad.MEDIO -> 120
        Dificultad.DIFICIL -> 150
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
        private val validarPalabraUseCase: ValidarPalabraUseCase,
        private val sesionRepositorio: SesionRepositorio
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(SopaLetrasViewModel::class.java)) {
                "Clase de ViewModel desconocida: ${modelClass.name}"
            }
            return SopaLetrasViewModel(
                obtenerSopaLetrasUseCase = obtenerSopaLetrasUseCase,
                validarPalabraUseCase = validarPalabraUseCase,
                sesionRepositorio = sesionRepositorio
            ) as T
        }
    }
}
