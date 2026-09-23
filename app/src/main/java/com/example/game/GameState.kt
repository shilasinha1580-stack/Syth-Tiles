package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.audio.TrackDef
import com.example.ui.theme.GreatCyan
import com.example.ui.theme.MissRed
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.PerfectGold

enum class GamePhase {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER,
    VICTORY
}

enum class HitRating(val label: String, val color: Color, val score: Int) {
    PERFECT("PERFECT!", PerfectGold, 500),
    GREAT("GREAT!", GreatCyan, 300),
    GOOD("GOOD", NeonAmber, 150),
    MISS("MISS", MissRed, 0)
}

data class ActiveTileState(
    val id: Int,
    val lane: Int,
    val timeMs: Long,
    val lengthMs: Long,
    val pitchHz: Float,
    val isBonus: Boolean,
    var isHit: Boolean = false,
    var isHolding: Boolean = false,
    var holdProgress: Float = 0f, // 0f to 1f
    var missed: Boolean = false
)

data class HitFeedback(
    val id: Long,
    val rating: HitRating,
    val combo: Int,
    val lane: Int,
    val timeCreated: Long = System.currentTimeMillis()
)

data class Particle(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float = 1.0f,
    val color: Color,
    val size: Float
)

data class LaneFlash(
    val lane: Int,
    val color: Color,
    val startTime: Long
)
