package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.TrackDef
import com.example.game.ActiveTileState
import com.example.game.GamePhase
import com.example.game.GameUiState
import com.example.game.HitFeedback
import com.example.game.LaneFlash
import com.example.game.Particle
import com.example.ui.components.PauseDialog
import com.example.ui.components.ResultDialog
import com.example.ui.components.SynthBackground
import com.example.ui.theme.GreatCyan
import com.example.ui.theme.MissRed
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PerfectGold
import com.example.ui.theme.SynthSurface
import com.example.ui.theme.SynthSurfaceBorder
import com.example.ui.theme.SynthSurfaceElevated
import kotlinx.coroutines.isActive

@Composable
fun GameScreen(
    state: GameUiState,
    tiles: List<ActiveTileState>,
    particles: List<Particle>,
    speedMultiplier: Float = 1.0f,
    onGameTick: (deltaMs: Long) -> Unit,
    onLaneDown: (lane: Int, screenWidthPx: Float, hitYPx: Float) -> Unit,
    onLaneUp: (lane: Int) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onQuitToMenu: () -> Unit,
    onRevive: () -> Unit
) {
    // 60-120 FPS high-precision game loop
    LaunchedEffect(state.phase) {
        if (state.phase == GamePhase.PLAYING) {
            var lastNano = System.nanoTime()
            while (isActive && state.phase == GamePhase.PLAYING) {
                withFrameNanos { currentNano ->
                    val deltaMs = ((currentNano - lastNano) / 1_000_000L).coerceIn(1L, 45L)
                    lastNano = currentNano
                    onGameTick(deltaMs)
                }
            }
        }
    }

    val track = state.selectedTrack
    val fallDurationMs = (1750f / speedMultiplier).toLong()

    val feverTransition = rememberInfiniteTransition(label = "fever_anim")
    val feverGlowAlpha by feverTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fever_glow"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .testTag("game_screen")
    ) {
        val density = LocalDensity.current
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()
        val hitYPx = screenHeightPx * 0.80f // 80% down the screen
        val laneWidthPx = screenWidthPx / 4f

        // 1. Synthwave Background Canvas
        SynthBackground(
            isFever = state.isFeverActive,
            showSun = false
        )

        // 2. 4 Vertical Lanes & Falling Tiles Rendering Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val h = size.height
            val w = size.width
            val hitY = hitYPx
            val laneW = laneWidthPx

            // Lane Divider Lines
            for (i in 1..3) {
                val x = i * laneW
                drawLine(
                    color = Color.White.copy(alpha = 0.12f),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1.2f
                )
            }

            // Lane Hit Flashes
            state.laneFlashes.forEach { flash ->
                val elapsed = System.currentTimeMillis() - flash.startTime
                val alpha = (1f - (elapsed / 250f)).coerceIn(0f, 1f)
                val left = flash.lane * laneW
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            flash.color.copy(alpha = alpha * 0.45f),
                            flash.color.copy(alpha = alpha * 0.8f)
                        ),
                        startY = hitY - 260f,
                        endY = h
                    ),
                    topLeft = Offset(left, hitY - 260f),
                    size = Size(laneW, h - (hitY - 260f))
                )
            }

            // Hit Zone Target Line (Glowing Cyan Beam)
            val hitLineColor = if (state.isFeverActive) NeonMagenta else NeonCyan
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        hitLineColor.copy(alpha = 0.4f),
                        hitLineColor,
                        hitLineColor,
                        hitLineColor.copy(alpha = 0.4f)
                    )
                ),
                start = Offset(0f, hitY),
                end = Offset(w, hitY),
                strokeWidth = 3.5f
            )

            // Hit Zone Target Receptors (Glowing Pads for each lane)
            for (lane in 0..3) {
                val padCenter = Offset(lane * laneW + laneW / 2f, hitY)
                drawCircle(
                    color = hitLineColor.copy(alpha = 0.2f),
                    radius = laneW * 0.35f,
                    center = padCenter
                )
                drawCircle(
                    color = hitLineColor.copy(alpha = 0.8f),
                    radius = laneW * 0.35f,
                    center = padCenter,
                    style = Stroke(width = 2f)
                )
            }

            // Draw Falling Tiles
            val currentTime = state.gameTimeMs
            val tilePadding = laneW * 0.07f
            val tileW = laneW - tilePadding * 2f

            for (tile in tiles) {
                if (tile.isHit && tile.lengthMs == 0L) continue
                if (tile.missed) continue

                val timeRemaining = tile.timeMs - currentTime
                // Skip if far above screen or far below
                if (timeRemaining > fallDurationMs + 200 || timeRemaining < -400) continue

                // y position of the tile's strike line
                val tileY = hitY - (timeRemaining.toFloat() / fallDurationMs) * hitY
                val tileX = tile.lane * laneW + tilePadding

                val tileColor = when {
                    tile.isBonus -> PerfectGold
                    state.isFeverActive -> NeonMagenta
                    tile.lane == 0 -> NeonCyan
                    tile.lane == 1 -> NeonMagenta
                    tile.lane == 2 -> NeonPurple
                    else -> NeonBlue
                }

                if (tile.lengthMs > 0L) {
                    // Long / Hold Tile
                    val holdLengthPx = (tile.lengthMs.toFloat() / fallDurationMs) * hitY
                    val topY = tileY - holdLengthPx

                    // Laser Beam Body
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                tileColor.copy(alpha = 0.35f),
                                tileColor.copy(alpha = 0.85f)
                            ),
                            startY = topY,
                            endY = tileY
                        ),
                        topLeft = Offset(tileX + tileW * 0.18f, topY),
                        size = Size(tileW * 0.64f, holdLengthPx),
                        cornerRadius = CornerRadius(14f, 14f)
                    )

                    // Outer laser beam border
                    drawRoundRect(
                        color = tileColor,
                        topLeft = Offset(tileX + tileW * 0.18f, topY),
                        size = Size(tileW * 0.64f, holdLengthPx),
                        cornerRadius = CornerRadius(14f, 14f),
                        style = Stroke(width = 2f)
                    )

                    // Head Pad
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White, tileColor),
                            startY = tileY - 24f,
                            endY = tileY + 24f
                        ),
                        topLeft = Offset(tileX, tileY - 20f),
                        size = Size(tileW, 40f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                } else {
                    // Standard Tap Tile
                    val tileH = laneW * 0.68f

                    // Outer Glow / Card Body
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                tileColor.copy(alpha = 0.95f),
                                tileColor.copy(alpha = 0.65f)
                            ),
                            startY = tileY - tileH / 2f,
                            endY = tileY + tileH / 2f
                        ),
                        topLeft = Offset(tileX, tileY - tileH / 2f),
                        size = Size(tileW, tileH),
                        cornerRadius = CornerRadius(14f, 14f)
                    )

                    // Neon Highlight Border
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(tileX, tileY - tileH / 2f),
                        size = Size(tileW, tileH),
                        cornerRadius = CornerRadius(14f, 14f),
                        style = Stroke(width = 2f)
                    )

                    // Inner decorative neon stripe or star
                    if (tile.isBonus) {
                        // Golden star accent
                        drawCircle(
                            color = Color.White,
                            radius = 12f,
                            center = Offset(tileX + tileW / 2f, tileY)
                        )
                    } else {
                        drawLine(
                            color = Color.White.copy(alpha = 0.8f),
                            start = Offset(tileX + tileW * 0.25f, tileY),
                            end = Offset(tileX + tileW * 0.75f, tileY),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Draw Neon Particles
            for (p in particles) {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                    radius = p.size,
                    center = Offset(p.x, p.y)
                )
            }
        }

        // 3. Multi-Touch Detection Columns across 4 Lanes
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 20.dp)
        ) {
            for (lane in 0..3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(lane) {
                            detectTapGestures(
                                onPress = {
                                    onLaneDown(lane, screenWidthPx, hitYPx)
                                    tryAwaitRelease()
                                    onLaneUp(lane)
                                }
                            )
                        }
                        .testTag("lane_touch_zone_$lane")
                )
            }
        }

        // 4. Floating Judgments & Feedback Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(bottom = 120.dp),
            contentAlignment = Alignment.Center
        ) {
            state.hitFeedbacks.lastOrNull()?.let { feedback ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = feedback.rating.label,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = feedback.rating.color
                    )

                    if (feedback.combo >= 2) {
                        Text(
                            text = "${feedback.combo} COMBO!",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = if (state.isFeverActive) NeonMagenta else NeonCyan
                        )
                    }
                }
            }
        }

        // 5. Top Game HUD Bar & Progress
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            // Track Progress Bar with star checkpoints
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(SynthSurfaceBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = state.songProgress)
                        .background(
                            Brush.horizontalGradient(
                                listOf(NeonCyan, NeonMagenta, PerfectGold)
                            )
                        )
                )
            }

            // Top HUD Row: Track Info, Shields, Score, Pause
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Track Info & Shields
                Column {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..state.maxShields) {
                            val active = i <= state.shields
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Shield $i",
                                tint = if (active) NeonMagenta else Color.Gray.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 3.dp)
                            )
                        }
                    }
                }

                // Score Display
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%,d", state.score),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                    if (state.combo > 1) {
                        Text(
                            text = "${state.combo}x COMBO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = NeonCyan
                        )
                    }
                }

                // Pause Button
                IconButton(
                    onClick = onPause,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SynthSurface.copy(alpha = 0.85f))
                        .border(1.dp, SynthSurfaceBorder, CircleShape)
                        .size(42.dp)
                        .testTag("pause_game_button")
                ) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "Pause Game",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Fever Meter Indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                if (state.isFeverActive) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .border(1.5.dp, NeonMagenta, RoundedCornerShape(8.dp)),
                        color = NeonMagenta.copy(alpha = feverGlowAlpha * 0.4f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = PerfectGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HYPER FEVER 2X ACTIVATED!",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                } else {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "FEVER GAUGE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB39DDB)
                            )
                            Text(
                                text = "${state.feverCharge.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SynthSurfaceElevated)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction = state.feverCharge / 100f)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(NeonCyan, NeonMagenta)
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }

        // 6. Bottom Touch Receptor Visual Labels (Lanes 1, 2, 3, 4)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
        ) {
            listOf("LANE 1", "LANE 2", "LANE 3", "LANE 4").forEachIndexed { index, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White.copy(alpha = 0.25f)
                    )
                }
            }
        }

        // 7. Modals: Pause and Game Over / Victory
        if (state.phase == GamePhase.PAUSED) {
            PauseDialog(
                onResume = onResume,
                onRestart = onRestart,
                onQuit = onQuitToMenu
            )
        }

        if (state.phase == GamePhase.VICTORY) {
            ResultDialog(
                state = state,
                isVictory = true,
                onPlayAgain = onRestart,
                onTrackSelect = onQuitToMenu
            )
        }

        if (state.phase == GamePhase.GAME_OVER) {
            ResultDialog(
                state = state,
                isVictory = false,
                onPlayAgain = onRestart,
                onTrackSelect = onQuitToMenu,
                onRevive = onRevive
            )
        }
    }
}
