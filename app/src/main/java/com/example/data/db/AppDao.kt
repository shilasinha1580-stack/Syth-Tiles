package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.GameSettings
import com.example.data.model.TrackRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM track_records")
    fun getAllRecords(): Flow<List<TrackRecord>>

    @Query("SELECT * FROM track_records WHERE trackId = :trackId")
    suspend fun getRecordForTrack(trackId: String): TrackRecord?

    @Query("SELECT * FROM track_records WHERE trackId = :trackId")
    fun observeRecordForTrack(trackId: String): Flow<TrackRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRecord(record: TrackRecord)

    @Query("SELECT * FROM game_settings WHERE id = 1")
    fun getSettings(): Flow<GameSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: GameSettings)
}
