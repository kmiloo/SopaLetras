package com.miempresa.sopaletras

import com.miempresa.sopaletras.dominio.casosdeuso.ResultadoValidacion
import com.miempresa.sopaletras.dominio.casosdeuso.ValidarPalabraUseCase
import com.miempresa.sopaletras.dominio.modelos.Celda
import com.miempresa.sopaletras.dominio.modelos.Palabra
import com.miempresa.sopaletras.dominio.modelos.Posicion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para [ValidarPalabraUseCase].
 *
 * La validación se basa ahora en las posiciones reales de las celdas,
 * no únicamente en el texto formado.
 */
class ValidarPalabraUseCaseTest {

    private val validar = ValidarPalabraUseCase()

    private val posicionesKotlin = listOf(
        Posicion(0, 0), Posicion(0, 1), Posicion(0, 2),
        Posicion(0, 3), Posicion(0, 4), Posicion(0, 5)
    )

    private val celdasKotlin = "KOTLIN".mapIndexed { i, letra ->
        Celda(posicion = posicionesKotlin[i], letra = letra)
    }

    @Test
    fun `selección vacía retorna PalabraInvalida`() {
        val resultado = validar(
            celdasSeleccionadas = emptyList(),
            palabrasObjetivo = listOf(Palabra("KOTLIN", posiciones = posicionesKotlin))
        )
        assertTrue(resultado is ResultadoValidacion.PalabraInvalida)
    }

    @Test
    fun `selección coincidente en orden directo retorna PalabraValida`() {
        val palabra = Palabra(texto = "KOTLIN", posiciones = posicionesKotlin)

        val resultado = validar(
            celdasSeleccionadas = celdasKotlin,
            palabrasObjetivo = listOf(palabra)
        )

        assertTrue(resultado is ResultadoValidacion.PalabraValida)
        assertEquals("KOTLIN", (resultado as ResultadoValidacion.PalabraValida).palabra.texto)
    }

    @Test
    fun `selección coincidente en orden inverso retorna PalabraValida`() {
        val palabra = Palabra(texto = "KOTLIN", posiciones = posicionesKotlin)

        val resultado = validar(
            celdasSeleccionadas = celdasKotlin.reversed(),
            palabrasObjetivo = listOf(palabra)
        )

        assertTrue(resultado is ResultadoValidacion.PalabraValida)
    }

    @Test
    fun `selección con posiciones distintas retorna PalabraInvalida`() {
        val posicionesOtras = listOf(
            Posicion(1, 0), Posicion(1, 1), Posicion(1, 2),
            Posicion(1, 3), Posicion(1, 4), Posicion(1, 5)
        )
        val celdasOtras = "KOTLIN".mapIndexed { i, letra ->
            Celda(posicion = posicionesOtras[i], letra = letra)
        }
        val palabra = Palabra(texto = "KOTLIN", posiciones = posicionesKotlin)

        val resultado = validar(
            celdasSeleccionadas = celdasOtras,
            palabrasObjetivo = listOf(palabra)
        )

        assertTrue(resultado is ResultadoValidacion.PalabraInvalida)
    }

    @Test
    fun `palabra ya encontrada no vuelve a validarse`() {
        val palabra = Palabra(
            texto = "KOTLIN",
            posiciones = posicionesKotlin,
            estaEncontrada = true
        )

        val resultado = validar(
            celdasSeleccionadas = celdasKotlin,
            palabrasObjetivo = listOf(palabra)
        )

        assertTrue(resultado is ResultadoValidacion.PalabraInvalida)
    }
}
