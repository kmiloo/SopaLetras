package com.miempresa.sopaletras.presentacion.ui.tema

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val EsquemaOscuro = darkColorScheme(
    primary = AzulJuegoClaro,
    secondary = VerdeAciertoClaro,
    tertiary = AmarilloPistaClaro,
    background = FondoOscuro,
    surface = SuperficieOscuro
)

private val EsquemaClaro = lightColorScheme(
    primary = AzulJuego,
    secondary = VerdeAcierto,
    tertiary = AmarilloPista,
    background = FondoClaro,
    surface = SuperficieClaro
)

/**
 * Tema visual principal de la aplicación. Soporta tema claro/oscuro
 * y colores dinámicos en dispositivos con Android 12+.
 */
@Composable
fun TemaSopaLetras(
    temaOscuro: Boolean = isSystemInDarkTheme(),
    colorDinamico: Boolean = false,
    content: @Composable () -> Unit
) {
    val esquemaColores = when {
        colorDinamico && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (temaOscuro) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        temaOscuro -> EsquemaOscuro
        else -> EsquemaClaro
    }

    MaterialTheme(
        colorScheme = esquemaColores,
        typography = Tipografia,
        content = content
    )
}
