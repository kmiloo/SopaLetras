package com.miempresa.sopaletras.datos.fuentes.remoto

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfaz Retrofit para Random Words API de KushCreates.
 */
interface PalabrasApiService {

    /**
     * Obtiene palabras filtradas para armar la sopa de letras.
     */
    @GET("api")
    suspend fun obtenerPalabras(
        @Query("language") idioma: String = "es",
        @Query("words") cantidad: Int,
        @Query("type") tipo: String = "uppercase",
        @Query("category") categoria: String? = null,
        @Query("length") longitud: Int? = null,
        @Query("alphabetize") ordenar: Boolean? = null
    ): List<PalabraRemotaDto>
}

data class PalabraRemotaDto(
    val word: String? = null,
    val length: Int? = null,
    val category: String? = null,
    val language: String? = null
)
