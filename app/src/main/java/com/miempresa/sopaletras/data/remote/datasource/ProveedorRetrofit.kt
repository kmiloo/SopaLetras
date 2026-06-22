package com.miempresa.sopaletras.data.remote.datasource

import com.miempresa.sopaletras.data.remote.api.PalabrasApiService
import com.miempresa.sopaletras.data.remote.api.SopaLetrasBackendApiService

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Configuracion centralizada de Retrofit para consumir Random Words API.
 */
object ProveedorRetrofit {

    private const val URL_BASE = "https://random-words-api.kushcreates.com/"
    private const val URL_BACKEND_SOPA_LETRAS = "http://192.168.4.24:8080/"

    val servicioPalabras: PalabrasApiService by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PalabrasApiService::class.java)
    }

    val servicioBackend: SopaLetrasBackendApiService by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BACKEND_SOPA_LETRAS)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SopaLetrasBackendApiService::class.java)
    }
}
