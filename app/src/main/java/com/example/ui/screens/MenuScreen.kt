package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.TrackCatalog
import com.example.audio.TrackDef
import com.example.data.model.GameSettings
import com.example.data.model.TrackRecord
import com.example.ui.components.HowToPlayDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SynthBackground
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PerfectGold
import com.example.ui.theme.SynthSurface
import com.example.ui.theme.SynthSurfaceBorder
import com.example.ui.theme.SynthSurfaceElevated

@Composable
fun MenuScreen(
    records: List<TrackRecord>,
    settings: GameSettings?,
    onStartTrack: (TrackDef) -> Unit,
    onSaveSettings: (speed: Float, haptics: Boolean, bgm: Float, sfx: Float) -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }
    var showHowToPlay by remember { mutableStateOf(false) }

    val recordMap = remember(records) {
        records.associateBy { it.trackId }
    }

    val totalStars = records.sumOf { it.starsEarned }
    val totalScore = records.sumOf { it.highScore }
    val completedTracks = records.count { it.isCompleted }

    val tracks = TrackCatalog.allTracks

    Box(modifier = Modifier.fillMaxSize()) {
        // Atmospheric retro synthwave backdrop
        SynthBackground(showSun = true, isFever = false)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(NeonMagenta, NeonPurple)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SYNTH TILES",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "RHYTHMIC SYNTHWAVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            ),
                            color = NeonCyan
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showHowToPlay = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SynthSurface.copy(alpha = 0.8f))
                            .border(1.dp, SynthSurfaceBorder, CircleShape)
                            .size(42.dp)
                            .testTag("how_to_play_button")
                    ) {
                        Icon(
                            Icons.Default.HelpOutline,
                            contentDescription = "How To Play",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SynthSurface.copy(alpha = 0.8f))
                            .border(1.dp, SynthSurfaceBorder, CircleShape)
                            .size(42.dp)
                            .testTag("settings_button")
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Settings",
                            tint = NeonMagenta,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Global Player Stats Bar
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SynthSurface.copy(alpha = 0.85f)),
                border = BorderStroke(1.dp, SynthSurfaceBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Total Stars
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = PerfectGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$totalStars / 12",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = Color.White
                            )
                        }
                        Text(
                            text = "STARS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB39DDB)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(SynthSurfaceBorder)
                    )

                    // Total High Score
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format("%,d", totalScore),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = NeonCyan
                        )
                        Text(
                            text = "TOTAL SCORE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB39DDB)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(SynthSurfaceBorder)
                    )

                    // Cleared Tracks
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$completedTracks / ${tracks.size}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = NeonMagenta
                        )
                        Text(
                            text = "CLEARED",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFB39DDB)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Track List Header
            Text(
                text = "SELECT SYNTH TRACK",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.5.sp
                ),
                color = Color(0xFFD4C2EB),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )

            // Scrollable Track Cards
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("track_list"),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(tracks, key = { it.id }) { track ->
                    val record = recordMap[track.id]
                    TrackCard(
                        track = track,
                        record = record,
                        onPlay = { onStartTrack(track) }
                    )
                }
            }
        }

        // Dialogs
        if (showSettings) {
            SettingsDialog(
                currentSettings = settings,
                onDismiss = { showSettings = false },
                onSaveSettings = onSaveSettings
            )
        }

        if (showHowToPlay) {
            HowToPlayDialog(onDismiss = { showHowToPlay = false })
        }
    }
}

@Composable
fun TrackCard(
    track: TrackDef,
    record: TrackRecord?,
    onPlay: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "pulse_card")
    val borderPulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_pulse"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SynthSurface.copy(alpha = 0.92f)
        ),
        border = BorderStroke(
            1.5.dp,
            track.primaryColor.copy(alpha = borderPulse)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .testTag("track_card_${track.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Row 1: Title & Difficulty Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = track.primaryColor
                    )
                }

                Surface(
                    color = track.difficulty.badgeColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, track.difficulty.badgeColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = track.difficulty.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                        color = track.difficulty.badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row 2: Chips (BPM, Key, Duration)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SynthSurfaceElevated,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${track.bpm} BPM",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD4C2EB),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = SynthSurfaceElevated,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = track.keySignature,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD4C2EB),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = SynthSurfaceElevated,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${track.durationSeconds.toInt()}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD4C2EB),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 3: Record Stats & Play Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High Score & Stars
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..3) {
                            val earned = (record?.starsEarned ?: 0) >= i
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (earned) PerfectGold else Color.Gray.copy(alpha = 0.35f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (record != null && record.highScore > 0) {
                            "Best: ${String.format("%,d", record.highScore)}"
                        } else {
                            "Not played yet"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (record != null && record.highScore > 0) Color.White else Color(0xFF8E79A8)
                    )
                }

                // Play Button
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = track.primaryColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("play_button_${track.id}")
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PLAY",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
