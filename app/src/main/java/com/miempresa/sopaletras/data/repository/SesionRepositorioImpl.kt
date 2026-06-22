package com.miempresa.sopaletras.data.repository

import android.content.SharedPreferences
import com.miempresa.sopaletras.data.remote.api.SopaLetrasBackendApiService
import com.miempresa.sopaletras.data.remote.dto.CreateScoreRequestDto
import com.miempresa.sopaletras.data.remote.dto.LoginRequestDto
import com.miempresa.sopaletras.data.remote.dto.RegisterRequestDto
import com.miempresa.sopaletras.data.remote.dto.ScoreDto
import com.miempresa.sopaletras.data.remote.dto.UserDto
import com.miempresa.sopaletras.domain.model.PuntajeRanking
import com.miempresa.sopaletras.domain.model.UsuarioSesion
import com.miempresa.sopaletras.domain.repository.SesionRepositorio

/**
 * Repositorio de sesion: unica capa de la app que conoce Retrofit y SharedPreferences.
 */
class SesionRepositorioImpl(
    private val api: SopaLetrasBackendApiService,
    private val preferencias: SharedPreferences
) : SesionRepositorio {

    override val usuarioActual: UsuarioSesion?
        get() {
            val id = preferencias.getString(CLAVE_USER_ID, null) ?: return null
            val username = preferencias.getString(CLAVE_USERNAME, null) ?: return null
            val email = preferencias.getString(CLAVE_EMAIL, null) ?: return null
            return UsuarioSesion(id = id, username = username, email = email)
        }

    override val haySesionActiva: Boolean
        get() = !preferencias.getString(CLAVE_TOKEN, null).isNullOrBlank()

    override suspend fun registrar(
        username: String,
        email: String,
        password: String
    ): Result<UsuarioSesion> = runCatching {
        val usuario = api.registrar(RegisterRequestDto(username, email, password)).toDomain()
        login(email, password).getOrThrow()
        usuarioActual ?: usuario
    }

    override suspend fun login(email: String, password: String): Result<UsuarioSesion> = runCatching {
        val response = api.login(LoginRequestDto(email, password))
        guardarSesion(token = response.token, user = response.user)
        response.user.toDomain()
    }

    override suspend fun guardarPuntaje(score: Int, durationSeconds: Int): Result<Unit> = runCatching {
        val token = preferencias.getString(CLAVE_TOKEN, null)
            ?: error("Inicia sesion para guardar puntajes")

        api.guardarPuntaje(
            authorization = "Bearer $token",
            request = CreateScoreRequestDto(score = score, durationSeconds = durationSeconds)
        )
    }

    override suspend fun obtenerTop10(): Result<List<PuntajeRanking>> = runCatching {
        val token = preferencias.getString(CLAVE_TOKEN, null)
            ?: error("Inicia sesion para ver el ranking")

        api.obtenerTop10("Bearer $token").map { it.toDomain() }
    }

    override fun cerrarSesion() {
        preferencias.edit().clear().apply()
    }

    private fun guardarSesion(token: String, user: UserDto) {
        preferencias.edit()
            .putString(CLAVE_TOKEN, token)
            .putString(CLAVE_USER_ID, user.id)
            .putString(CLAVE_USERNAME, user.username)
            .putString(CLAVE_EMAIL, user.email)
            .apply()
    }

    private fun UserDto.toDomain(): UsuarioSesion =
        UsuarioSesion(id = id, username = username, email = email)

    private fun ScoreDto.toDomain(): PuntajeRanking =
        PuntajeRanking(
            username = username ?: "Jugador",
            score = score,
            durationSeconds = durationSeconds
        )

    private companion object {
        const val CLAVE_TOKEN = "token"
        const val CLAVE_USER_ID = "user_id"
        const val CLAVE_USERNAME = "username"
        const val CLAVE_EMAIL = "email"
    }
}
