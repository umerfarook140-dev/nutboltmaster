package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var soundEnabled: Boolean = true
    var hapticsEnabled: Boolean = true

    // Pre-generated audio PCM buffers for zero latency
    private val audioCache = ConcurrentHashMap<String, ShortArray>()
    private val sampleRate = 22050

    init {
        scope.launch {
            audioCache["click"] = generateTone(880.0, 0.04, 0.4)
            audioCache["unscrew"] = generateUnscrewSound()
            audioCache["tray_drop"] = generateTrayDropSound()
            audioCache["match"] = generateMatchChime()
            audioCache["plate_fall"] = generatePlateFallSound()
            audioCache["win"] = generateVictorySound()
            audioCache["game_over"] = generateGameOverSound()
        }
    }

    fun playClick() {
        if (!soundEnabled) return
        playSound("click")
        vibrate(15)
    }

    fun playUnscrew() {
        if (!soundEnabled) return
        playSound("unscrew")
        vibrate(35)
    }

    fun playTrayDrop() {
        if (!soundEnabled) return
        playSound("tray_drop")
        vibrate(20)
    }

    fun playMatch() {
        if (!soundEnabled) return
        playSound("match")
        vibrate(60)
    }

    fun playPlateFall() {
        if (!soundEnabled) return
        playSound("plate_fall")
        vibrate(80)
    }

    fun playWin() {
        if (!soundEnabled) return
        playSound("win")
        vibratePattern(longArrayOf(0, 50, 50, 100))
    }

    fun playGameOver() {
        if (!soundEnabled) return
        playSound("game_over")
        vibrate(120)
    }

    private fun playSound(key: String) {
        scope.launch {
            val samples = audioCache[key] ?: return@launch
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(samples.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(samples, 0, samples.size)
                track.play()
                // Auto release after sound finishes
                track.setNotificationMarkerPosition(samples.size)
                track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                    override fun onMarkerReached(t: AudioTrack?) {
                        try {
                            t?.stop()
                            t?.release()
                        } catch (_: Exception) {}
                    }
                    override fun onPeriodicNotification(t: AudioTrack?) {}
                })
            } catch (_: Exception) {}
        }
    }

    private fun vibrate(durationMs: Long) {
        if (!hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(pattern: LongArray) {
        if (!hapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }

    // Audio synthesis generators
    private fun generateTone(freq: Double, durationSec: Double, volume: Double): ShortArray {
        val count = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val decay = exp(-t * 20.0)
            val sample = sin(2 * PI * freq * t) * decay * volume
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateUnscrewSound(): ShortArray {
        // Series of metallic ratchet clicks ramping pitch
        val durationSec = 0.28
        val count = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(count)
        val numClicks = 6
        val clickInterval = count / numClicks

        for (c in 0 until numClicks) {
            val startIdx = c * clickInterval
            val clickFreq = 1200.0 + c * 350.0
            val clickLength = (sampleRate * 0.025).toInt().coerceAtMost(count - startIdx)
            for (i in 0 until clickLength) {
                val t = i.toDouble() / sampleRate
                val env = exp(-t * 80.0)
                val sample = (sin(2 * PI * clickFreq * t) + 0.5 * sin(2 * PI * clickFreq * 1.5 * t)) * env * 0.45
                val idx = startIdx + i
                if (idx < count) {
                    buffer[idx] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
        }
        return buffer
    }

    private fun generateTrayDropSound(): ShortArray {
        // Metallic thud with pleasant ring
        val durationSec = 0.12
        val count = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val env = exp(-t * 35.0)
            val sample = (sin(2 * PI * 650.0 * t) * 0.7 + sin(2 * PI * 1800.0 * t) * 0.3) * env * 0.5
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateMatchChime(): ShortArray {
        // Ascending harmonic triad (C5: 523Hz, E5: 659Hz, G5: 784Hz, C6: 1046Hz)
        val durationSec = 0.4
        val count = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(count)
        val freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.5)
        val noteDelay = (sampleRate * 0.07).toInt()

        for (n in freqs.indices) {
            val startIdx = n * noteDelay
            val freq = freqs[n]
            for (i in startIdx until count) {
                val t = (i - startIdx).toDouble() / sampleRate
                val env = exp(-t * 9.0)
                val sample = sin(2 * PI * freq * t) * env * 0.25
                val current = buffer[i].toInt()
                val combined = (current + sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                buffer[i] = combined.toShort()
            }
        }
        return buffer
    }

    private fun generatePlateFallSound(): ShortArray {
        // Deep woosh followed by wooden-metallic impact
        val durationSec = 0.35
        val count = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val noise = (Math.random() * 2.0 - 1.0)
            val lowHum = sin(2 * PI * (180.0 - t * 100.0) * t)
            val env = if (t < 0.15) t / 0.15 else exp(-(t - 0.15) * 12.0)
            val sample = (lowHum * 0.6 + noise * 0.25) * env * 0.5
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }

    private fun generateVictorySound(): ShortArray {
        // Triumphant victory fanfare (G4 -> C5 -> E5 -> G5)
        val notes = doubleArrayOf(392.0, 523.25, 659.25, 783.99)
        val noteLengthSec = 0.12
        val lastNoteSec = 0.35
        val totalSec = noteLengthSec * 3 + lastNoteSec
        val count = (sampleRate * totalSec).toInt()
        val buffer = ShortArray(count)

        for (n in notes.indices) {
            val start = (n * noteLengthSec * sampleRate).toInt()
            val len = if (n == notes.lastIndex) (lastNoteSec * sampleRate).toInt() else (noteLengthSec * sampleRate).toInt()
            val freq = notes[n]
            for (i in 0 until len) {
                val idx = start + i
                if (idx < count) {
                    val t = i.toDouble() / sampleRate
                    val decay = if (n == notes.lastIndex) exp(-t * 5.0) else exp(-t * 8.0)
                    val sample = (sin(2 * PI * freq * t) + 0.3 * sin(2 * PI * freq * 2.0 * t)) * decay * 0.35
                    buffer[idx] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
            }
        }
        return buffer
    }

    private fun generateGameOverSound(): ShortArray {
        // Descending disappointed boing
        val durationSec = 0.4
        val count = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val freq = 440.0 - t * 240.0
            val env = exp(-t * 6.0)
            val sample = sin(2 * PI * freq * t) * env * 0.4
            buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return buffer
    }
}
