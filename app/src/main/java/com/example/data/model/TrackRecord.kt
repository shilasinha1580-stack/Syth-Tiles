package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_records")
data class TrackRecord(
    @PrimaryKey
    val trackId: String,
    val highScore: Int = 0,
    val maxCombo: Int = 0,
    val starsEarned: Int = 0,
    val playCount: Int = 0,
    val isCompleted: Boolean = false,
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)
