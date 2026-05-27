package com.miempresa.sopaletras.data.remote.datasource

import com.miempresa.sopaletras.data.remote.api.PalabrasApiService

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Configuracion centralizada de Retrofit para consumir Random Words API.
 */
object ProveedorRetrofit {

    private const val URL_BASE = "https://random-words-api.kushcreates.com/"

    val servicioPalabras: PalabrasApiService by lazy {
        Retrofit.Builder()
            .baseUrl(URL_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PalabrasApiService::class.java)
    }
}
