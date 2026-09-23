package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.graphics.Color
import com.example.audio.SynthEngine
import com.example.audio.TrackCatalog
import com.example.audio.TrackDef
import com.example.data.db.AppDatabase
import com.example.data.model.GameSettings
import com.example.data.model.TrackRecord
import com.example.data.repository.GameRepository
import com.example.ui.theme.MissRed
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PerfectGold
import com.example.util.HapticHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class GameUiState(
    val phase: GamePhase = GamePhase.MENU,
    val selectedTrack: TrackDef = TrackCatalog.allTracks[0],
    val gameTimeMs: Long = 0L,
    val score: Int = 0,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val shields: Int = 3,
    val maxShields: Int = 3,
    val feverCharge: Float = 0f, // 0f to 100f
    val isFeverActive: Boolean = false,
    val feverTimeLeftMs: Long = 0L,
    val perfectCount: Int = 0,
    val greatCount: Int = 0,
    val goodCount: Int = 0,
    val missCount: Int = 0,
    val isNewHighScore: Boolean = false,
    val starsEarned: Int = 0,
    val songProgress: Float = 0f,
    val hitFeedbacks: List<HitFeedback> = emptyList(),
    val laneFlashes: List<LaneFlash> = emptyList()
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = GameRepository(database.appDao())
    val synthEngine = SynthEngine()
    private val hapticHelper = HapticHelper(application)

    val allRecords: StateFlow<List<TrackRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val gameSettings: StateFlow<GameSettings?> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GameSettings()
        )

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Internal active tile states for performance
    var currentTiles: List<ActiveTileState> = emptyList()
        private set

    val particles = mutableListOf<Particle>()
    private var particleIdCounter = 0L
    private var feedbackIdCounter = 0L

    init {
        synthEngine.init()
        viewModelScope.launch {
            gameSettings.collect { settings ->
                settings?.let {
                    synthEngine.bgmVolume = it.bgmVolume
                    synthEngine.sfxVolume = it.sfxVolume
                }
            }
        }
    }

    fun selectTrack(track: TrackDef) {
        _uiState.value = _uiState.value.copy(selectedTrack = track)
    }

    fun startGame(track: TrackDef = _uiState.value.selectedTrack) {
        val activeTiles = track.tiles.map { note ->
            ActiveTileState(
                id = note.id,
                lane = note.lane,
                timeMs = note.timeMs,
                lengthMs = note.lengthMs,
                pitchHz = note.pitchHz,
                isBonus = note.isBonus
            )
        }
        currentTiles = activeTiles
        particles.clear()

        _uiState.value = GameUiState(
            phase = GamePhase.PLAYING,
            selectedTrack = track,
            gameTimeMs = 0L,
            score = 0,
            combo = 0,
            maxCombo = 0,
            shields = 3,
            maxShields = 3,
            feverCharge = 0f,
            isFeverActive = false,
            feverTimeLeftMs = 0L,
            perfectCount = 0,
            greatCount = 0,
            goodCount = 0,
            missCount = 0,
            isNewHighScore = false,
            starsEarned = 0,
            songProgress = 0f,
            hitFeedbacks = emptyList(),
            laneFlashes = emptyList()
        )

        synthEngine.startTrack(track)
    }

    fun updateGameLoop(deltaMs: Long) {
        val state = _uiState.value
        if (state.phase != GamePhase.PLAYING) return

        val newGameTime = state.gameTimeMs + deltaMs
        val track = state.selectedTrack
        val totalTrackMs = (track.durationSeconds * 1000f).toLong()

        // 1. Update Fever status
        var isFever = state.isFeverActive
        var feverTimeLeft = state.feverTimeLeftMs
        var feverCharge = state.feverCharge

        if (isFever) {
            feverTimeLeft = maxOf(0L, feverTimeLeft - deltaMs)
            feverCharge = (feverTimeLeft / 8000f) * 100f
            if (feverTimeLeft <= 0L) {
                isFever = false
                feverCharge = 0f
            }
        }

        // 2. Check for missed tiles (passed hit-line without tap)
        // Hit timing window allows up to +180ms late. Beyond +200ms is a Miss.
        var shields = state.shields
        var combo = state.combo
        var missCount = state.missCount
        val now = System.currentTimeMillis()
        val hitFeedbacks = state.hitFeedbacks.filter { now - it.timeCreated < 800 }.toMutableList()
        val laneFlashes = state.laneFlashes.filter { now - it.startTime < 250 }

        for (tile in currentTiles) {
            if (!tile.isHit && !tile.missed && newGameTime > tile.timeMs + 200) {
                tile.missed = true
                missCount++
                combo = 0
                shields = maxOf(0, shields - 1)
                synthEngine.triggerMiss()
                if (gameSettings.value?.hapticsEnabled != false) {
                    hapticHelper.missStumble()
                }

                hitFeedbacks.add(
                    HitFeedback(
                        id = feedbackIdCounter++,
                        rating = HitRating.MISS,
                        combo = 0,
                        lane = tile.lane
                    )
                )
            }
        }

        // 3. Update active particles
        val pIt = particles.iterator()
        while (pIt.hasNext()) {
            val p = pIt.next()
            p.x += p.vx * (deltaMs / 16f)
            p.y += p.vy * (deltaMs / 16f)
            p.alpha -= (deltaMs / 400f)
            if (p.alpha <= 0f) {
                pIt.remove()
            }
        }

        // 4. Check for Game Over (all shields lost)
        if (shields <= 0) {
            synthEngine.stopTrack()
            saveRunResults(isVictory = false)
            _uiState.value = state.copy(
                phase = GamePhase.GAME_OVER,
                shields = 0,
                combo = 0,
                missCount = missCount,
                hitFeedbacks = hitFeedbacks
            )
            return
        }

        // 5. Check for Song Completion / Victory
        val songProgress = (newGameTime.toFloat() / totalTrackMs).coerceIn(0f, 1f)
        val allTilesDone = currentTiles.all { it.isHit || it.missed }
        if (songProgress >= 1f && allTilesDone) {
            synthEngine.stopTrack()
            saveRunResults(isVictory = true)
            _uiState.value = state.copy(
                phase = GamePhase.VICTORY,
                gameTimeMs = newGameTime,
                songProgress = 1f,
                hitFeedbacks = hitFeedbacks
            )
            return
        }

        _uiState.value = state.copy(
            gameTimeMs = newGameTime,
            songProgress = songProgress,
            shields = shields,
            combo = combo,
            missCount = missCount,
            isFeverActive = isFever,
            feverTimeLeftMs = feverTimeLeft,
            feverCharge = feverCharge,
            hitFeedbacks = hitFeedbacks,
            laneFlashes = laneFlashes
        )
    }

    fun onLaneTouchDown(lane: Int, screenWidthPx: Float = 1080f, hitYPx: Float = 1600f) {
        val state = _uiState.value
        if (state.phase != GamePhase.PLAYING) return

        val currentTime = state.gameTimeMs
        val now = System.currentTimeMillis()

        // Find nearest unhit tile in this lane within timing threshold (+/- 220ms)
        val candidate = currentTiles
            .filter { it.lane == lane && !it.isHit && !it.missed }
            .minByOrNull { kotlin.math.abs(it.timeMs - currentTime) }

        val diff = if (candidate != null) kotlin.math.abs(candidate.timeMs - currentTime) else 9999L

        if (candidate != null && diff <= 220L) {
            // Successful Hit!
            candidate.isHit = true
            candidate.isHolding = candidate.lengthMs > 0

            val rating = when {
                diff <= 55L -> HitRating.PERFECT
                diff <= 120L -> HitRating.GREAT
                else -> HitRating.GOOD
            }

            val feverMult = if (state.isFeverActive) 2 else 1
            val comboMult = when {
                state.combo >= 40 -> 4
                state.combo >= 20 -> 3
                state.combo >= 10 -> 2
                else -> 1
            }

            val bonusScore = if (candidate.isBonus) 1000 else 0
            val earnedScore = (rating.score * comboMult * feverMult) + bonusScore
            val newScore = state.score + earnedScore
            val newCombo = state.combo + 1
            val newMaxCombo = maxOf(state.maxCombo, newCombo)

            // Audio & Haptics
            synthEngine.triggerTileHit(candidate.pitchHz, candidate.lengthMs, candidate.isBonus)
            if (gameSettings.value?.hapticsEnabled != false) {
                hapticHelper.hitSuccess()
            }

            // Fever charge increase
            var newFeverCharge = state.feverCharge + (if (candidate.isBonus) 25f else 6f)
            var feverActive = state.isFeverActive
            var feverTimeLeft = state.feverTimeLeftMs

            if (!feverActive && newFeverCharge >= 100f) {
                feverActive = true
                feverTimeLeft = 8000L
                newFeverCharge = 100f
                if (gameSettings.value?.hapticsEnabled != false) {
                    hapticHelper.feverBonus()
                }
            }

            // Spawn neon particles at lane position
            val laneWidth = screenWidthPx / 4f
            val spawnX = lane * laneWidth + laneWidth / 2f
            spawnHitParticles(spawnX, hitYPx, candidate.isBonus, rating)

            // Feedback
            val feedbacks = state.hitFeedbacks.toMutableList().apply {
                add(HitFeedback(feedbackIdCounter++, rating, newCombo, lane))
            }

            val flashes = state.laneFlashes.toMutableList().apply {
                removeAll { it.lane == lane }
                add(LaneFlash(lane, rating.color, now))
            }

            val pCount = state.perfectCount + if (rating == HitRating.PERFECT) 1 else 0
            val gCount = state.greatCount + if (rating == HitRating.GREAT) 1 else 0
            val gdCount = state.goodCount + if (rating == HitRating.GOOD) 1 else 0

            _uiState.value = state.copy(
                score = newScore,
                combo = newCombo,
                maxCombo = newMaxCombo,
                feverCharge = newFeverCharge.coerceAtMost(100f),
                isFeverActive = feverActive,
                feverTimeLeftMs = feverTimeLeft,
                perfectCount = pCount,
                greatCount = gCount,
                goodCount = gdCount,
                hitFeedbacks = feedbacks,
                laneFlashes = flashes
            )
        } else {
            // Tap on empty space or way off-beat (Empty tap penalty if strict, or subtle glitch)
            synthEngine.triggerMiss()
            val flashes = state.laneFlashes.toMutableList().apply {
                removeAll { it.lane == lane }
                add(LaneFlash(lane, MissRed.copy(alpha = 0.6f), now))
            }
            _uiState.value = state.copy(laneFlashes = flashes)
        }
    }

    fun onLaneTouchUp(lane: Int) {
        val candidate = currentTiles.firstOrNull { it.lane == lane && it.isHolding }
        candidate?.isHolding = false
    }

    private fun spawnHitParticles(x: Float, y: Float, isBonus: Boolean, rating: HitRating) {
        val count = if (isBonus) 24 else 14
        val colors = if (isBonus) {
            listOf(PerfectGold, NeonAmber, NeonMagenta, NeonCyan)
        } else {
            listOf(rating.color, NeonCyan, NeonMagenta, Color.White)
        }

        for (i in 0 until count) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 12f + 4f
            particles.add(
                Particle(
                    id = particleIdCounter++,
                    x = x + (Random.nextFloat() - 0.5f) * 40f,
                    y = y + (Random.nextFloat() - 0.5f) * 20f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 4f, // initial upward burst
                    alpha = 1.0f,
                    color = colors[i % colors.size],
                    size = Random.nextFloat() * 6f + 4f
                )
            )
        }
    }

    private fun saveRunResults(isVictory: Boolean) {
        val state = _uiState.value
        val track = state.selectedTrack
        val totalNotes = track.tiles.size
        val hitNotes = state.perfectCount + state.greatCount + state.goodCount
        val accuracy = if (totalNotes > 0) (hitNotes.toFloat() / totalNotes) else 0f

        val stars = when {
            accuracy >= 0.95f && isVictory -> 3
            accuracy >= 0.75f && isVictory -> 2
            hitNotes > totalNotes / 2 -> 1
            else -> 0
        }

        viewModelScope.launch {
            val existing = repository.getRecord(track.id)
            val isNewRecord = state.score > (existing?.highScore ?: 0)
            _uiState.value = _uiState.value.copy(
                isNewHighScore = isNewRecord,
                starsEarned = stars
            )

            repository.updateScore(
                trackId = track.id,
                score = state.score,
                combo = state.maxCombo,
                stars = stars,
                completed = isVictory
            )
        }
    }

    fun revive() {
        val state = _uiState.value
        _uiState.value = state.copy(
            phase = GamePhase.PLAYING,
            shields = 2
        )
        synthEngine.resumeMusic()
    }

    fun pauseGame() {
        if (_uiState.value.phase == GamePhase.PLAYING) {
            _uiState.value = _uiState.value.copy(phase = GamePhase.PAUSED)
            synthEngine.pauseMusic()
        }
    }

    fun resumeGame() {
        if (_uiState.value.phase == GamePhase.PAUSED) {
            _uiState.value = _uiState.value.copy(phase = GamePhase.PLAYING)
            synthEngine.resumeMusic()
        }
    }

    fun restartGame() {
        startGame(_uiState.value.selectedTrack)
    }

    fun quitToMenu() {
        synthEngine.stopTrack()
        _uiState.value = _uiState.value.copy(phase = GamePhase.MENU)
    }

    fun updateSettings(speed: Float, haptics: Boolean, bgm: Float, sfx: Float) {
        viewModelScope.launch {
            val updated = GameSettings(
                id = 1,
                speedMultiplier = speed,
                hapticsEnabled = haptics,
                bgmVolume = bgm,
                sfxVolume = sfx
            )
            repository.saveSettings(updated)
            synthEngine.bgmVolume = bgm
            synthEngine.sfxVolume = sfx
        }
    }

    override fun onCleared() {
        super.onCleared()
        synthEngine.release()
    }
}
