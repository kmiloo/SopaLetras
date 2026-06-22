package com.miempresa.sopaletras.domain.repository

import com.miempresa.sopaletras.domain.model.PuntajeRanking
import com.miempresa.sopaletras.domain.model.UsuarioSesion

interface SesionRepositorio {
    val usuarioActual: UsuarioSesion?
    val haySesionActiva: Boolean

    suspend fun registrar(username: String, email: String, password: String): Result<UsuarioSesion>
    suspend fun login(email: String, password: String): Result<UsuarioSesion>
    suspend fun guardarPuntaje(score: Int, durationSeconds: Int): Result<Unit>
    suspend fun obtenerTop10(): Result<List<PuntajeRanking>>
    fun cerrarSesion()
}
