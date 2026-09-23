package com.example.data.repository

import com.example.data.db.AppDao
import com.example.data.model.GameSettings
import com.example.data.model.TrackRecord
import kotlinx.coroutines.flow.Flow

class GameRepository(private val appDao: AppDao) {
    val allRecords: Flow<List<TrackRecord>> = appDao.getAllRecords()
    val settings: Flow<GameSettings?> = appDao.getSettings()

    suspend fun getRecord(trackId: String): TrackRecord? = appDao.getRecordForTrack(trackId)

    suspend fun updateScore(
        trackId: String,
        score: Int,
        combo: Int,
        stars: Int,
        completed: Boolean
    ): TrackRecord {
        val existing = appDao.getRecordForTrack(trackId) ?: TrackRecord(trackId = trackId)
        val updated = existing.copy(
            highScore = maxOf(existing.highScore, score),
            maxCombo = maxOf(existing.maxCombo, combo),
            starsEarned = maxOf(existing.starsEarned, stars),
            playCount = existing.playCount + 1,
            isCompleted = existing.isCompleted || completed,
            lastPlayedTimestamp = System.currentTimeMillis()
        )
        appDao.saveRecord(updated)
        return updated
    }

    suspend fun saveSettings(settings: GameSettings) {
        appDao.saveSettings(settings)
    }
}
