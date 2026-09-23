package com.example

import com.example.audio.TrackCatalog
import com.example.audio.TrackDifficulty
import com.example.game.HitRating
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SynthTilesTest {

    @Test
    fun verifyTrackCatalogIntegrity() {
        val tracks = TrackCatalog.allTracks
        assertEquals(4, tracks.size)

        val neonHorizon = TrackCatalog.getTrackById("neon_horizon")
        assertNotNull(neonHorizon)
        assertEquals("Neon Horizon", neonHorizon.title)
        assertEquals(110, neonHorizon.bpm)
        assertEquals(TrackDifficulty.NORMAL, neonHorizon.difficulty)
        assertTrue(neonHorizon.tiles.isNotEmpty())

        val cyberDrive = TrackCatalog.getTrackById("cyber_drive")
        assertNotNull(cyberDrive)
        assertEquals(124, cyberDrive.bpm)
        assertEquals(TrackDifficulty.HARD, cyberDrive.difficulty)
        assertTrue(cyberDrive.tiles.isNotEmpty())

        val midnightOdyssey = TrackCatalog.getTrackById("midnight_odyssey")
        assertNotNull(midnightOdyssey)
        assertEquals(TrackDifficulty.EXPERT, midnightOdyssey.difficulty)

        val starlight = TrackCatalog.getTrackById("starlight_1984")
        assertNotNull(starlight)
        assertEquals(136, starlight.bpm)
        assertEquals(TrackDifficulty.MASTER, starlight.difficulty)
    }

    @Test
    fun verifyAllTilesAreInValidLanes() {
        TrackCatalog.allTracks.forEach { track ->
            track.tiles.forEach { tile ->
                assertTrue("Tile lane must be 0..3", tile.lane in 0..3)
                assertTrue("Tile pitch must be positive", tile.pitchHz > 0)
                assertTrue("Tile timestamp must be positive", tile.timeMs > 0)
            }
        }
    }

    @Test
    fun verifyHitRatingScoring() {
        assertEquals(500, HitRating.PERFECT.score)
        assertEquals(300, HitRating.GREAT.score)
        assertEquals(150, HitRating.GOOD.score)
        assertEquals(0, HitRating.MISS.score)
    }
}
