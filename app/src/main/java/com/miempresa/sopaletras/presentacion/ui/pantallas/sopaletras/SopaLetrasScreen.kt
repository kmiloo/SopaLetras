package com.miempresa.sopaletras.presentacion.ui.pantallas.sopaletras

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miempresa.sopaletras.dominio.modelos.Celda
import com.miempresa.sopaletras.dominio.modelos.Dificultad
import com.miempresa.sopaletras.dominio.modelos.Matriz
import com.miempresa.sopaletras.dominio.modelos.Palabra
import com.miempresa.sopaletras.dominio.modelos.Posicion
import com.miempresa.sopaletras.presentacion.ui.tema.obtenerColorPalabra
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max

/**
 * Pantalla principal del juego. Es un Composable "tonto":
 * sólo observa el estado y reenvía eventos al ViewModel.
 *
 * La UI se compone en tarjetas (Cards) para separar visualmente las secciones:
 * encabezado, progreso, cuadrícula, listado de palabras, acciones y selector de dificultad.
 */
@Composable
fun SopaLetrasScreen(viewModel: SopaLetrasViewModel) {
    val estado by viewModel.estado.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TituloEncabezado()

        val sopa = estado.sopaLetras
        if (sopa != null) {
            ProgresoJuego(
                encontradas = estado.palabrasEncontradas.size,
                total = sopa.palabras.size,
                dificultad = sopa.dificultad,
                segundosTranscurridos = estado.segundosTranscurridos,
                errores = estado.errores,
                pistasUsadas = estado.pistasUsadas
            )
        }

        when {
            estado.estaCargando -> CargaIndicador()
            estado.mensajeError != null -> MensajeErrorTarjeta(estado.mensajeError!!)
            sopa == null -> PantallaInicio(onElegirDificultad = viewModel::cargarSopaLetras)
            else -> {
                CuadriculaCarta(
                    matriz = sopa.matriz,
                    celdasSeleccionadas = estado.celdasSeleccionadas,
                    onSeleccionArrastrada = viewModel::reemplazarSeleccion,
                    onArrastreFinalizado = viewModel::confirmarSeleccionArrastrada,
                    onArrastreCancelado = viewModel::limpiarSeleccion
                )
                ListaPalabrasCarta(palabras = sopa.palabras)
                BotonesAccion(
                    haySeleccion = estado.celdasSeleccionadas.isNotEmpty(),
                    juegoFinalizado = estado.juegoCompletado,
                    onPista = viewModel::usarPista,
                    onRendirse = viewModel::rendirse,
                    onLimpiar = viewModel::limpiarSeleccion,
                    onNuevaPartida = { viewModel.cargarSopaLetras(sopa.dificultad) }
                )
                SelectorDificultad(
                    dificultadActual = sopa.dificultad,
                    onCambioDificultad = viewModel::cargarSopaLetras
                )
                AnimatedVisibility(
                    visible = estado.juegoCompletado,
                    enter = fadeIn(animationSpec = tween(durationMillis = 400)),
                    exit = fadeOut()
                ) {
                    MensajeVictoria(
                        fueRendido = estado.juegoRendido,
                        onNuevaPartida = { viewModel.cargarSopaLetras(sopa.dificultad) }
                    )
                }
            }
        }
    }
}

/* =====================  Componentes de la pantalla  ===================== */

@Composable
private fun TituloEncabezado() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Sopa de Letras",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Desliza en linea recta para marcar una palabra",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PantallaInicio(onElegirDificultad: (Dificultad) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Elige dificultad",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Cada nivel cambia el tamano del tablero y la cantidad de palabras.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Dificultad.entries.forEach { dificultad ->
                OpcionDificultad(
                    dificultad = dificultad,
                    onClick = { onElegirDificultad(dificultad) }
                )
            }
        }
    }
}

@Composable
private fun OpcionDificultad(dificultad: Dificultad, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = nombreDificultad(dificultad),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${dificultad.filas} x ${dificultad.columnas} · ${dificultad.cantidadPalabras} palabras",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "Jugar",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ProgresoJuego(
    encontradas: Int,
    total: Int,
    dificultad: Dificultad,
    segundosTranscurridos: Int,
    errores: Int,
    pistasUsadas: Int
) {
    val progreso = if (total > 0) encontradas.toFloat() / total else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Encontradas: $encontradas / $total",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                EtiquetaDificultad(dificultad)
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IndicadorPartida(titulo = "Tiempo", valor = formatearTiempo(segundosTranscurridos))
                IndicadorPartida(titulo = "Errores", valor = errores.toString())
                IndicadorPartida(titulo = "Pistas", valor = pistasUsadas.toString())
            }
        }
    }
}

@Composable
private fun IndicadorPartida(titulo: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valor,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = titulo,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatearTiempo(segundos: Int): String {
    val minutos = segundos / 60
    val resto = segundos % 60
    return "%02d:%02d".format(minutos, resto)
}

@Composable
private fun EtiquetaDificultad(dificultad: Dificultad) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = nombreDificultad(dificultad),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun nombreDificultad(d: Dificultad): String = when (d) {
    Dificultad.FACIL -> "FÁCIL"
    Dificultad.MEDIO -> "MEDIO"
    Dificultad.DIFICIL -> "DIFÍCIL"
}

@Composable
private fun CargaIndicador() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Obteniendo palabras…",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MensajeErrorTarjeta(mensaje: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Text(
            text = "Error: $mensaje",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

/* ----------  Cuadrícula  ---------- */

/**
 * Tarjeta contenedora de la cuadrícula. Usa tamaños de celda fijos y un
 * scroll horizontal para garantizar que las celdas sean tappeables incluso
 * en dificultad DIFÍCIL (15 x 15).
 */
@Composable
private fun CuadriculaCarta(
    matriz: Matriz,
    celdasSeleccionadas: List<Celda>,
    onSeleccionArrastrada: (List<Celda>) -> Unit,
    onArrastreFinalizado: () -> Unit,
    onArrastreCancelado: () -> Unit
) {
    val anchoPantalla = LocalConfiguration.current.screenWidthDp.dp
    val tamCelda: Dp = when {
        matriz.columnas <= 8 -> ((anchoPantalla - 44.dp) / matriz.columnas.toFloat()).coerceIn(34.dp, 44.dp)
        matriz.columnas <= 12 -> ((anchoPantalla - 44.dp) / matriz.columnas.toFloat()).coerceIn(26.dp, 38.dp)
        matriz.columnas > 12 -> ((anchoPantalla - 44.dp) / matriz.columnas.toFloat()).coerceIn(22.dp, 38.dp)
        else -> 38.dp // DIFÍCIL: mantiene celdas tappeables; la cuadrícula puede desbordarse y se scrollea
    }
    val tamFuente: TextUnit = (tamCelda.value * 0.62f).coerceIn(14f, 22f).sp

    val densidad = LocalDensity.current
    val tamCeldaPx = with(densidad) { tamCelda.toPx() }
    var inicioArrastre by remember { mutableStateOf<Celda?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .pointerInput(matriz, tamCeldaPx) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val celdaInicial = celdaDesdeOffset(offset, tamCeldaPx, matriz)
                                inicioArrastre = celdaInicial
                                if (celdaInicial != null) {
                                    onSeleccionArrastrada(listOf(celdaInicial))
                                }
                            },
                            onDrag = { cambio, _ ->
                                val inicio = inicioArrastre ?: return@detectDragGestures
                                val celdaActual = celdaDesdeOffset(cambio.position, tamCeldaPx, matriz)
                                    ?: return@detectDragGestures
                                onSeleccionArrastrada(
                                    construirSeleccionLineal(
                                        matriz = matriz,
                                        inicio = inicio.posicion,
                                        fin = celdaActual.posicion
                                    )
                                )
                            },
                            onDragEnd = {
                                inicioArrastre = null
                                onArrastreFinalizado()
                            },
                            onDragCancel = {
                                inicioArrastre = null
                                onArrastreCancelado()
                            }
                        )
                    }
            ) {
                matriz.celdas.forEach { fila ->
                    Row {
                        fila.forEach { celda ->
                            CeldaLetra(
                                celda = celda,
                                estaSeleccionada = celdasSeleccionadas.contains(celda),
                                tamFuente = tamFuente,
                                tamCelda = tamCelda
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun celdaDesdeOffset(offset: Offset, tamCeldaPx: Float, matriz: Matriz): Celda? {
    if (offset.x < 0f || offset.y < 0f) return null

    val columna = floor(offset.x / tamCeldaPx).toInt()
    val fila = floor(offset.y / tamCeldaPx).toInt()
    return matriz.obtenerCelda(fila, columna)
}

private fun construirSeleccionLineal(
    matriz: Matriz,
    inicio: Posicion,
    fin: Posicion
): List<Celda> {
    val diferenciaFila = fin.fila - inicio.fila
    val diferenciaColumna = fin.columna - inicio.columna

    if (diferenciaFila == 0 && diferenciaColumna == 0) {
        return listOfNotNull(matriz.obtenerCelda(inicio.fila, inicio.columna))
    }

    val pasoFila: Int
    val pasoColumna: Int
    val pasos: Int

    when {
        diferenciaFila == 0 -> {
            pasoFila = 0
            pasoColumna = signo(diferenciaColumna)
            pasos = abs(diferenciaColumna)
        }
        diferenciaColumna == 0 -> {
            pasoFila = signo(diferenciaFila)
            pasoColumna = 0
            pasos = abs(diferenciaFila)
        }
        abs(diferenciaFila) == abs(diferenciaColumna) -> {
            pasoFila = signo(diferenciaFila)
            pasoColumna = signo(diferenciaColumna)
            pasos = abs(diferenciaFila)
        }
        else -> {
            val seleccionDominante = ajustarADireccionMasCercana(diferenciaFila, diferenciaColumna)
            pasoFila = seleccionDominante.pasoFila
            pasoColumna = seleccionDominante.pasoColumna
            pasos = seleccionDominante.pasos
        }
    }

    return (0..pasos).mapNotNull { indice ->
        matriz.obtenerCelda(
            fila = inicio.fila + pasoFila * indice,
            columna = inicio.columna + pasoColumna * indice
        )
    }
}

private data class SeleccionDominante(
    val pasoFila: Int,
    val pasoColumna: Int,
    val pasos: Int
)

private fun ajustarADireccionMasCercana(
    diferenciaFila: Int,
    diferenciaColumna: Int
): SeleccionDominante {
    val absFila = abs(diferenciaFila)
    val absColumna = abs(diferenciaColumna)

    return when {
        absColumna > absFila * 2 -> SeleccionDominante(
            pasoFila = 0,
            pasoColumna = signo(diferenciaColumna),
            pasos = absColumna
        )
        absFila > absColumna * 2 -> SeleccionDominante(
            pasoFila = signo(diferenciaFila),
            pasoColumna = 0,
            pasos = absFila
        )
        else -> SeleccionDominante(
            pasoFila = signo(diferenciaFila),
            pasoColumna = signo(diferenciaColumna),
            pasos = max(absFila, absColumna)
        )
    }
}

private fun signo(valor: Int): Int = when {
    valor > 0 -> 1
    valor < 0 -> -1
    else -> 0
}

@Composable
private fun CeldaLetra(
    celda: Celda,
    estaSeleccionada: Boolean,
    tamFuente: TextUnit,
    tamCelda: Dp
) {
    val colorFondoObjetivo = when {
        celda.perteneceAPalabraEncontrada -> obtenerColorPalabra(celda.colorIndice ?: 0)
        estaSeleccionada -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }
    val colorTextoObjetivo = when {
        celda.perteneceAPalabraEncontrada -> Color.White
        estaSeleccionada -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    val colorFondo by animateColorAsState(
        targetValue = colorFondoObjetivo,
        animationSpec = tween(250),
        label = "fondoCelda"
    )
    val colorTexto by animateColorAsState(
        targetValue = colorTextoObjetivo,
        animationSpec = tween(250),
        label = "textoCelda"
    )

    Box(
        modifier = Modifier
            .size(tamCelda)
            .padding(1.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(colorFondo)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = celda.letra.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = tamFuente,
            color = colorTexto
        )
    }
}

/* ----------  Lista de palabras  ---------- */

@Composable
private fun ListaPalabrasCarta(palabras: List<Palabra>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Palabras a encontrar",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                palabras.chunked(2).forEach { paresPalabras ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        paresPalabras.forEach { palabra ->
                            Box(modifier = Modifier.weight(1f)) {
                                ChipPalabra(
                                    palabra = palabra,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        if (paresPalabras.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipPalabra(palabra: Palabra, modifier: Modifier = Modifier) {
    val fondoColor = if (palabra.estaEncontrada) {
        obtenerColorPalabra(palabra.colorIndice)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val colorTexto = if (palabra.estaEncontrada) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        color = fondoColor,
        shape = RoundedCornerShape(20.dp),
        border = if (!palabra.estaEncontrada) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        } else {
            null
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (palabra.estaEncontrada) {
                Text(
                    text = "✓ ",
                    color = colorTexto,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = palabra.texto,
                color = colorTexto,
                fontWeight = FontWeight.SemiBold,
                style = if (palabra.estaEncontrada) {
                    MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = TextDecoration.LineThrough
                    )
                } else {
                    MaterialTheme.typography.bodyMedium
                }
            )
        }
    }
}

/* ----------  Botones de acción  ---------- */

@Composable
private fun BotonesAccion(
    haySeleccion: Boolean,
    juegoFinalizado: Boolean,
    onPista: () -> Unit,
    onRendirse: () -> Unit,
    onLimpiar: () -> Unit,
    onNuevaPartida: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPista,
                    enabled = !juegoFinalizado,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pista")
                }
                OutlinedButton(
                    onClick = onRendirse,
                    enabled = !juegoFinalizado,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Rendirse")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onLimpiar,
                    enabled = haySeleccion && !juegoFinalizado,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Limpiar")
                }
                Button(
                    onClick = onNuevaPartida,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Nuevo juego")
                }
            }
        }
    }
}

/* ----------  Selector de dificultad  ---------- */

@Composable
private fun SelectorDificultad(
    dificultadActual: Dificultad,
    onCambioDificultad: (Dificultad) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Cambiar dificultad",
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Dificultad.entries.forEach { d ->
                    val seleccionada = d == dificultadActual
                    if (seleccionada) {
                        Button(
                            onClick = { onCambioDificultad(d) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(nombreDificultad(d), fontSize = 12.sp)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onCambioDificultad(d) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(nombreDificultad(d), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

/* ----------  Mensaje de victoria  ---------- */

@Composable
private fun MensajeVictoria(fueRendido: Boolean, onNuevaPartida: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = if (fueRendido) "Fin" else "OK", fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (fueRendido) "Partida revelada" else "¡Felicidades!",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = if (fueRendido) {
                    "Se mostraron todas las palabras del tablero"
                } else {
                    "Has encontrado todas las palabras"
                },
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onNuevaPartida) {
                Text("Nueva partida")
            }
        }
    }
}
