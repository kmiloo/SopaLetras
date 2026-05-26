package com.miempresa.sopaletras.dominio.casosdeuso

import com.miempresa.sopaletras.dominio.modelos.Celda
import com.miempresa.sopaletras.dominio.modelos.Palabra

/**
 * Caso de uso que valida si la secuencia de celdas seleccionadas por el usuario
 * coincide con la ubicación real de alguna palabra objetivo en la matriz.
 *
 * La validación se hace comparando las posiciones (orden directo o inverso),
 * no sólo el texto. Esto garantiza que el jugador realmente encontró la palabra
 * en la dirección en la que fue colocada, y no formó las letras por coincidencia
 * eligiendo celdas no contiguas.
 */
class ValidarPalabraUseCase {

    operator fun invoke(
        celdasSeleccionadas: List<Celda>,
        palabrasObjetivo: List<Palabra>
    ): ResultadoValidacion {

        if (celdasSeleccionadas.isEmpty()) {
            return ResultadoValidacion.PalabraInvalida
        }

        val posicionesSeleccionadas = celdasSeleccionadas.map { it.posicion }

        val palabraEncontrada = palabrasObjetivo.firstOrNull { palabra ->
            !palabra.estaEncontrada && (
                palabra.posiciones == posicionesSeleccionadas ||
                    palabra.posiciones == posicionesSeleccionadas.reversed()
                )
        }

        return palabraEncontrada
            ?.let { ResultadoValidacion.PalabraValida(it) }
            ?: ResultadoValidacion.PalabraInvalida
    }
}

/**
 * Resultado sellado para representar las posibles salidas de la validación.
 */
sealed class ResultadoValidacion {
    data class PalabraValida(val palabra: Palabra) : ResultadoValidacion()
    data object PalabraInvalida : ResultadoValidacion()
}
