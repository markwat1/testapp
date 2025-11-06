package com.example.metronome

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sin

/**
 * Utility class for audio-related operations
 * Provides fallback audio generation when click.wav is not available
 */
object AudioUtils {
    
    private const val TAG = "AudioUtils"
    
    /**
     * Generate a simple click sound programmatically
     * Creates a short beep sound that can be used as a fallback
     * 
     * @param context Application context
     * @return true if click sound was generated successfully, false otherwise
     */
    fun generateClickSound(context: Context): Boolean {
        return try {
            val sampleRate = 44100
            val duration = 0.05 // 50ms
            val numSamples = (duration * sampleRate).toInt()
            val samples = ShortArray(numSamples)
            
            // Generate a short beep (1000Hz sine wave with envelope)
            for (i in samples.indices) {
                val time = i.toDouble() / sampleRate
                val frequency = 1000.0 // 1kHz
                
                // Apply envelope to avoid clicks (fade in/out)
                val envelope = when {
                    i < numSamples * 0.1 -> i / (numSamples * 0.1) // Fade in
                    i > numSamples * 0.9 -> (numSamples - i) / (numSamples * 0.1) // Fade out
                    else -> 1.0 // Sustain
                }
                
                val amplitude = 0.3 * envelope // 30% volume with envelope
                val sample = (amplitude * sin(2 * Math.PI * frequency * time) * Short.MAX_VALUE).toInt()
                samples[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            
            // Create WAV file in internal storage as fallback
            val internalDir = File(context.filesDir, "audio")
            if (!internalDir.exists()) {
                internalDir.mkdirs()
            }
            
            val wavFile = File(internalDir, "generated_click.wav")
            createWavFile(samples, sampleRate, wavFile)
            
            Log.d(TAG, "Generated click sound at: ${wavFile.absolutePath}")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate click sound: ${e.message}")
            false
        }
    }
    
    /**
     * Create a WAV file from audio samples
     * 
     * @param samples Audio samples as short array
     * @param sampleRate Sample rate in Hz
     * @param outputFile Output file
     */
    private fun createWavFile(samples: ShortArray, sampleRate: Int, outputFile: File) {
        val channels = 1 // Mono
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8
        val dataSize = samples.size * 2 // 2 bytes per sample
        val fileSize = 36 + dataSize
        
        FileOutputStream(outputFile).use { fos ->
            // WAV header
            fos.write("RIFF".toByteArray())
            fos.write(intToByteArray(fileSize))
            fos.write("WAVE".toByteArray())
            
            // Format chunk
            fos.write("fmt ".toByteArray())
            fos.write(intToByteArray(16)) // Chunk size
            fos.write(shortToByteArray(1)) // Audio format (PCM)
            fos.write(shortToByteArray(channels.toShort()))
            fos.write(intToByteArray(sampleRate))
            fos.write(intToByteArray(byteRate))
            fos.write(shortToByteArray(blockAlign.toShort()))
            fos.write(shortToByteArray(bitsPerSample.toShort()))
            
            // Data chunk
            fos.write("data".toByteArray())
            fos.write(intToByteArray(dataSize))
            
            // Audio data
            for (sample in samples) {
                fos.write(shortToByteArray(sample))
            }
        }
    }
    
    /**
     * Convert int to little-endian byte array
     */
    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }
    
    /**
     * Convert short to little-endian byte array
     */
    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xFF).toByte(),
            ((value.toInt() shr 8) and 0xFF).toByte()
        )
    }
    
    /**
     * Play a simple beep using AudioTrack (alternative to MediaPlayer)
     * Can be used as immediate fallback when MediaPlayer fails
     * 
     * @param frequency Frequency in Hz
     * @param durationMs Duration in milliseconds
     */
    fun playBeep(frequency: Double = 1000.0, durationMs: Int = 50) {
        try {
            val sampleRate = 44100
            val numSamples = (durationMs * sampleRate / 1000)
            val samples = ShortArray(numSamples)
            
            // Generate sine wave
            for (i in samples.indices) {
                val time = i.toDouble() / sampleRate
                val amplitude = 0.3 // 30% volume
                val sample = (amplitude * sin(2 * Math.PI * frequency * time) * Short.MAX_VALUE).toInt()
                samples[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            
            // Play using AudioTrack
            val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                samples.size * 2,
                AudioTrack.MODE_STATIC
            )
            
            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()
            
            // Clean up after playback
            Thread {
                Thread.sleep(durationMs.toLong() + 100)
                audioTrack.stop()
                audioTrack.release()
            }.start()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play beep: ${e.message}")
        }
    }
}