package com.miempresa.sopaletras.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.miempresa.sopaletras.domain.model.Celda
import com.miempresa.sopaletras.presentation.theme.AlertRed
import com.miempresa.sopaletras.presentation.theme.HighStimulusSurfaceHigh
import com.miempresa.sopaletras.presentation.theme.NeonCyan
import com.miempresa.sopaletras.presentation.theme.obtenerColorPalabra

fun Modifier.shake(offset: Float): Modifier = graphicsLayer {
    translationX = offset
}

@Composable
fun rememberShakeOffset(shakeKey: Int, distance: Float = 18f): Float {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(shakeKey) {
        if (shakeKey > 0) {
            repeat(4) {
                offset.animateTo(distance, tween(38))
                offset.animateTo(-distance, tween(38))
            }
            offset.animateTo(0f, tween(50))
        }
    }
    return offset.value
}

@Composable
fun LetterCell(
    celda: Celda,
    selected: Boolean,
    invalidShakeKey: Int,
    size: Dp,
    fontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (selected || invalidShakeKey > 0) 1.16f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "letterScale"
    )
    val shakeOffset = rememberShakeOffset(invalidShakeKey)
    val isInvalidFlash = invalidShakeKey > 0
    val backgroundTarget = when {
        isInvalidFlash -> AlertRed
        celda.perteneceAPalabraEncontrada -> obtenerColorPalabra(celda.colorIndice ?: 0)
        selected -> NeonCyan
        else -> Color.White
    }
    val textTarget = if (selected || celda.perteneceAPalabraEncontrada || isInvalidFlash) {
        Color.Black
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val background by animateColorAsState(backgroundTarget, tween(120), label = "cellBackground")
    val textColor by animateColorAsState(textTarget, tween(120), label = "cellText")

    Box(
        modifier = modifier
            .shake(if (selected || isInvalidFlash) shakeOffset else 0f)
            .size(size)
            .padding(1.dp)
            .scale(scale)
            .background(background, RoundedCornerShape(6.dp))
            .border(
                width = if (selected) 2.dp else 1.5.dp,
                color = if (selected) Color.White else Color(0xFF9FB1D1),
                shape = RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = celda.letra.toString(),
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }
}
