package com.example.audio

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple

object TrackCatalog {

    // Standard Note Frequencies (Hz)
    const val NOTE_C4 = 261.63f
    const val NOTE_D4 = 293.66f
    const val NOTE_E4 = 329.63f
    const val NOTE_F4 = 349.23f
    const val NOTE_G4 = 392.00f
    const val NOTE_A4 = 440.00f
    const val NOTE_B4 = 493.88f
    const val NOTE_C5 = 523.25f
    const val NOTE_D5 = 587.33f
    const val NOTE_E5 = 659.25f
    const val NOTE_F5 = 698.46f
    const val NOTE_G5 = 783.99f
    const val NOTE_A5 = 880.00f
    const val NOTE_B5 = 987.77f
    const val NOTE_C6 = 1046.50f

    val allTracks: List<TrackDef> by lazy {
        listOf(
            createNeonHorizon(),
            createCyberDrive(),
            createMidnightOdyssey(),
            createStarlight1984()
        )
    }

    fun getTrackById(id: String): TrackDef {
        return allTracks.firstOrNull { it.id == id } ?: allTracks[0]
    }

    private fun createNeonHorizon(): TrackDef {
        val bpm = 110
        val beatMs = (60_000.0 / bpm).toLong()
        val tiles = mutableListOf<TileNote>()
        var idCounter = 1

        // Scale: A Minor (A4, C5, D5, E5, G5, A5)
        val melodyNotes = listOf(
            Pair(NOTE_A4, "A4"),
            Pair(NOTE_C5, "C5"),
            Pair(NOTE_D5, "D5"),
            Pair(NOTE_E5, "E5"),
            Pair(NOTE_G5, "G5"),
            Pair(NOTE_A5, "A5")
        )

        // 32 bars of music ~ 43.6 seconds (lead-in delay 2000ms)
        val startTime = 2200L
        val totalBars = 20

        // Phrasing patterns for lanes (0..3)
        val lanePatterns = listOf(
            listOf(0, 1, 2, 3, 2, 1, 0, 2),
            listOf(1, 2, 3, 1, 0, 2, 3, 2),
            listOf(0, 2, 1, 3, 2, 0, 1, 3),
            listOf(3, 2, 1, 0, 1, 2, 3, 1)
        )

        for (bar in 0 until totalBars) {
            val barStart = startTime + bar * (beatMs * 4)
            val pattern = lanePatterns[bar % lanePatterns.size]

            // Beats 1, 2, 3, 4 with syncopated 8th notes
            for (step in 0..7) {
                // Intro is lighter, drops get denser
                val shouldSpawn = when {
                    bar < 2 -> step % 2 == 0 // simple quarter notes in intro
                    bar in 2..9 -> step % 2 == 0 || (step == 3 || step == 7)
                    bar in 10..15 -> true // fast driving section
                    else -> step % 2 == 0 // outro
                }

                if (shouldSpawn) {
                    val time = barStart + step * (beatMs / 2)
                    val lane = pattern[step % pattern.size]
                    val note = melodyNotes[(lane + (bar % 3)) % melodyNotes.size]
                    val isHold = (bar in 4..15 && step == 0 && bar % 2 == 1)
                    val isBonus = (bar == 8 || bar == 14) && step == 4

                    tiles.add(
                        TileNote(
                            id = idCounter++,
                            timeMs = time,
                            lane = lane,
                            lengthMs = if (isHold) beatMs else 0L,
                            pitchHz = note.first,
                            noteName = note.second,
                            isBonus = isBonus
                        )
                    )
                }
            }
        }

        return TrackDef(
            id = "neon_horizon",
            title = "Neon Horizon",
            subtitle = "Sunset Outrun Groove",
            bpm = bpm,
            keySignature = "A Minor",
            difficulty = TrackDifficulty.NORMAL,
            durationSeconds = 48f,
            primaryColor = NeonCyan,
            secondaryColor = NeonMagenta,
            tiles = tiles,
            chordProgression = listOf("Am", "F", "C", "G", "Am", "Dm", "Em", "Am")
        )
    }

    private fun createCyberDrive(): TrackDef {
        val bpm = 124
        val beatMs = (60_000.0 / bpm).toLong()
        val tiles = mutableListOf<TileNote>()
        var idCounter = 1000

        // Scale: D Minor (D4, F4, G4, A4, C5, D5)
        val melodyNotes = listOf(
            Pair(NOTE_D4, "D4"),
            Pair(NOTE_F4, "F4"),
            Pair(NOTE_G4, "G4"),
            Pair(NOTE_A4, "A4"),
            Pair(NOTE_C5, "C5"),
            Pair(NOTE_D5, "D5")
        )

        val startTime = 2000L
        val totalBars = 22

        val lanePatterns = listOf(
            listOf(1, 0, 2, 3, 1, 3, 2, 0),
            listOf(0, 3, 1, 2, 0, 2, 1, 3),
            listOf(2, 1, 3, 0, 2, 0, 3, 1),
            listOf(3, 1, 0, 2, 3, 2, 1, 0)
        )

        for (bar in 0 until totalBars) {
            val barStart = startTime + bar * (beatMs * 4)
            val pattern = lanePatterns[bar % lanePatterns.size]

            for (step in 0..7) {
                val shouldSpawn = when {
                    bar < 2 -> step % 2 == 0
                    bar in 2..7 -> step != 5
                    bar in 8..17 -> true // intense highway chase
                    else -> step % 2 == 0
                }

                if (shouldSpawn) {
                    val time = barStart + step * (beatMs / 2)
                    val lane = pattern[step % pattern.size]
                    val noteIndex = (lane * 2 + (step % 2) + bar) % melodyNotes.size
                    val note = melodyNotes[noteIndex]
                    val isHold = (step == 0 && (bar == 4 || bar == 10 || bar == 16))
                    val isBonus = (bar in 10..14 && step == 2 && bar % 2 == 0)

                    tiles.add(
                        TileNote(
                            id = idCounter++,
                            timeMs = time,
                            lane = lane,
                            lengthMs = if (isHold) (beatMs * 1.2).toLong() else 0L,
                            pitchHz = note.first,
                            noteName = note.second,
                            isBonus = isBonus
                        )
                    )
                }
            }
        }

        return TrackDef(
            id = "cyber_drive",
            title = "Cyber Drive",
            subtitle = "High-Speed Highway",
            bpm = bpm,
            keySignature = "D Minor",
            difficulty = TrackDifficulty.HARD,
            durationSeconds = 46f,
            primaryColor = NeonMagenta,
            secondaryColor = NeonAmber,
            tiles = tiles,
            chordProgression = listOf("Dm", "Bb", "F", "C", "Dm", "Gm", "A7", "Dm")
        )
    }

    private fun createMidnightOdyssey(): TrackDef {
        val bpm = 118
        val beatMs = (60_000.0 / bpm).toLong()
        val tiles = mutableListOf<TileNote>()
        var idCounter = 2000

        // Scale: F Minor (F4, Ab4, Bb4, C5, Eb5, F5)
        val melodyNotes = listOf(
            Pair(NOTE_F4, "F4"),
            Pair(415.30f, "Ab4"),
            Pair(466.16f, "Bb4"),
            Pair(NOTE_C5, "C5"),
            Pair(622.25f, "Eb5"),
            Pair(NOTE_F5, "F5")
        )

        val startTime = 2200L
        val totalBars = 24

        val lanePatterns = listOf(
            listOf(0, 2, 1, 3, 0, 3, 1, 2),
            listOf(2, 0, 3, 1, 2, 1, 0, 3),
            listOf(3, 1, 2, 0, 3, 0, 2, 1),
            listOf(1, 3, 0, 2, 1, 2, 3, 0)
        )

        for (bar in 0 until totalBars) {
            val barStart = startTime + bar * (beatMs * 4)
            val pattern = lanePatterns[bar % lanePatterns.size]

            for (step in 0..7) {
                val shouldSpawn = when {
                    bar < 2 -> step % 2 == 0
                    bar in 2..5 -> step % 2 == 0 || step == 3
                    bar in 6..19 -> true
                    else -> step % 2 == 0
                }

                if (shouldSpawn) {
                    val time = barStart + step * (beatMs / 2)
                    val lane = pattern[step % pattern.size]
                    val noteIndex = (lane + bar) % melodyNotes.size
                    val note = melodyNotes[noteIndex]
                    val isHold = (step == 0 && bar % 3 == 0)
                    val isBonus = (bar == 12 || bar == 18) && step == 6

                    tiles.add(
                        TileNote(
                            id = idCounter++,
                            timeMs = time,
                            lane = lane,
                            lengthMs = if (isHold) (beatMs * 1.5).toLong() else 0L,
                            pitchHz = note.first,
                            noteName = note.second,
                            isBonus = isBonus
                        )
                    )
                }
            }
        }

        return TrackDef(
            id = "midnight_odyssey",
            title = "Midnight Odyssey",
            subtitle = "Dark Cyberpunk Pulse",
            bpm = bpm,
            keySignature = "F Minor",
            difficulty = TrackDifficulty.EXPERT,
            durationSeconds = 52f,
            primaryColor = NeonPurple,
            secondaryColor = NeonCyan,
            tiles = tiles,
            chordProgression = listOf("Fm", "Db", "Ab", "Eb", "Bbm", "Fm", "C7", "Fm")
        )
    }

    private fun createStarlight1984(): TrackDef {
        val bpm = 136
        val beatMs = (60_000.0 / bpm).toLong()
        val tiles = mutableListOf<TileNote>()
        var idCounter = 3000

        // Scale: E Minor (E4, G4, A4, B4, D5, E5, G5)
        val melodyNotes = listOf(
            Pair(NOTE_E4, "E4"),
            Pair(NOTE_G4, "G4"),
            Pair(NOTE_A4, "A4"),
            Pair(NOTE_B4, "B4"),
            Pair(NOTE_D5, "D5"),
            Pair(NOTE_E5, "E5"),
            Pair(NOTE_G5, "G5")
        )

        val startTime = 1800L
        val totalBars = 24

        val lanePatterns = listOf(
            listOf(0, 1, 2, 3, 2, 1, 0, 3),
            listOf(3, 2, 1, 0, 1, 2, 3, 0),
            listOf(1, 0, 3, 2, 0, 3, 1, 2),
            listOf(2, 3, 0, 1, 3, 0, 2, 1)
        )

        for (bar in 0 until totalBars) {
            val barStart = startTime + bar * (beatMs * 4)
            val pattern = lanePatterns[bar % lanePatterns.size]

            for (step in 0..7) {
                val shouldSpawn = when {
                    bar < 2 -> step % 2 == 0
                    bar in 2..5 -> true
                    bar in 6..19 -> true
                    else -> step % 2 == 0
                }

                if (shouldSpawn) {
                    val time = barStart + step * (beatMs / 2)
                    val lane = pattern[step % pattern.size]
                    val noteIndex = (lane * 2 + (bar % 4)) % melodyNotes.size
                    val note = melodyNotes[noteIndex]
                    val isHold = (step == 0 && bar % 2 == 0 && bar in 8..16)
                    val isBonus = (bar in 7..19 && step == 3 && bar % 3 == 0)

                    tiles.add(
                        TileNote(
                            id = idCounter++,
                            timeMs = time,
                            lane = lane,
                            lengthMs = if (isHold) (beatMs * 1.3).toLong() else 0L,
                            pitchHz = note.first,
                            noteName = note.second,
                            isBonus = isBonus
                        )
                    )
                }
            }
        }

        return TrackDef(
            id = "starlight_1984",
            title = "Starlight 1984",
            subtitle = "Laser Wave Hyper-Drive",
            bpm = bpm,
            keySignature = "E Minor",
            difficulty = TrackDifficulty.MASTER,
            durationSeconds = 45f,
            primaryColor = NeonAmber,
            secondaryColor = NeonMagenta,
            tiles = tiles,
            chordProgression = listOf("Em", "C", "G", "D", "Em", "Am", "B7", "Em")
        )
    }
}
