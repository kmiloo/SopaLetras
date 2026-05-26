package com.miempresa.sopaletras.dominio.casosdeuso

import com.miempresa.sopaletras.dominio.modelos.Dificultad
import com.miempresa.sopaletras.dominio.modelos.SopaLetras
import com.miempresa.sopaletras.dominio.repositorios.SopaLetrasRepositorio

/**
 * Caso de uso encargado de obtener una nueva partida de sopa de letras.
 *
 * Cada caso de uso tiene una única responsabilidad (Principio SRP de SOLID),
 * y se invoca como una función gracias al operador `invoke`.
 */
class ObtenerSopaLetrasUseCase(
    private val repositorio: SopaLetrasRepositorio
) {
    suspend operator fun invoke(dificultad: Dificultad): Result<SopaLetras> {
        return repositorio.obtenerSopaLetras(dificultad)
    }
}
