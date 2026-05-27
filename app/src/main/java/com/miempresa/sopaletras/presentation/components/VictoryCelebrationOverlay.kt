package com.miempresa.sopaletras.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.miempresa.sopaletras.presentation.theme.AlertOrange
import com.miempresa.sopaletras.presentation.theme.HighStimulusBackground
import com.miempresa.sopaletras.presentation.theme.NeonCyan
import com.miempresa.sopaletras.presentation.theme.NeonPink
import com.miempresa.sopaletras.presentation.theme.SuccessGreen
import com.miempresa.sopaletras.presentation.theme.VictoryGold
import com.miempresa.sopaletras.presentation.theme.VictoryGoldLight
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

private data class ConfettiParticle(
    val xFactor: Float,
    val yOffset: Float,
    val size: Float,
    val speed: Float,
    val rotationSpeed: Float,
    val color: Color
)

private val VictoryBounceEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

@Composable
fun VictoryCelebrationOverlay(
    visible: Boolean,
    score: Int,
    combo: Int,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val cardScale = remember { Animatable(0.72f) }
    val particles = remember {
        mutableStateListOf<ConfettiParticle>().apply {
            val palette = listOf(VictoryGold, VictoryGoldLight, NeonCyan, NeonPink, SuccessGreen, AlertOrange)
            repeat(96) {
                add(
                    ConfettiParticle(
                        xFactor = Random.nextFloat(),
                        yOffset = Random.nextFloat() * -900f,
                        size = Random.nextFloat() * 11f + 7f,
                        speed = Random.nextFloat() * 420f + 220f,
                        rotationSpeed = Random.nextFloat() * 360f + 80f,
                        color = palette.random()
                    )
                )
            }
        }
    }
    val transition = rememberInfiniteTransition(label = "victoryLoop")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600), RepeatMode.Restart),
        label = "confettiProgress"
    )
    val shimmer by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(480), RepeatMode.Reverse),
        label = "goldShimmer"
    )

    LaunchedEffect(visible, combo) {
        if (visible) {
            cardScale.snapTo(0.72f)
            cardScale.animateTo(1f, tween(650, easing = VictoryBounceEasing))
            repeat(combo.coerceIn(1, 4)) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(90)
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(140)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HighStimulusBackground.copy(alpha = 0.92f)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                particles.forEachIndexed { index, particle ->
                    val fall = (particle.yOffset + progress * particle.speed * 3.2f) % (size.height + 120f)
                    val x = particle.xFactor * size.width + sin(progress * 8f + index) * 22f
                    rotate(
                        degrees = progress * particle.rotationSpeed,
                        pivot = Offset(x, fall)
                    ) {
                        drawRect(
                            color = particle.color,
                            topLeft = Offset(x, fall),
                            size = androidx.compose.ui.geometry.Size(
                                particle.size,
                                particle.size * 0.58f
                            ),
                            alpha = 0.92f
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .scale(cardScale.value),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "VICTORIA",
                        color = VictoryGold.copy(alpha = shimmer),
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("Puntaje ")
                            withStyle(SpanStyle(color = VictoryGoldLight, fontWeight = FontWeight.Black)) {
                                append(score.toString())
                            }
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Combo x$combo",
                        color = NeonCyan,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Button(onClick = onNewGame, modifier = Modifier.fillMaxWidth()) {
                        Text("JUGAR DE NUEVO", fontSize = 16.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
