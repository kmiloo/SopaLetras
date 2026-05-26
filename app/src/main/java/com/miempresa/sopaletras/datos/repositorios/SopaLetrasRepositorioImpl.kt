package com.miempresa.sopaletras.datos.repositorios

import com.miempresa.sopaletras.datos.fuentes.local.FuentePalabrasLocal
import com.miempresa.sopaletras.datos.fuentes.remoto.FuentePalabrasRemota
import com.miempresa.sopaletras.dominio.modelos.Celda
import com.miempresa.sopaletras.dominio.modelos.Dificultad
import com.miempresa.sopaletras.dominio.modelos.Direccion
import com.miempresa.sopaletras.dominio.modelos.Matriz
import com.miempresa.sopaletras.dominio.modelos.Palabra
import com.miempresa.sopaletras.dominio.modelos.Posicion
import com.miempresa.sopaletras.dominio.modelos.SopaLetras
import com.miempresa.sopaletras.dominio.repositorios.SopaLetrasRepositorio
import kotlin.math.abs
import kotlin.random.Random

class SopaLetrasRepositorioImpl(
    private val fuenteRemota: FuentePalabrasRemota,
    private val fuenteLocal: FuentePalabrasLocal
) : SopaLetrasRepositorio {

    override suspend fun obtenerSopaLetras(dificultad: Dificultad): Result<SopaLetras> {
        return runCatching {
            val palabrasTexto = obtenerPalabrasParaJuego(dificultad)
            val (matriz, palabrasColocadas) = generarSopa(dificultad, palabrasTexto)
            SopaLetras(matriz = matriz, palabras = palabrasColocadas, dificultad = dificultad)
        }
    }

    override suspend fun guardarProgreso(sopaLetras: SopaLetras): Result<Unit> {
        return Result.success(Unit)
    }

    private suspend fun obtenerPalabrasParaJuego(dificultad: Dificultad): List<String> {
        val tamanoMaximo = minOf(dificultad.filas, dificultad.columnas)
        val palabrasDeApi = fuenteRemota
            .obtenerPalabras(cantidad = dificultad.cantidadPalabras * 4)
            .getOrDefault(emptyList())

        val candidatas = if (palabrasDeApi.size >= dificultad.cantidadPalabras) {
            palabrasDeApi
        } else {
            (palabrasDeApi + fuenteLocal.obtenerPalabras()).distinct()
        }

        return candidatas
            .filter { it.length in 3..tamanoMaximo }
            .distinct()
            .shuffled()
            .take(dificultad.cantidadPalabras)
    }

    private fun generarSopa(
        dificultad: Dificultad,
        textosPalabras: List<String>
    ): Pair<Matriz, List<Palabra>> {
        val filas = dificultad.filas
        val columnas = dificultad.columnas
        val matrizCaracteres = Array(filas) { CharArray(columnas) { CELDA_VACIA } }
        val palabrasColocadas = mutableListOf<Palabra>()

        for (texto in textosPalabras.sortedByDescending { it.length }) {
            val palabraSinColor = intentarColocarPalabra(
                texto = texto,
                matriz = matrizCaracteres,
                filas = filas,
                columnas = columnas,
                palabrasColocadas = palabrasColocadas
            )

            if (palabraSinColor != null) {
                palabrasColocadas.add(palabraSinColor.copy(colorIndice = palabrasColocadas.size))
            }
        }

        rellenarCeldasVacias(matrizCaracteres, filas, columnas)

        val celdas = List(filas) { fila ->
            List(columnas) { columna ->
                Celda(posicion = Posicion(fila, columna), letra = matrizCaracteres[fila][columna])
            }
        }

        return Matriz(celdas, filas, columnas) to palabrasColocadas
    }

    private fun intentarColocarPalabra(
        texto: String,
        matriz: Array<CharArray>,
        filas: Int,
        columnas: Int,
        palabrasColocadas: List<Palabra>
    ): Palabra? {
        var mejorCandidato: List<Posicion>? = null
        var mejorPuntaje = Int.MIN_VALUE

        repeat(MAX_INTENTOS_POR_PALABRA) {
            val candidato = generarCandidato(texto, filas, columnas)
            if (esColocacionValida(texto, candidato, matriz, filas, columnas, palabrasColocadas)) {
                val puntaje = puntuarCandidato(candidato, matriz, filas, columnas)
                if (puntaje > mejorPuntaje) {
                    mejorPuntaje = puntaje
                    mejorCandidato = candidato
                }
            }
        }

        return mejorCandidato?.let { candidato ->
            escribirEnMatriz(texto, candidato, matriz)
            Palabra(texto = texto, posiciones = candidato)
        }
    }

    private fun generarCandidato(
        texto: String,
        filas: Int,
        columnas: Int
    ): List<Posicion> {
        val direccion = Direccion.entries.random()
        val filaInicio = Random.nextInt(filas)
        val columnaInicio = Random.nextInt(columnas)

        return List(texto.length) { indice ->
            Posicion(
                fila = filaInicio + direccion.deltaFila * indice,
                columna = columnaInicio + direccion.deltaColumna * indice
            )
        }
    }

    private fun esColocacionValida(
        texto: String,
        posiciones: List<Posicion>,
        matriz: Array<CharArray>,
        filas: Int,
        columnas: Int,
        palabrasColocadas: List<Palabra>
    ): Boolean {
        for (indice in posiciones.indices) {
            val posicion = posiciones[indice]
            if (posicion.fila !in 0 until filas) return false
            if (posicion.columna !in 0 until columnas) return false

            val existente = matriz[posicion.fila][posicion.columna]
            if (existente != CELDA_VACIA && existente != texto[indice]) return false
        }

        val posicionesSet = posiciones.toSet()
        val estaEncimaDeOtra = palabrasColocadas.any { palabra ->
            val existentes = palabra.posiciones.toSet()
            posicionesSet == existentes ||
                existentes.containsAll(posicionesSet) ||
                posicionesSet.containsAll(existentes)
        }
        if (estaEncimaDeOtra) return false

        val cruces = contarCruces(posiciones, matriz)
        val maxCrucesPermitidos = maxOf(1, texto.length / 3)
        return cruces < texto.length && cruces <= maxCrucesPermitidos
    }

    private fun contarCruces(
        posiciones: List<Posicion>,
        matriz: Array<CharArray>
    ): Int {
        return posiciones.count { posicion ->
            posicion.fila in matriz.indices &&
                posicion.columna in matriz[posicion.fila].indices &&
                matriz[posicion.fila][posicion.columna] != CELDA_VACIA
        }
    }

    private fun puntuarCandidato(
        posiciones: List<Posicion>,
        matriz: Array<CharArray>,
        filas: Int,
        columnas: Int
    ): Int {
        val cruces = contarCruces(posiciones, matriz)
        val nuevas = posiciones.size - cruces
        val bordeMasCercano = posiciones.minOf { posicion ->
            minOf(
                posicion.fila,
                posicion.columna,
                filas - 1 - posicion.fila,
                columnas - 1 - posicion.columna
            )
        }
        val centroFila = filas / 2
        val centroColumna = columnas / 2
        val distanciaCentro = posiciones.sumOf { posicion ->
            abs(posicion.fila - centroFila) + abs(posicion.columna - centroColumna)
        } / posiciones.size

        return nuevas * 30 - cruces * 20 + bordeMasCercano * 3 + distanciaCentro + Random.nextInt(0, 8)
    }

    private fun escribirEnMatriz(
        texto: String,
        posiciones: List<Posicion>,
        matriz: Array<CharArray>
    ) {
        for (indice in posiciones.indices) {
            val posicion = posiciones[indice]
            matriz[posicion.fila][posicion.columna] = texto[indice]
        }
    }

    private fun rellenarCeldasVacias(matriz: Array<CharArray>, filas: Int, columnas: Int) {
        for (fila in 0 until filas) {
            for (columna in 0 until columnas) {
                if (matriz[fila][columna] == CELDA_VACIA) {
                    matriz[fila][columna] = ALFABETO.random()
                }
            }
        }
    }

    private companion object {
        const val MAX_INTENTOS_POR_PALABRA = 700
        const val CELDA_VACIA = ' '
        val ALFABETO = ('A'..'Z').toList()
    }
}
