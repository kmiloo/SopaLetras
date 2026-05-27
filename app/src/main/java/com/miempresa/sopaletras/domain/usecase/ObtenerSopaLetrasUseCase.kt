package com.miempresa.sopaletras.domain.usecase

import com.miempresa.sopaletras.domain.model.Dificultad
import com.miempresa.sopaletras.domain.model.SopaLetras
import com.miempresa.sopaletras.domain.repository.SopaLetrasRepositorio

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
