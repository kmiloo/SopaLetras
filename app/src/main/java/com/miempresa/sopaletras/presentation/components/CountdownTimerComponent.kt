package com.miempresa.sopaletras.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miempresa.sopaletras.presentation.theme.AlertRed
import com.miempresa.sopaletras.presentation.theme.AlertYellow
import com.miempresa.sopaletras.presentation.theme.HighStimulusSurfaceHigh
import com.miempresa.sopaletras.presentation.theme.SuccessGreen

@Composable
fun CountdownTimerComponent(
    remainingSeconds: Int,
    totalSeconds: Int,
    modifier: Modifier = Modifier
) {
    val progress = (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    val isCritical = remainingSeconds <= 10
    val timerColorTarget = when {
        isCritical -> AlertRed
        progress <= 0.35f -> AlertYellow
        else -> SuccessGreen
    }
    val timerColor by animateColorAsState(
        targetValue = timerColorTarget,
        animationSpec = tween(180),
        label = "timerColor"
    )
    val pulse = rememberInfiniteTransition(label = "timerPulse")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = if (isCritical) 1.1f else 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isCritical) 260 else 720),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timerScale"
    )
    val alpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = if (isCritical) 1f else 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isCritical) 180 else 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timerAlpha"
    )

    Column(
        modifier = modifier
            .scale(scale)
            .background(HighStimulusSurfaceHigh, RoundedCornerShape(8.dp))
            .border(1.dp, timerColor.copy(alpha = alpha), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TIEMPO LIMITE",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = formatCountdown(remainingSeconds),
                color = timerColor,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = timerColor,
            trackColor = Color.White.copy(alpha = 0.12f)
        )
    }
}

private fun formatCountdown(seconds: Int): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    val minutes = safeSeconds / 60
    val rest = safeSeconds % 60
    return "%02d:%02d".format(minutes, rest)
}
