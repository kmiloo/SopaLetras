package com.miempresa.sopaletras.data.remote.api

import com.miempresa.sopaletras.data.remote.dto.CreateScoreRequestDto
import com.miempresa.sopaletras.data.remote.dto.LoginRequestDto
import com.miempresa.sopaletras.data.remote.dto.LoginResponseDto
import com.miempresa.sopaletras.data.remote.dto.RegisterRequestDto
import com.miempresa.sopaletras.data.remote.dto.ScoreDto
import com.miempresa.sopaletras.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * API Retrofit del backend local SopaLetras.
 * La UI no habla con esta interfaz directamente: pasa por repositorio y ViewModel.
 */
interface SopaLetrasBackendApiService {

    @POST("v1/auth/register")
    suspend fun registrar(@Body request: RegisterRequestDto): UserDto

    @POST("v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("v1/scores")
    suspend fun guardarPuntaje(
        @Header("Authorization") authorization: String,
        @Body request: CreateScoreRequestDto
    ): ScoreDto

    @GET("v1/scores/top10")
    suspend fun obtenerTop10(
        @Header("Authorization") authorization: String
    ): List<ScoreDto>
}
