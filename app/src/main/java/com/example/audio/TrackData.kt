package com.example.audio

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta

enum class TrackDifficulty(val label: String, val badgeColor: Color) {
    NORMAL("NORMAL", NeonCyan),
    HARD("HARD", NeonAmber),
    EXPERT("EXPERT", NeonMagenta),
    MASTER("MASTER", Color(0xFFB026FF))
}

data class TileNote(
    val id: Int,
    val timeMs: Long,
    val lane: Int, // 0..3
    val lengthMs: Long = 0L, // 0 for regular tap tile, > 0 for hold tile
    val pitchHz: Float = 440f,
    val noteName: String = "A4",
    val isBonus: Boolean = false
)

data class TrackDef(
    val id: String,
    val title: String,
    val subtitle: String,
    val bpm: Int,
    val keySignature: String,
    val difficulty: TrackDifficulty,
    val durationSeconds: Float,
    val primaryColor: Color,
    val secondaryColor: Color,
    val tiles: List<TileNote>,
    val chordProgression: List<String> // Chord names per 4-beat bar
)
