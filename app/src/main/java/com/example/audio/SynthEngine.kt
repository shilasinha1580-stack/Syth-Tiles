package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SynthEngine {

    companion object {
        private const val TAG = "SynthEngine"
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_FRAMES = 1024
    }

    private var audioTrack: AudioTrack? = null
    private var isRunning = false
    private var renderThread: Thread? = null

    // Track playback state
    private var currentTrack: TrackDef? = null
    private var trackStartSample: Long = 0L
    private var isPlayingMusic = false

    // Volume settings (0.0 to 1.0)
    var bgmVolume: Float = 0.85f
    var sfxVolume: Float = 0.95f
    var masterVolume: Float = 1.0f

    // Active interactive voices for tile hits
    private data class SynthVoice(
        val pitchHz: Float,
        val durationSamples: Int,
        var samplePos: Int = 0,
        val volume: Float = 0.7f,
        val isBonus: Boolean = false
    )

    private val activeVoices = ConcurrentLinkedQueue<SynthVoice>()

    // One-shot SFX queues (miss sound, perfect combo chime)
    private var missSoundSamplesLeft = 0
    private var missSoundPos = 0

    // Random generator for drum noise
    private var noiseSeed = 123456789L

    private fun nextNoise(): Float {
        noiseSeed = (noiseSeed * 1103515245L + 12345L) and 0x7fffffffL
        return ((noiseSeed.toFloat() / 0x7fffffffL) * 2f) - 1f
    }

    fun init() {
        if (audioTrack != null) return

        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = maxOf(minBufSize, BUFFER_FRAMES * 4)

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
            isRunning = true

            renderThread = Thread({ audioRenderLoop() }, "SynthWaveAudioThread").apply {
                priority = Thread.MAX_PRIORITY
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioTrack: ${e.message}", e)
        }
    }

    fun startTrack(track: TrackDef) {
        currentTrack = track
        trackStartSample = 0L
        activeVoices.clear()
        isPlayingMusic = true
    }

    fun pauseMusic() {
        isPlayingMusic = false
    }

    fun resumeMusic() {
        isPlayingMusic = true
    }

    fun stopTrack() {
        isPlayingMusic = false
        currentTrack = null
        activeVoices.clear()
    }

    fun triggerTileHit(pitchHz: Float, lengthMs: Long, isBonus: Boolean = false) {
        val durationMs = if (lengthMs > 0) lengthMs + 200 else 320L
        val durationSamples = ((durationMs / 1000f) * SAMPLE_RATE).toInt()
        val voiceVolume = if (isBonus) 0.85f else 0.72f
        activeVoices.add(
            SynthVoice(
                pitchHz = pitchHz,
                durationSamples = durationSamples,
                volume = voiceVolume,
                isBonus = isBonus
            )
        )
    }

    fun triggerMiss() {
        missSoundSamplesLeft = (0.25f * SAMPLE_RATE).toInt()
        missSoundPos = 0
    }

    private fun audioRenderLoop() {
        val pcmBuffer = ShortArray(BUFFER_FRAMES * 2) // Stereo (L, R)

        while (isRunning) {
            val track = currentTrack
            val playing = isPlayingMusic && track != null

            val bpm = track?.bpm ?: 120
            val samplesPerBeat = (SAMPLE_RATE * 60.0 / bpm).toDouble()
            val samplesPer16th = samplesPerBeat / 4.0

            for (i in 0 until BUFFER_FRAMES) {
                var mixL = 0f
                var mixR = 0f

                val globalSample = trackStartSample + i

                // 1. Synthwave Rhythm Track (Drums + Bassline + Chords)
                if (playing) {
                    val beatPos = (globalSample / samplesPerBeat)
                    val beat16th = (globalSample / samplesPer16th).toLong()
                    val sampleIn16th = (globalSample % samplesPer16th).toInt()

                    val bar = (beatPos / 4).toInt()
                    val beatInBar = (beatPos % 4).toInt()
                    val sampleInBeat = (globalSample % samplesPerBeat).toInt()

                    // --- A. Kick Drum (Beats 0, 1, 2, 3 - four on the floor!) ---
                    if (sampleInBeat < SAMPLE_RATE * 0.2) {
                        val t = sampleInBeat / SAMPLE_RATE.toFloat()
                        // Pitch sweeps from 160 Hz down to 42 Hz
                        val kickPitch = 42f + 118f * exp(-t * 28f)
                        val kickPhase = kickPitch * t * 2f * PI.toFloat()
                        val kickEnv = exp(-t * 16f)
                        val kickVal = sin(kickPhase) * kickEnv * 0.45f
                        mixL += kickVal
                        mixR += kickVal
                    }

                    // --- B. 80s Snare / Gated Reverb Clap (Beats 1 and 3) ---
                    if ((beatInBar == 1 || beatInBar == 3) && sampleInBeat < SAMPLE_RATE * 0.22) {
                        val t = sampleInBeat / SAMPLE_RATE.toFloat()
                        val snareTone = sin(220f * t * 2f * PI.toFloat()) * exp(-t * 22f) * 0.2f
                        val snareNoise = nextNoise() * exp(-t * 12f) * 0.28f
                        val snareVal = snareTone + snareNoise
                        mixL += snareVal
                        mixR += snareVal
                    }

                    // --- C. Crisp 16th Note Hi-Hats ---
                    if (sampleIn16th < SAMPLE_RATE * 0.05) {
                        val t = sampleIn16th / SAMPLE_RATE.toFloat()
                        val hatAccent = if (beat16th % 2 == 1L) 0.15f else 0.09f
                        val hatVal = nextNoise() * exp(-t * 60f) * hatAccent
                        // Panned slightly right
                        mixL += hatVal * 0.7f
                        mixR += hatVal * 1.1f
                    }

                    // --- D. Synthwave Sawtooth Bassline ---
                    val chordIdx = (bar / 2) % (track!!.chordProgression.size)
                    val chordName = track.chordProgression[chordIdx]
                    val bassRootHz = getBassFreqForChord(chordName)

                    // 16th note arpeggiated bass pattern (e.g. root, octave, root, octave)
                    val step16thInBeat = (beat16th % 4).toInt()
                    val bassFreq = when (step16thInBeat) {
                        1, 3 -> bassRootHz * 2f // Octave jump!
                        else -> bassRootHz
                    }

                    val t16 = sampleIn16th / SAMPLE_RATE.toFloat()
                    val bassEnv = exp(-t16 * 14f)
                    // Rich sawtooth wave approximation (fundamental + 3 harmonics)
                    val p1 = (bassFreq * t16 * 2f * PI.toFloat())
                    val bassSaw = (sin(p1) + 0.5f * sin(p1 * 2f) + 0.25f * sin(p1 * 3f)) * bassEnv * 0.26f

                    // Analog low-pass warmth filter simulation
                    mixL += bassSaw * 0.9f
                    mixR += bassSaw * 0.9f

                    // --- E. Warm Atmospheric Synth Chords / Pad ---
                    val chordNotes = getChordFrequencies(chordName)
                    val padTime = globalSample / SAMPLE_RATE.toFloat()
                    var padMix = 0f
                    for (noteHz in chordNotes) {
                        val phase = noteHz * padTime * 2f * PI.toFloat()
                        // Soft chorus detune
                        val phaseDetune = (noteHz * 1.004f) * padTime * 2f * PI.toFloat()
                        padMix += (sin(phase) + sin(phaseDetune)) * 0.035f
                    }
                    mixL += padMix * 0.8f
                    mixR += padMix * 1.2f // stereo spread
                }

                // Apply BGM volume
                mixL *= (bgmVolume * masterVolume)
                mixR *= (bgmVolume * masterVolume)

                // 2. Interactive Lead Melodies (Tile Hits)
                if (!activeVoices.isEmpty()) {
                    val it = activeVoices.iterator()
                    while (it.hasNext()) {
                        val voice = it.next()
                        if (voice.samplePos >= voice.durationSamples) {
                            it.remove()
                            continue
                        }

                        val t = voice.samplePos / SAMPLE_RATE.toFloat()
                        val durSec = voice.durationSamples / SAMPLE_RATE.toFloat()
                        // ADSR envelope: fast attack (8ms), decay to sustain, release
                        val env = when {
                            t < 0.008f -> t / 0.008f
                            t < durSec * 0.7f -> 1.0f - (t / durSec) * 0.3f
                            else -> ((durSec - t) / (durSec * 0.3f)).coerceIn(0f, 1f) * 0.7f
                        }

                        // Rich dual-oscillator synth lead (Square + Sawtooth with subtle chorus)
                        val freq = voice.pitchHz
                        val p1 = freq * t * 2f * PI.toFloat()
                        val p2 = (freq * 1.006f) * t * 2f * PI.toFloat() // chorus detune
                        val osc1 = sin(p1) + 0.3f * sin(p1 * 3f) // square-ish
                        val osc2 = sin(p2) + 0.4f * sin(p2 * 2f) // saw-ish

                        var leadSample = (osc1 * 0.6f + osc2 * 0.4f) * env * voice.volume

                        if (voice.isBonus) {
                            // Sparkling bonus octave shimmer!
                            val pSparkle = (freq * 2f) * t * 2f * PI.toFloat()
                            leadSample += sin(pSparkle) * env * 0.25f
                        }

                        val sfxScaled = leadSample * (sfxVolume * masterVolume)
                        mixL += sfxScaled * 0.95f
                        mixR += sfxScaled * 1.05f

                        voice.samplePos++
                    }
                }

                // 3. Miss Glitch SFX
                if (missSoundSamplesLeft > 0) {
                    val t = missSoundPos / SAMPLE_RATE.toFloat()
                    val pitch = 220f * exp(-t * 20f)
                    val glitchVal = (sin(pitch * t * 2f * PI.toFloat()) * 0.3f + nextNoise() * 0.15f) *
                            exp(-t * 12f) * (sfxVolume * masterVolume)
                    mixL += glitchVal
                    mixR += glitchVal
                    missSoundPos++
                    missSoundSamplesLeft--
                }

                // Master Soft-Clipping / Limiter to prevent harsh digital distortion
                val finalL = softClip(mixL).coerceIn(-1.0f, 1.0f)
                val finalR = softClip(mixR).coerceIn(-1.0f, 1.0f)

                pcmBuffer[i * 2] = (finalL * 32767f).toInt().toShort()
                pcmBuffer[i * 2 + 1] = (finalR * 32767f).toInt().toShort()
            }

            if (playing) {
                trackStartSample += BUFFER_FRAMES
            }

            audioTrack?.write(pcmBuffer, 0, pcmBuffer.size)
        }
    }

    private fun softClip(x: Float): Float {
        // Fast cubic soft clipper
        return if (x < -1.5f) -1.0f
        else if (x > 1.5f) 1.0f
        else x - (x * x * x) / 27f
    }

    private fun getBassFreqForChord(chord: String): Float {
        return when {
            chord.startsWith("Am") -> 110.00f // A2
            chord.startsWith("F") -> 87.31f   // F2
            chord.startsWith("C") -> 65.41f   // C2
            chord.startsWith("G") -> 98.00f   // G2
            chord.startsWith("Dm") -> 73.42f  // D2
            chord.startsWith("Em") -> 82.41f  // E2
            chord.startsWith("Bb") -> 116.54f // Bb2
            chord.startsWith("Bbm") -> 116.54f
            chord.startsWith("Ab") -> 103.83f // Ab2
            chord.startsWith("Eb") -> 77.78f  // Eb2
            chord.startsWith("Db") -> 69.30f  // Db2
            chord.startsWith("A7") -> 110.00f // A2
            chord.startsWith("B7") -> 123.47f // B2
            else -> 110.00f
        }
    }

    private fun getChordFrequencies(chord: String): List<Float> {
        return when {
            chord.startsWith("Am") -> listOf(220f, 261.63f, 329.63f) // A3, C4, E4
            chord.startsWith("F") -> listOf(174.61f, 220f, 261.63f)  // F3, A3, C4
            chord.startsWith("C") -> listOf(261.63f, 329.63f, 392f)  // C4, E4, G4
            chord.startsWith("G") -> listOf(196f, 246.94f, 293.66f)  // G3, B3, D4
            chord.startsWith("Dm") -> listOf(146.83f, 174.61f, 220f) // D3, F3, A3
            chord.startsWith("Em") -> listOf(164.81f, 196f, 246.94f) // E3, G3, B3
            chord.startsWith("Bb") -> listOf(233.08f, 293.66f, 349.23f)
            chord.startsWith("Bbm") -> listOf(233.08f, 277.18f, 349.23f)
            chord.startsWith("Ab") -> listOf(207.65f, 261.63f, 311.13f)
            chord.startsWith("Eb") -> listOf(155.56f, 196f, 233.08f)
            chord.startsWith("Db") -> listOf(138.59f, 174.61f, 207.65f)
            else -> listOf(220f, 261.63f, 329.63f)
        }
    }

    fun release() {
        isRunning = false
        renderThread?.interrupt()
        try {
            renderThread?.join(500)
        } catch (_: Exception) {}
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
    }
}
