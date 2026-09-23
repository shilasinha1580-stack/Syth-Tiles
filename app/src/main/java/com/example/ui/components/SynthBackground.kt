package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.SynthBackground
import kotlin.random.Random

data class StarPoint(val x: Float, val y: Float, val radius: Float, val alpha: Float)

@Composable
fun SynthBackground(
    modifier: Modifier = Modifier,
    isFever: Boolean = false,
    showSun: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "synth_bg")
    val gridOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isFever) 1200 else 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grid_anim"
    )

    val pulseAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    // Cached stars
    val stars = remember {
        List(40) {
            StarPoint(
                x = Random.nextFloat(),
                y = Random.nextFloat() * 0.55f, // upper half of screen
                radius = Random.nextFloat() * 2f + 1f,
                alpha = Random.nextFloat() * 0.6f + 0.3f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val horizonY = h * 0.48f

        // 1. Deep Space Sky Gradient
        val skyColors = if (isFever) {
            listOf(Color(0xFF1E002B), Color(0xFF380036), Color(0xFF0F0022))
        } else {
            listOf(SynthBackground, Color(0xFF150228), Color(0xFF220538))
        }
        drawRect(
            brush = Brush.verticalGradient(
                colors = skyColors,
                startY = 0f,
                endY = horizonY
            ),
            size = Size(w, horizonY)
        )

        // 2. Stars
        stars.forEach { star ->
            drawCircle(
                color = Color.White.copy(alpha = star.alpha * pulseAlpha),
                radius = star.radius,
                center = Offset(star.x * w, star.y * h)
            )
        }

        // 3. Glowing Retro Sun
        if (showSun) {
            val sunRadius = w * 0.22f
            val sunCenter = Offset(w * 0.5f, horizonY - sunRadius * 0.15f)

            // Outer sun glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonMagenta.copy(alpha = 0.4f),
                        NeonPurple.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = sunCenter,
                    radius = sunRadius * 1.8f
                ),
                radius = sunRadius * 1.8f,
                center = sunCenter
            )

            // Sun body with warm retro gradient
            val sunGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFEE55),
                    Color(0xFFFF7700),
                    NeonMagenta
                ),
                startY = sunCenter.y - sunRadius,
                endY = sunCenter.y + sunRadius
            )
            drawCircle(
                brush = sunGradient,
                radius = sunRadius,
                center = sunCenter
            )

            // Sun horizontal scanline blinds
            val blindCount = 8
            for (i in 0 until blindCount) {
                val lineY = sunCenter.y + (sunRadius * (i.toFloat() / blindCount))
                val lineH = (i + 1) * 1.6f
                drawRect(
                    color = SynthBackground.copy(alpha = 0.85f),
                    topLeft = Offset(sunCenter.x - sunRadius, lineY),
                    size = Size(sunRadius * 2f, lineH)
                )
            }
        }

        // 4. Horizon Glow Beam
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    (if (isFever) NeonMagenta else NeonCyan).copy(alpha = 0.5f),
                    Color.Transparent
                ),
                startY = horizonY - 12f,
                endY = horizonY + 36f
            ),
            topLeft = Offset(0f, horizonY - 12f),
            size = Size(w, 48f)
        )

        // 5. 3D Perspective Retro Grid Ground
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF140224),
                    Color(0xFF0A0012)
                ),
                startY = horizonY,
                endY = h
            ),
            topLeft = Offset(0f, horizonY),
            size = Size(w, h - horizonY)
        )

        val gridLineColor = if (isFever) NeonMagenta.copy(alpha = 0.45f) else NeonCyan.copy(alpha = 0.28f)

        // Horizontal perspective lines with exponential spacing
        val horizLines = 14
        for (i in 0..horizLines) {
            val progress = ((i.toFloat() + gridOffset) / horizLines).coerceIn(0f, 1f)
            val curvedProgress = progress * progress // exponential depth
            val y = horizonY + (h - horizonY) * curvedProgress
            val alpha = (curvedProgress * 0.5f + 0.1f).coerceIn(0f, 0.6f)
            drawLine(
                color = gridLineColor.copy(alpha = alpha),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = (curvedProgress * 2.2f + 0.8f)
            )
        }

        // Vanishing Perspective Radial Lines
        val vp = Offset(w * 0.5f, horizonY)
        val numRadial = 16
        for (i in -numRadial..numRadial) {
            val bottomX = w * 0.5f + (i * (w / (numRadial * 0.45f)))
            drawLine(
                color = gridLineColor.copy(alpha = 0.22f),
                start = vp,
                end = Offset(bottomX, h),
                strokeWidth = 1.2f
            )
        }

        // 6. Fever Mode Border Flare
        if (isFever) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, NeonMagenta.copy(alpha = 0.25f)),
                    center = Offset(w / 2, h / 2),
                    radius = w * 0.8f
                ),
                size = Size(w, h)
            )
        }
    }
}
