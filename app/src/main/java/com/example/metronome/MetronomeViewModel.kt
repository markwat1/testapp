package com.example.metronome

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing metronome state and business logic
 * 
 * Handles BPM settings, playback control, and state management using StateFlow
 * Integrates with AudioManager for actual audio playback
 */
class MetronomeViewModel(application: Application) : AndroidViewModel(application) {
    
    // Audio manager for beat playback
    private val audioManager = AudioManager(application.applicationContext)
    
    // Private mutable state
    private val _state = MutableStateFlow(MetronomeState())
    
    // Public read-only state
    val state: StateFlow<MetronomeState> = _state.asStateFlow()
    
    /**
     * Set the BPM value
     * Automatically validates and clamps the value to acceptable range
     * Updates beat interval accordingly
     * If metronome is playing, updates the audio manager with new interval
     * 
     * @param newBpm New BPM value to set
     * @return true if BPM was set successfully, false if invalid input was rejected
     */
    fun setBpm(newBpm: Int): Boolean {
        // Validate input range
        if (!MetronomeState.isValidBpm(newBpm)) {
            // Log invalid input but still clamp and set the value
            Log.w("MetronomeViewModel", "Invalid BPM value: $newBpm. Clamping to valid range.")
        }
        
        val currentState = _state.value
        val newState = currentState.withBpm(newBpm)
        
        // Prevent unnecessary updates if BPM hasn't actually changed
        if (currentState.bpm == newState.bpm) {
            return true
        }
        
        _state.value = newState
        
        // Update audio manager if currently playing
        if (newState.isPlaying) {
            try {
                audioManager.updateBeatInterval(newState.beatInterval)
            } catch (e: Exception) {
                Log.e("MetronomeViewModel", "Failed to update beat interval: ${e.message}")
                return false
            }
        }
        
        return true
    }
    
    /**
     * Toggle playback state between playing and stopped
     * Handles audio manager start/stop accordingly
     * 
     * @return true if state transition was successful, false otherwise
     */
    fun togglePlayback(): Boolean {
        val currentState = _state.value
        val newPlayingState = !currentState.isPlaying
        
        return try {
            viewModelScope.launch {
                if (newPlayingState) {
                    if (!startMetronome()) {
                        Log.e("MetronomeViewModel", "Failed to start metronome")
                    }
                } else {
                    stopMetronome()
                }
            }
            true
        } catch (e: Exception) {
            Log.e("MetronomeViewModel", "Failed to toggle playback: ${e.message}")
            false
        }
    }
    
    /**
     * Start the metronome playback
     * Initializes audio manager and starts beating
     * 
     * @return true if metronome started successfully, false otherwise
     */
    fun startMetronome(): Boolean {
        val currentState = _state.value
        
        // Prevent starting if already playing
        if (currentState.isPlaying) {
            Log.w("MetronomeViewModel", "Metronome is already playing")
            return true
        }
        
        // Validate BPM before starting
        if (!MetronomeState.isValidBpm(currentState.bpm)) {
            Log.e("MetronomeViewModel", "Cannot start metronome with invalid BPM: ${currentState.bpm}")
            return false
        }
        
        return try {
            // Test audio capability before starting
            if (!audioManager.testAudio()) {
                Log.e("MetronomeViewModel", "Audio system not ready")
                return false
            }
            
            _state.value = currentState.withPlaying(true)
            
            // Start audio playback
            audioManager.startBeating(currentState.beatInterval)
            true
        } catch (e: Exception) {
            Log.e("MetronomeViewModel", "Failed to start metronome: ${e.message}")
            // Revert state if starting failed
            _state.value = currentState.withPlaying(false)
            false
        }
    }
    
    /**
     * Stop the metronome playback
     * Stops audio manager beating
     */
    fun stopMetronome() {
        val currentState = _state.value
        
        // Only stop if currently playing
        if (!currentState.isPlaying) {
            Log.w("MetronomeViewModel", "Metronome is already stopped")
            return
        }
        
        try {
            // Stop audio playback first
            audioManager.stopBeating()
            
            // Update state after successful stop
            _state.value = currentState.withPlaying(false)
        } catch (e: Exception) {
            Log.e("MetronomeViewModel", "Error stopping metronome: ${e.message}")
            // Force state update even if audio stop failed
            _state.value = currentState.withPlaying(false)
        }
    }
    
    /**
     * Get current BPM value
     * 
     * @return Current BPM
     */
    fun getCurrentBpm(): Int {
        return _state.value.bpm
    }
    
    /**
     * Get current playing state
     * 
     * @return true if metronome is playing, false otherwise
     */
    fun isCurrentlyPlaying(): Boolean {
        return _state.value.isPlaying
    }
    
    /**
     * Get current beat interval in milliseconds
     * 
     * @return Beat interval in milliseconds
     */
    fun getCurrentBeatInterval(): Long {
        return _state.value.beatInterval
    }
    
    /**
     * Reset metronome to default state
     * Stops any ongoing playback
     */
    fun reset() {
        stopMetronome()
        _state.value = MetronomeState()
    }
    
    /**
     * Test audio playback capability
     * 
     * @return true if audio system is ready, false otherwise
     */
    fun testAudio(): Boolean {
        return audioManager.testAudio()
    }
    
    /**
     * Play a single test beat
     * 
     * @return true if beat was played successfully, false otherwise
     */
    fun playTestBeat(): Boolean {
        return audioManager.playBeat()
    }
    
    /**
     * Check if audio system is ready
     * 
     * @return true if audio is ready for playback, false otherwise
     */
    fun isAudioReady(): Boolean {
        return audioManager.isAudioReady()
    }
    
    /**
     * Validate if the current state is consistent and valid
     * 
     * @return true if state is valid, false otherwise
     */
    fun validateState(): Boolean {
        val currentState = _state.value
        
        // Check BPM validity
        if (!MetronomeState.isValidBpm(currentState.bpm)) {
            Log.e("MetronomeViewModel", "Invalid BPM in state: ${currentState.bpm}")
            return false
        }
        
        // Check beat interval consistency
        val expectedInterval = MetronomeState.calculateInterval(currentState.bpm)
        if (currentState.beatInterval != expectedInterval) {
            Log.e("MetronomeViewModel", "Beat interval inconsistency: expected $expectedInterval, got ${currentState.beatInterval}")
            return false
        }
        
        // Check audio manager consistency if playing
        if (currentState.isPlaying && !audioManager.isBeating()) {
            Log.w("MetronomeViewModel", "State shows playing but audio manager is not beating")
            return false
        }
        
        return true
    }
    
    /**
     * Attempt to recover from invalid state
     * 
     * @return true if recovery was successful, false otherwise
     */
    fun recoverFromInvalidState(): Boolean {
        return try {
            val currentState = _state.value
            
            // Fix BPM if invalid
            val validBpm = MetronomeState.clampBpm(currentState.bpm)
            val correctedState = MetronomeState(
                bpm = validBpm,
                isPlaying = false, // Stop playback during recovery
                beatInterval = MetronomeState.calculateInterval(validBpm)
            )
            
            // Stop any ongoing audio
            audioManager.stopBeating()
            
            // Update to corrected state
            _state.value = correctedState
            
            Log.i("MetronomeViewModel", "Recovered from invalid state. BPM corrected to: $validBpm")
            true
        } catch (e: Exception) {
            Log.e("MetronomeViewModel", "Failed to recover from invalid state: ${e.message}")
            false
        }
    }
    
    /**
     * Clean up resources when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        
        try {
            // Stop any ongoing playback
            if (_state.value.isPlaying) {
                Log.d("MetronomeViewModel", "Stopping metronome during cleanup")
                stopMetronome()
            }
            
            // Release audio resources
            audioManager.release()
            
            Log.d("MetronomeViewModel", "ViewModel resources cleaned up")
        } catch (e: Exception) {
            Log.e("MetronomeViewModel", "Error during cleanup: ${e.message}")
        }
    }
    
    /**
     * Handle application going to background
     * Pauses metronome to save battery and resources
     */
    fun onAppGoingToBackground() {
        if (_state.value.isPlaying) {
            Log.d("MetronomeViewModel", "Pausing metronome - app going to background")
            stopMetronome()
        }
    }
    
    /**
     * Handle application coming to foreground
     * Can be used to restore state or reinitialize resources
     */
    fun onAppComingToForeground() {
        // Validate state when coming back to foreground
        if (!validateState()) {
            Log.w("MetronomeViewModel", "Invalid state detected when coming to foreground")
            recoverFromInvalidState()
        }
        
        // Test audio system
        if (!audioManager.testAudio()) {
            Log.w("MetronomeViewModel", "Audio system not ready when coming to foreground")
        }
    }
}