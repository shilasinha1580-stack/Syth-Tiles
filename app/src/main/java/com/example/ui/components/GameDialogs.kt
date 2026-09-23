package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.GameSettings
import com.example.game.GameUiState
import com.example.ui.theme.GreatCyan
import com.example.ui.theme.MissRed
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PerfectGold
import com.example.ui.theme.SynthSurface
import com.example.ui.theme.SynthSurfaceBorder
import com.example.ui.theme.SynthSurfaceElevated

@Composable
fun SettingsDialog(
    currentSettings: GameSettings?,
    onDismiss: () -> Unit,
    onSaveSettings: (speed: Float, haptics: Boolean, bgm: Float, sfx: Float) -> Unit
) {
    var speed by remember { mutableFloatStateOf(currentSettings?.speedMultiplier ?: 1.0f) }
    var haptics by remember { mutableStateOf(currentSettings?.hapticsEnabled ?: true) }
    var bgm by remember { mutableFloatStateOf(currentSettings?.bgmVolume ?: 0.85f) }
    var sfx by remember { mutableFloatStateOf(currentSettings?.sfxVolume ?: 0.95f) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SynthSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, NeonCyan, RoundedCornerShape(20.dp))
                .padding(4.dp)
                .testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AUDIO & CONTROLS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = NeonCyan
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tile Fall Speed
                Text(
                    text = "TILE FALL SPEED: ${String.format("%.2fx", speed)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFD4C2EB),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1.0f, 1.25f, 1.5f, 2.0f).forEach { s ->
                        val isSelected = speed == s
                        Button(
                            onClick = { speed = s },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("speed_option_${s}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) NeonCyan else SynthSurfaceElevated,
                                contentColor = if (isSelected) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${s}x",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // BGM Volume Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Music Volume", tint = NeonMagenta)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MUSIC (BGM): ${(bgm * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
                Slider(
                    value = bgm,
                    onValueChange = { bgm = it },
                    colors = SliderDefaults.colors(
                        thumbColor = NeonMagenta,
                        activeTrackColor = NeonMagenta,
                        inactiveTrackColor = SynthSurfaceBorder
                    ),
                    modifier = Modifier.testTag("bgm_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // SFX Volume Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "SFX Volume", tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SYNTH HITS (SFX): ${(sfx * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
                Slider(
                    value = sfx,
                    onValueChange = { sfx = it },
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan,
                        inactiveTrackColor = SynthSurfaceBorder
                    ),
                    modifier = Modifier.testTag("sfx_slider")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Haptic Feedback Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = "Haptics", tint = NeonAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HAPTIC FEEDBACK",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                    Switch(
                        checked = haptics,
                        onCheckedChange = { haptics = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonAmber,
                            checkedTrackColor = Color(0xFF5A3B00)
                        ),
                        modifier = Modifier.testTag("haptics_switch")
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Save Button
                Button(
                    onClick = {
                        onSaveSettings(speed, haptics, bgm, sfx)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_settings_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "APPLY SETTINGS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HowToPlayDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SynthSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, NeonMagenta, RoundedCornerShape(20.dp))
                .padding(4.dp)
                .testTag("how_to_play_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HOW TO PLAY",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        ),
                        color = NeonMagenta
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_how_to_play_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rule 1: Tap in sync
                TileExplainerRow(
                    badgeText = "TAP",
                    badgeColor = NeonCyan,
                    title = "Tap Falling Tiles",
                    description = "Tap each tile as it reaches the glowing bottom receptor line to trigger rich synthwave lead notes in sync with the beat."
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rule 2: Hold Tiles
                TileExplainerRow(
                    badgeText = "HOLD",
                    badgeColor = NeonPurple,
                    title = "Elongated Hold Beams",
                    description = "Press and hold laser beam tiles until the tail passes for sustained synth chords and bonus multiplier points."
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rule 3: Golden Star Bonus
                TileExplainerRow(
                    badgeText = "STAR",
                    badgeColor = PerfectGold,
                    title = "Golden Bonus Tiles",
                    description = "Hit golden star tiles to instantly earn +1000 points and a massive boost to your Fever gauge!"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rule 4: Hyper Fever Mode
                TileExplainerRow(
                    badgeText = "FEVER",
                    badgeColor = NeonMagenta,
                    title = "Hyper Fever 2X",
                    description = "Fill your Fever meter to unleash Hyper Fever mode: double score multiplier and electric particle storms!"
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("got_it_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "LET'S GROOVE!", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun TileExplainerRow(
    badgeText: String,
    badgeColor: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor.copy(alpha = 0.2f))
                .border(1.5.dp, badgeColor, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badgeText,
                color = badgeColor,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFD4C2EB),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onQuit: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SynthSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, NeonCyan, RoundedCornerShape(20.dp))
                .padding(4.dp)
                .testTag("pause_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GAME PAUSED",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = NeonCyan
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("resume_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "RESUME", fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("restart_button"),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(NeonMagenta, NeonPurple))),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = NeonMagenta)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "RESTART TRACK", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onQuit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("quit_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "TRACK SELECT", color = Color(0xFFD4C2EB))
                }
            }
        }
    }
}

@Composable
fun ResultDialog(
    state: GameUiState,
    isVictory: Boolean,
    onPlayAgain: () -> Unit,
    onTrackSelect: () -> Unit,
    onRevive: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_high_score")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val totalNotes = state.selectedTrack.tiles.size
    val hitNotes = state.perfectCount + state.greatCount + state.goodCount
    val accuracy = if (totalNotes > 0) ((hitNotes.toFloat() / totalNotes) * 100f).toInt() else 0

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SynthSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    2.dp,
                    if (isVictory) NeonCyan else MissRed,
                    RoundedCornerShape(24.dp)
                )
                .padding(4.dp)
                .testTag("result_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Banner
                Text(
                    text = if (isVictory) "TRACK CLEARED!" else "OVERDRIVE FAILED",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = if (isVictory) NeonCyan else MissRed,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = state.selectedTrack.title.uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                    color = Color(0xFFD4C2EB),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stars Display
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val earned = i <= state.starsEarned
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star $i",
                            tint = if (earned) PerfectGold else Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(if (i == 2) 44.dp else 34.dp)
                                .padding(horizontal = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Final Score
                Text(
                    text = String.format("%,d", state.score),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    ),
                    color = Color.White
                )

                if (state.isNewHighScore) {
                    Surface(
                        color = NeonMagenta.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonMagenta),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text(
                            text = "NEW HIGH SCORE!",
                            color = NeonMagenta,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Detailed Statistics Grid
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SynthSurfaceElevated),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "ACCURACY", color = Color(0xFFD4C2EB), fontSize = 12.sp)
                            Text(text = "$accuracy%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "MAX COMBO", color = Color(0xFFD4C2EB), fontSize = 12.sp)
                            Text(text = "${state.maxCombo}x", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "PERFECT / GREAT", color = Color(0xFFD4C2EB), fontSize = 12.sp)
                            Text(text = "${state.perfectCount} / ${state.greatCount}", color = PerfectGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "GOOD / MISS", color = Color(0xFFD4C2EB), fontSize = 12.sp)
                            Text(text = "${state.goodCount} / ${state.missCount}", color = MissRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Revive Option (if failed)
                if (!isVictory && onRevive != null) {
                    Button(
                        onClick = onRevive,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("revive_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "REVIVE (1 EXTRA LIFE)", fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Play Again Button
                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("play_again_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "PLAY AGAIN", fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Track List Button
                OutlinedButton(
                    onClick = onTrackSelect,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("track_list_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "TRACK SELECT", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
