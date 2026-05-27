package com.miempresa.sopaletras.presentation.screens.sopaletras

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miempresa.sopaletras.domain.model.Celda
import com.miempresa.sopaletras.domain.model.Dificultad
import com.miempresa.sopaletras.domain.model.Matriz
import com.miempresa.sopaletras.domain.model.Palabra
import com.miempresa.sopaletras.domain.model.Posicion
import com.miempresa.sopaletras.presentation.components.CountdownTimerComponent
import com.miempresa.sopaletras.presentation.components.LetterCell
import com.miempresa.sopaletras.presentation.components.VictoryCelebrationOverlay
import com.miempresa.sopaletras.presentation.theme.AlertRed
import com.miempresa.sopaletras.presentation.theme.FocusTextSecondary
import com.miempresa.sopaletras.presentation.theme.HighStimulusSurface
import com.miempresa.sopaletras.presentation.theme.HighStimulusSurfaceHigh
import com.miempresa.sopaletras.presentation.theme.NeonCyan
import com.miempresa.sopaletras.presentation.theme.NeonPink
import com.miempresa.sopaletras.presentation.theme.NeonPurple
import com.miempresa.sopaletras.presentation.theme.SelectionGradientEnd
import com.miempresa.sopaletras.presentation.theme.SuccessGold
import com.miempresa.sopaletras.presentation.theme.dangerBrush
import com.miempresa.sopaletras.presentation.theme.obtenerColorPalabra
import com.miempresa.sopaletras.presentation.theme.selectionNeonBrush
import com.miempresa.sopaletras.presentation.viewmodel.SopaLetrasViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max

@Composable
fun SopaLetrasScreen(viewModel: SopaLetrasViewModel) {
    val estado by viewModel.estado.collectAsState()
    val sopa = estado.sopaLetras
    val totalSeconds = sopa?.dificultad?.totalSeconds() ?: Dificultad.FACIL.totalSeconds()
    val remainingSeconds = totalSeconds - estado.segundosTranscurridos

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HeaderPanel()

            when {
                estado.estaCargando -> LoadingPanel()
                estado.mensajeError != null -> ErrorPanel(estado.mensajeError.orEmpty())
                sopa == null -> StartPanel(onStart = viewModel::cargarSopaLetras)
                else -> {
                    CountdownTimerComponent(
                        remainingSeconds = remainingSeconds,
                        totalSeconds = totalSeconds
                    )
                    ScoreStrip(
                        found = estado.palabrasEncontradas.size,
                        total = sopa.palabras.size,
                        errors = estado.errores,
                        hints = estado.pistasUsadas,
                        difficulty = sopa.dificultad
                    )
                    NeonWordGrid(
                        matriz = sopa.matriz,
                        selectedCells = estado.celdasSeleccionadas,
                        invalidCells = estado.ultimaSeleccionInvalida,
                        invalidShakeKey = estado.eventoSeleccionInvalida,
                        onSelectionChanged = viewModel::reemplazarSeleccion,
                        onDragFinished = viewModel::confirmarSeleccionArrastrada,
                        onDragCanceled = viewModel::limpiarSeleccion
                    )
                    WordBadges(words = sopa.palabras)
                    ActionPanel(
                        gameFinished = estado.juegoCompletado,
                        onHint = viewModel::usarPista,
                        onGiveUp = viewModel::rendirse,
                        onClear = viewModel::limpiarSeleccion,
                        onNewGame = { viewModel.cargarSopaLetras(sopa.dificultad) },
                        onDifficulty = viewModel::cargarSopaLetras
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        VictoryCelebrationOverlay(
            visible = estado.juegoCompletado && !estado.juegoRendido,
            score = calculateScore(
                totalSeconds = totalSeconds,
                remainingSeconds = remainingSeconds,
                errors = estado.errores,
                hints = estado.pistasUsadas,
                words = sopa?.palabras?.size ?: 0
            ),
            combo = estado.palabrasEncontradas.size.coerceAtLeast(1),
            onNewGame = { viewModel.cargarSopaLetras(sopa?.dificultad ?: Dificultad.FACIL) }
        )
    }
}

@Composable
private fun HeaderPanel() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
        color = HighStimulusSurface,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "SOPA RELAMPAGO",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Encuentra palabras rapido, evita errores y cierra la partida con estilo.",
                color = FocusTextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun StartPanel(onStart: (Dificultad) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = HighStimulusSurface)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.White,
                            HighStimulusSurfaceHigh,
                            NeonCyan.copy(alpha = 0.12f)
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Elige tu desafio",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Desliza sobre las letras antes de que el tiempo te alcance.",
                color = FocusTextSecondary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            MiniPreviewBoard()
            Dificultad.entries.forEach { dificultad ->
                DifficultyCard(dificultad = dificultad, onStart = onStart)
            }
        }
    }
}

@Composable
private fun MiniPreviewBoard() {
    val letters = listOf("S", "O", "P", "A", "L", "E", "T", "R", "A")
    Column(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.78f), RoundedCornerShape(8.dp))
            .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        letters.chunked(3).forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEachIndexed { columnIndex, letter ->
                    val selected = rowIndex == columnIndex
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (selected) selectionNeonBrush else Brush.linearGradient(listOf(HighStimulusSurfaceHigh, Color.White)),
                                RoundedCornerShape(7.dp)
                            )
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(7.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DifficultyCard(dificultad: Dificultad, onStart: (Dificultad) -> Unit) {
    val accent = when (dificultad) {
        Dificultad.FACIL -> NeonCyan
        Dificultad.MEDIO -> NeonPink
        Dificultad.DIFICIL -> AlertRed
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.38f), RoundedCornerShape(8.dp)),
        color = Color.White,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(accent.copy(alpha = 0.14f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = difficultyShortLabel(dificultad),
                    color = accent,
                    fontWeight = FontWeight.Black
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = difficultyLabel(dificultad),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
                Text(
                    text = "${dificultad.filas}x${dificultad.columnas} - ${dificultad.cantidadPalabras} palabras - ${dificultad.totalSeconds()} segundos",
                    color = FocusTextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Button(
                onClick = { onStart(dificultad) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text("Jugar", fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun ScoreStrip(
    found: Int,
    total: Int,
    errors: Int,
    hints: Int,
    difficulty: Dificultad
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HudStat("HECHAS", "$found/$total", Modifier.weight(1f), NeonCyan)
        HudStat("ERRORES", errors.toString(), Modifier.weight(1f), AlertRed)
        HudStat("PISTAS", hints.toString(), Modifier.weight(1f), SuccessGold)
        HudStat("NIVEL", difficultyShortLabel(difficulty), Modifier.weight(1f), NeonPurple)
    }
}

@Composable
private fun HudStat(label: String, value: String, modifier: Modifier, color: Color) {
    Column(
        modifier = modifier
            .background(HighStimulusSurfaceHigh, RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, color = color, fontWeight = FontWeight.Black, fontSize = 19.sp)
        Text(text = label, color = FocusTextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NeonWordGrid(
    matriz: Matriz,
    selectedCells: List<Celda>,
    invalidCells: List<Celda>,
    invalidShakeKey: Int,
    onSelectionChanged: (List<Celda>) -> Unit,
    onDragFinished: () -> Unit,
    onDragCanceled: () -> Unit
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cellSize = gridCellSize(screenWidth, matriz.columnas)
    val fontSize: TextUnit = (cellSize.value * 0.64f).coerceIn(20f, 28f).sp
    val density = LocalDensity.current
    val cellSizePx = with(density) { cellSize.toPx() }
    val selectedPositions = remember(selectedCells) { selectedCells.map { it.posicion }.toSet() }
    var visibleInvalidKey by remember { mutableStateOf(0) }
    LaunchedEffect(invalidShakeKey) {
        if (invalidShakeKey > 0) {
            visibleInvalidKey = invalidShakeKey
            delay(420)
            visibleInvalidKey = 0
        }
    }
    val activeInvalidCells = if (visibleInvalidKey == invalidShakeKey) invalidCells else emptyList()
    val invalidPositions = remember(activeInvalidCells, visibleInvalidKey) {
        activeInvalidCells.map { it.posicion }.toSet()
    }
    var dragStart by remember(matriz) { mutableStateOf<Celda?>(null) }
    var fingerPosition by remember { mutableStateOf<Offset?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = HighStimulusSurface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, NeonCyan.copy(alpha = 0.32f), RoundedCornerShape(8.dp))
                    .pointerInput(matriz, cellSizePx) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val start = cellFromOffset(offset, cellSizePx, matriz)
                                dragStart = start
                                fingerPosition = offset
                                if (start != null) onSelectionChanged(listOf(start))
                            },
                            onDrag = { change, _ ->
                                val start = dragStart ?: return@detectDragGestures
                                val current = cellFromOffset(change.position, cellSizePx, matriz)
                                    ?: return@detectDragGestures
                                fingerPosition = change.position
                                onSelectionChanged(buildLinearSelection(matriz, start.posicion, current.posicion))
                            },
                            onDragEnd = {
                                dragStart = null
                                fingerPosition = null
                                onDragFinished()
                            },
                            onDragCancel = {
                                dragStart = null
                                fingerPosition = null
                                onDragCanceled()
                            }
                        )
                    }
            ) {
                SelectionLineCanvas(
                    selectedCells = selectedCells,
                    invalidCells = activeInvalidCells,
                    invalidShakeKey = visibleInvalidKey,
                    cellSize = cellSize,
                    liveFinger = fingerPosition,
                    modifier = Modifier.matchParentSize()
                )
                Column {
                    matriz.celdas.forEach { row ->
                        Row {
                            row.forEach { cell ->
                                LetterCell(
                                    celda = cell,
                                    selected = cell.posicion in selectedPositions,
                                    invalidShakeKey = if (cell.posicion in invalidPositions) visibleInvalidKey else 0,
                                    size = cellSize,
                                    fontSize = fontSize
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionLineCanvas(
    selectedCells: List<Celda>,
    invalidCells: List<Celda>,
    invalidShakeKey: Int,
    cellSize: Dp,
    liveFinger: Offset?,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val cellPx = with(density) { cellSize.toPx() }
    val lineCells = if (invalidShakeKey > 0 && invalidCells.isNotEmpty()) invalidCells else selectedCells
    val lineBrush = if (invalidShakeKey > 0 && invalidCells.isNotEmpty()) dangerBrush else selectionNeonBrush

    Canvas(modifier = modifier) {
        if (lineCells.isEmpty()) return@Canvas
        val points = lineCells.map {
            Offset(
                x = it.posicion.columna * cellPx + cellPx / 2f,
                y = it.posicion.fila * cellPx + cellPx / 2f
            )
        }.toMutableList()
        if (liveFinger != null && points.isNotEmpty()) {
            points.add(liveFinger)
        }
        if (points.size == 1) {
            drawCircle(lineBrush, radius = cellPx * 0.36f, center = points.first(), alpha = 0.75f)
            return@Canvas
        }
        points.zipWithNext().forEach { (start, end) ->
            drawLine(
                brush = lineBrush,
                start = start,
                end = end,
                strokeWidth = cellPx * 0.32f,
                cap = StrokeCap.Round,
                alpha = 0.72f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.55f),
                start = start,
                end = end,
                strokeWidth = cellPx * 0.08f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun WordBadges(words: List<Palabra>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = HighStimulusSurface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "PALABRAS",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
            words.chunked(3).forEach { rowWords ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowWords.forEach { word ->
                        WordBadge(word = word, modifier = Modifier.weight(1f))
                    }
                    repeat(3 - rowWords.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun WordBadge(word: Palabra, modifier: Modifier = Modifier) {
    val color = if (word.estaEncontrada) obtenerColorPalabra(word.colorIndice) else HighStimulusSurfaceHigh
    val content = if (word.estaEncontrada) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        modifier = modifier,
        color = color,
        shape = RoundedCornerShape(8.dp),
        border = if (word.estaEncontrada) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Text(
            text = word.texto,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
            color = content,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            textDecoration = if (word.estaEncontrada) TextDecoration.LineThrough else null
        )
    }
}

@Composable
private fun ActionPanel(
    gameFinished: Boolean,
    onHint: () -> Unit,
    onGiveUp: () -> Unit,
    onClear: () -> Unit,
    onNewGame: () -> Unit,
    onDifficulty: (Dificultad) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onHint, enabled = !gameFinished, modifier = Modifier.weight(1f)) {
                Text("PISTA", fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
            OutlinedButton(onClick = onClear, enabled = !gameFinished, modifier = Modifier.weight(1f)) {
                Text("LIMPIAR", fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
            Button(
                onClick = onGiveUp,
                enabled = !gameFinished,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
            ) {
                Text("RENDIRSE", fontSize = 15.sp, fontWeight = FontWeight.Black)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Dificultad.entries.forEach { dificultad ->
                OutlinedButton(
                    onClick = { onDifficulty(dificultad) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(difficultyLabel(dificultad), fontSize = 15.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        Button(onClick = onNewGame, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
            Text("NUEVA PARTIDA", fontSize = 16.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun LoadingPanel() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = NeonCyan)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Preparando la sopa de letras...", color = FocusTextSecondary, fontSize = 17.sp)
    }
}

@Composable
private fun ErrorPanel(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "ERROR: $message",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontSize = 16.sp,
            fontWeight = FontWeight.Black
        )
    }
}

private fun gridCellSize(screenWidth: Dp, columns: Int): Dp {
    val fitSize = (screenWidth - 44.dp) / columns.toFloat()
    return fitSize.coerceIn(34.dp, 48.dp)
}

private fun cellFromOffset(offset: Offset, cellSizePx: Float, matriz: Matriz): Celda? {
    if (offset.x < 0f || offset.y < 0f) return null
    val column = floor(offset.x / cellSizePx).toInt()
    val row = floor(offset.y / cellSizePx).toInt()
    return matriz.obtenerCelda(row, column)
}

private fun buildLinearSelection(matriz: Matriz, start: Posicion, end: Posicion): List<Celda> {
    val rowDiff = end.fila - start.fila
    val colDiff = end.columna - start.columna
    if (rowDiff == 0 && colDiff == 0) return listOfNotNull(matriz.obtenerCelda(start.fila, start.columna))

    val rowStep: Int
    val colStep: Int
    val steps: Int
    when {
        rowDiff == 0 -> {
            rowStep = 0
            colStep = sign(colDiff)
            steps = abs(colDiff)
        }
        colDiff == 0 -> {
            rowStep = sign(rowDiff)
            colStep = 0
            steps = abs(rowDiff)
        }
        abs(rowDiff) == abs(colDiff) -> {
            rowStep = sign(rowDiff)
            colStep = sign(colDiff)
            steps = abs(rowDiff)
        }
        else -> {
            val dominant = snapToDominantDirection(rowDiff, colDiff)
            rowStep = dominant.rowStep
            colStep = dominant.colStep
            steps = dominant.steps
        }
    }

    return (0..steps).mapNotNull { index ->
        matriz.obtenerCelda(start.fila + rowStep * index, start.columna + colStep * index)
    }
}

private data class DominantDirection(val rowStep: Int, val colStep: Int, val steps: Int)

private fun snapToDominantDirection(rowDiff: Int, colDiff: Int): DominantDirection {
    val absRow = abs(rowDiff)
    val absCol = abs(colDiff)
    return when {
        absCol > absRow * 2 -> DominantDirection(0, sign(colDiff), absCol)
        absRow > absCol * 2 -> DominantDirection(sign(rowDiff), 0, absRow)
        else -> DominantDirection(sign(rowDiff), sign(colDiff), max(absRow, absCol))
    }
}

private fun sign(value: Int): Int = when {
    value > 0 -> 1
    value < 0 -> -1
    else -> 0
}

private fun difficultyLabel(dificultad: Dificultad): String = when (dificultad) {
    Dificultad.FACIL -> "Facil"
    Dificultad.MEDIO -> "Medio"
    Dificultad.DIFICIL -> "Dificil"
}

private fun difficultyShortLabel(dificultad: Dificultad): String = when (dificultad) {
    Dificultad.FACIL -> "F"
    Dificultad.MEDIO -> "M"
    Dificultad.DIFICIL -> "D"
}

private fun Dificultad.totalSeconds(): Int = when (this) {
    Dificultad.FACIL -> 90
    Dificultad.MEDIO -> 120
    Dificultad.DIFICIL -> 150
}

private fun calculateScore(
    totalSeconds: Int,
    remainingSeconds: Int,
    errors: Int,
    hints: Int,
    words: Int
): Int {
    val speedBonus = remainingSeconds.coerceAtLeast(0) * 8
    val base = words * 500 + totalSeconds
    val penalties = errors * 120 + hints * 80
    return (base + speedBonus - penalties).coerceAtLeast(0)
}
