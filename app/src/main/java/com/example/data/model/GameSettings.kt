package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_settings")
data class GameSettings(
    @PrimaryKey
    val id: Int = 1,
    val speedMultiplier: Float = 1.0f,
    val hapticsEnabled: Boolean = true,
    val bgmVolume: Float = 0.85f,
    val sfxVolume: Float = 0.95f
)
