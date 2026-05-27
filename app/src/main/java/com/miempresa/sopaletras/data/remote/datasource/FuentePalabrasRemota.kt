package com.miempresa.sopaletras.data.remote.datasource

import com.miempresa.sopaletras.data.remote.api.PalabrasApiService
import java.text.Normalizer

/**
 * Fuente remota encargada solo de pedir palabras y normalizarlas para el juego.
 */
class FuentePalabrasRemota(
    private val servicio: PalabrasApiService
) {

    suspend fun obtenerPalabras(cantidad: Int): Result<List<String>> = runCatching {
        servicio.obtenerPalabras(
            idioma = IDIOMA_ESPANOL,
            cantidad = cantidad,
            tipo = TIPO_MAYUSCULAS
        )
            .mapNotNull { it.word?.let(::normalizar) }
            .filter { it.length in LONGITUD_MINIMA..LONGITUD_MAXIMA }
            .distinct()
    }

    private fun normalizar(palabra: String): String {
        val sinDiacriticos = Normalizer.normalize(palabra.uppercase(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")

        return sinDiacriticos.filter { it in 'A'..'Z' }
    }

    private companion object {
        const val IDIOMA_ESPANOL = "es"
        const val TIPO_MAYUSCULAS = "uppercase"
        const val LONGITUD_MINIMA = 3
        const val LONGITUD_MAXIMA = 12
    }
}
