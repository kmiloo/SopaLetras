package com.miempresa.sopaletras.dominio.repositorios

import com.miempresa.sopaletras.dominio.modelos.Dificultad
import com.miempresa.sopaletras.dominio.modelos.SopaLetras

/**
 * Contrato del repositorio de sopa de letras.
 *
 * Aplica el Principio de Inversión de Dependencias (DIP de SOLID):
 * la capa de dominio define el contrato y la capa de datos lo implementa.
 * Esto permite que el dominio no dependa de detalles de infraestructura.
 */
interface SopaLetrasRepositorio {

    /**
     * Obtiene una nueva sopa de letras según el nivel de dificultad.
     * Se devuelve un [Result] para encapsular el éxito o el fracaso de la operación.
     */
    suspend fun obtenerSopaLetras(dificultad: Dificultad): Result<SopaLetras>

    /**
     * Persiste el progreso actual del jugador. Útil para reanudar partidas.
     */
    suspend fun guardarProgreso(sopaLetras: SopaLetras): Result<Unit>
}
