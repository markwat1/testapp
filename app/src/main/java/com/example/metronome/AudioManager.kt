package com.example.metronome

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import java.io.File
import java.io.IOException

/**
 * AudioManager handles metronome beat audio playback with precise timing
 * 
 * Uses MediaPlayer for audio playback and Handler + Runnable for accurate timing control
 * Implements high-precision timing using SystemClock.uptimeMillis()
 */
class AudioManager(private val context: Context) {
    
    companion object {
        private const val TAG = "AudioManager"
        private const val CLICK_ASSET_PATH = "click.wav"
        private const val FALLBACK_AUDIO_PATH = "audio/generated_click.wav"
    }
    
    // MediaPlayer for audio playback
    private var mediaPlayer: MediaPlayer? = null
    
    // Handler for timing control
    private val handler = Handler(Looper.getMainLooper())
    
    // Runnable for beat scheduling
    private var beatRunnable: Runnable? = null
    
    // Timing variables
    private var beatInterval: Long = 0
    private var lastBeatTime: Long = 0
    private var isBeatingActive = false
    
    // High-precision timing optimization
    private var targetBeatTime: Long = 0
    private var beatCount: Long = 0
    private var cumulativeError: Long = 0
    private var averageLatency: Long = 0
    private val latencyHistory = mutableListOf<Long>()
    private val maxLatencyHistorySize = 10
    
    // Fallback mode flag
    private var useFallbackAudio = false
    
    /**
     * Initialize the MediaPlayer with the click sound
     * 
     * @return true if initialization successful, false otherwise
     */
    private fun initializeMediaPlayer(): Boolean {
        return try {
            releaseMediaPlayer()
            
            // Check if audio file exists in assets
            val assetList = context.assets.list("") ?: emptyArray()
            if (!assetList.contains(CLICK_ASSET_PATH)) {
                Log.w(TAG, "Audio file $CLICK_ASSET_PATH not found in assets, trying fallback")
                return initializeFallbackAudio()
            }
            
            mediaPlayer = MediaPlayer().apply {
                // Load audio file from assets
                val assetFileDescriptor = context.assets.openFd(CLICK_ASSET_PATH)
                setDataSource(
                    assetFileDescriptor.fileDescriptor,
                    assetFileDescriptor.startOffset,
                    assetFileDescriptor.length
                )
                assetFileDescriptor.close()
                
                // Prepare the MediaPlayer
                prepare()
                
                // Set completion listener to reset for next play
                setOnCompletionListener { mp ->
                    try {
                        mp.seekTo(0)
                    } catch (e: IllegalStateException) {
                        Log.w(TAG, "Cannot seek MediaPlayer: ${e.message}")
                    }
                }
                
                // Set error listener
                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    false // Return false to trigger OnCompletionListener
                }
                
                // Set prepared listener for additional validation
                setOnPreparedListener { mp ->
                    Log.d(TAG, "MediaPlayer prepared successfully")
                }
            }
            
            Log.d(TAG, "MediaPlayer initialized successfully")
            true
            
        } catch (e: IOException) {
            Log.e(TAG, "Failed to initialize MediaPlayer - IO Error: ${e.message}")
            Log.w(TAG, "Trying fallback audio generation")
            return initializeFallbackAudio()
        } catch (e: IllegalStateException) {
            Log.e(TAG, "MediaPlayer illegal state: ${e.message}")
            return initializeFallbackAudio()
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception accessing audio file: ${e.message}")
            return initializeFallbackAudio()
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error initializing MediaPlayer: ${e.message}")
            return initializeFallbackAudio()
        }
    }
    
    /**
     * Initialize fallback audio using generated click sound
     * 
     * @return true if fallback initialization successful, false otherwise
     */
    private fun initializeFallbackAudio(): Boolean {
        return try {
            // Generate click sound if it doesn't exist
            val fallbackFile = File(context.filesDir, FALLBACK_AUDIO_PATH)
            if (!fallbackFile.exists()) {
                if (!AudioUtils.generateClickSound(context)) {
                    Log.e(TAG, "Failed to generate fallback audio")
                    useFallbackAudio = true // Use AudioTrack fallback
                    return true
                }
            }
            
            releaseMediaPlayer()
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(fallbackFile.absolutePath)
                prepare()
                
                setOnCompletionListener { mp ->
                    try {
                        mp.seekTo(0)
                    } catch (e: IllegalStateException) {
                        Log.w(TAG, "Cannot seek MediaPlayer: ${e.message}")
                    }
                }
                
                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "Fallback MediaPlayer error: what=$what, extra=$extra")
                    useFallbackAudio = true // Switch to AudioTrack fallback
                    false
                }
            }
            
            Log.d(TAG, "Fallback MediaPlayer initialized successfully")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize fallback audio: ${e.message}")
            useFallbackAudio = true // Use AudioTrack as last resort
            true // Return true to allow AudioTrack fallback
        }
    }
    
    /**
     * Play a single beat sound
     * 
     * @return true if beat was played successfully, false otherwise
     */
    fun playBeat(): Boolean {
        return try {
            // Use AudioTrack fallback if MediaPlayer is not available
            if (useFallbackAudio) {
                AudioUtils.playBeep()
                return true
            }
            
            val player = mediaPlayer
            if (player == null) {
                if (!initializeMediaPlayer()) {
                    // If initialization fails, use AudioTrack fallback
                    AudioUtils.playBeep()
                    return true
                }
            }
            
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.seekTo(0)
                } else {
                    mp.start()
                }
                true
            } ?: run {
                // Fallback to AudioTrack
                AudioUtils.playBeep()
                true
            }
            
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Failed to play beat - illegal state: ${e.message}")
            // Use AudioTrack fallback
            AudioUtils.playBeep()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play beat: ${e.message}")
            // Use AudioTrack fallback
            AudioUtils.playBeep()
            true
        }
    }
    
    /**
     * Start continuous beating with specified interval
     * 
     * @param intervalMs Interval between beats in milliseconds
     */
    fun startBeating(intervalMs: Long) {
        if (isBeatingActive) {
            stopBeating()
        }
        
        beatInterval = intervalMs
        isBeatingActive = true
        
        // Initialize high-precision timing variables
        val currentTime = SystemClock.uptimeMillis()
        lastBeatTime = currentTime
        targetBeatTime = currentTime
        beatCount = 0
        cumulativeError = 0
        latencyHistory.clear()
        averageLatency = 0
        
        // Initialize MediaPlayer if needed
        if (mediaPlayer == null) {
            if (!initializeMediaPlayer()) {
                Log.e(TAG, "Failed to initialize audio for beating")
                isBeatingActive = false
                return
            }
        }
        
        // Create and start the beat runnable with enhanced precision
        beatRunnable = object : Runnable {
            override fun run() {
                if (!isBeatingActive) return
                
                val actualBeatTime = SystemClock.uptimeMillis()
                
                // Calculate and track timing accuracy
                trackTimingAccuracy(actualBeatTime)
                
                // Play the beat
                playBeat()
                
                // Schedule next beat with dynamic adjustment
                scheduleNextBeatWithDynamicAdjustment()
            }
        }
        
        // Start the first beat immediately
        handler.post(beatRunnable!!)
        
        Log.d(TAG, "Started high-precision beating with interval: ${intervalMs}ms")
    }
    
    /**
     * Schedule the next beat with dynamic adjustment for maximum precision
     */
    private fun scheduleNextBeatWithDynamicAdjustment() {
        if (!isBeatingActive) return
        
        val currentTime = SystemClock.uptimeMillis()
        beatCount++
        
        // Calculate ideal next beat time based on original start time
        targetBeatTime += beatInterval
        
        // Calculate timing error and apply correction
        val timingError = currentTime - (targetBeatTime - beatInterval)
        cumulativeError += timingError
        
        // Apply dynamic adjustment based on accumulated error and average latency
        val errorCorrection = if (beatCount > 5) {
            // Use weighted correction: 70% current error, 30% cumulative trend
            val currentErrorWeight = (timingError * 0.7).toLong()
            val cumulativeErrorWeight = (cumulativeError / beatCount * 0.3).toLong()
            val latencyCompensation = averageLatency / 2
            
            currentErrorWeight + cumulativeErrorWeight + latencyCompensation
        } else {
            // For first few beats, use simple error correction
            timingError / 2
        }
        
        // Calculate delay with dynamic adjustment
        val baseDelay = targetBeatTime - currentTime
        val adjustedDelay = baseDelay - errorCorrection
        
        // Ensure minimum delay and maximum reasonable delay
        val finalDelay = adjustedDelay.coerceIn(0L, beatInterval)
        
        // Update last beat time for tracking
        lastBeatTime = currentTime
        
        // Schedule next beat
        handler.postDelayed(beatRunnable!!, finalDelay)
        
        // Log precision metrics periodically
        if (beatCount % 20 == 0L) {
            val avgError = if (beatCount > 0) cumulativeError / beatCount else 0
            Log.d(TAG, "Timing metrics - Beat: $beatCount, Avg Error: ${avgError}ms, Avg Latency: ${averageLatency}ms")
        }
    }
    
    /**
     * Track timing accuracy and calculate latency metrics
     */
    private fun trackTimingAccuracy(actualBeatTime: Long) {
        if (beatCount > 0) {
            val expectedBeatTime = targetBeatTime
            val latency = actualBeatTime - expectedBeatTime
            
            // Update latency history for rolling average
            latencyHistory.add(latency)
            if (latencyHistory.size > maxLatencyHistorySize) {
                latencyHistory.removeAt(0)
            }
            
            // Calculate average latency
            averageLatency = if (latencyHistory.isNotEmpty()) {
                latencyHistory.sum() / latencyHistory.size
            } else {
                0
            }
        }
    }
    
    /**
     * Stop continuous beating
     */
    fun stopBeating() {
        isBeatingActive = false
        
        // Remove any pending beat callbacks
        beatRunnable?.let { runnable ->
            handler.removeCallbacks(runnable)
        }
        beatRunnable = null
        
        Log.d(TAG, "Stopped beating")
    }
    
    /**
     * Update the beat interval for ongoing playback with smooth transition
     * 
     * @param newIntervalMs New interval in milliseconds
     */
    fun updateBeatInterval(newIntervalMs: Long) {
        if (isBeatingActive) {
            val oldInterval = beatInterval
            beatInterval = newIntervalMs
            
            // Reset timing calculations for smooth transition
            val currentTime = SystemClock.uptimeMillis()
            targetBeatTime = currentTime + newIntervalMs
            
            // Reset error accumulation to prevent artifacts from old tempo
            cumulativeError = 0
            latencyHistory.clear()
            
            Log.d(TAG, "Updated beat interval from ${oldInterval}ms to ${newIntervalMs}ms with timing reset")
        }
    }
    
    /**
     * Check if currently beating
     * 
     * @return true if beating is active, false otherwise
     */
    fun isBeating(): Boolean {
        return isBeatingActive
    }
    
    /**
     * Check if audio system is ready for playback
     * 
     * @return true if MediaPlayer is initialized and ready, false otherwise
     */
    fun isAudioReady(): Boolean {
        return try {
            mediaPlayer?.let { mp ->
                // Try to get duration to verify MediaPlayer is properly initialized
                mp.duration >= 0
            } ?: false
        } catch (e: Exception) {
            Log.w(TAG, "Audio readiness check failed: ${e.message}")
            false
        }
    }
    
    /**
     * Get timing precision statistics
     * 
     * @return Map containing timing metrics
     */
    fun getTimingStats(): Map<String, Long> {
        return mapOf(
            "beatCount" to beatCount,
            "averageError" to if (beatCount > 0) cumulativeError / beatCount else 0,
            "averageLatency" to averageLatency,
            "cumulativeError" to cumulativeError,
            "currentInterval" to beatInterval
        )
    }
    
    /**
     * Test audio playback capability
     * Attempts to initialize MediaPlayer if not already done
     * 
     * @return true if audio can be played, false otherwise
     */
    fun testAudio(): Boolean {
        return if (isAudioReady()) {
            true
        } else {
            initializeMediaPlayer()
        }
    }
    
    /**
     * Release MediaPlayer resources
     */
    private fun releaseMediaPlayer() {
        mediaPlayer?.let { mp ->
            try {
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing MediaPlayer: ${e.message}")
            }
        }
        mediaPlayer = null
    }
    
    /**
     * Release all resources
     * Should be called when AudioManager is no longer needed
     */
    fun release() {
        try {
            // Stop any ongoing beating
            stopBeating()
            
            // Remove any pending callbacks
            beatRunnable?.let { runnable ->
                handler.removeCallbacks(runnable)
            }
            beatRunnable = null
            
            // Release MediaPlayer resources
            releaseMediaPlayer()
            
            // Reset state
            beatInterval = 0
            lastBeatTime = 0
            isBeatingActive = false
            useFallbackAudio = false
            
            // Reset high-precision timing variables
            targetBeatTime = 0
            beatCount = 0
            cumulativeError = 0
            averageLatency = 0
            latencyHistory.clear()
            
            Log.d(TAG, "AudioManager resources released successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during AudioManager cleanup: ${e.message}")
        }
    }
}