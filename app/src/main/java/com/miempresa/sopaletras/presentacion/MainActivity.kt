package com.miempresa.sopaletras.presentacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.miempresa.sopaletras.datos.fuentes.local.FuentePalabrasLocal
import com.miempresa.sopaletras.datos.fuentes.remoto.FuentePalabrasRemota
import com.miempresa.sopaletras.datos.fuentes.remoto.ProveedorRetrofit
import com.miempresa.sopaletras.datos.repositorios.SopaLetrasRepositorioImpl
import com.miempresa.sopaletras.dominio.casosdeuso.ObtenerSopaLetrasUseCase
import com.miempresa.sopaletras.dominio.casosdeuso.ValidarPalabraUseCase
import com.miempresa.sopaletras.presentacion.ui.pantallas.sopaletras.SopaLetrasScreen
import com.miempresa.sopaletras.presentacion.ui.pantallas.sopaletras.SopaLetrasViewModel
import com.miempresa.sopaletras.presentacion.ui.tema.TemaSopaLetras

/**
 * Actividad principal y único punto de entrada de la app.
 *
 * Para esta entrega se instancian manualmente las dependencias del grafo
 * (fuentes de datos → repositorio → casos de uso → ViewModel).
 * En entregas futuras esta responsabilidad se delegará a Hilt (paquete `di`).
 */
class MainActivity : ComponentActivity() {

    private val viewModel: SopaLetrasViewModel by viewModels {
        // Composición manual de dependencias siguiendo Clean Architecture
        val fuenteRemota = FuentePalabrasRemota(ProveedorRetrofit.servicioPalabras)
        val fuenteLocal = FuentePalabrasLocal()
        val repositorio = SopaLetrasRepositorioImpl(
            fuenteRemota = fuenteRemota,
            fuenteLocal = fuenteLocal
        )
        SopaLetrasViewModel.Factoria(
            obtenerSopaLetrasUseCase = ObtenerSopaLetrasUseCase(repositorio),
            validarPalabraUseCase = ValidarPalabraUseCase()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemaSopaLetras {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SopaLetrasScreen(viewModel = viewModel)
                }
            }
        }
    }
}
