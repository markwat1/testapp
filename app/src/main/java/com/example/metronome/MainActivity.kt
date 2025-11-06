package com.example.metronome

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.metronome.ui.theme.MetronomeTheme

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        private const val KEY_WAS_PLAYING = "was_playing_before_pause"
    }
    
    private var metronomeViewModel: MetronomeViewModel? = null
    private var wasPlayingBeforePause = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Restore state if available
        savedInstanceState?.let { bundle ->
            wasPlayingBeforePause = bundle.getBoolean(KEY_WAS_PLAYING, false)
            Log.d(TAG, "Restored state: wasPlayingBeforePause = $wasPlayingBeforePause")
        }
        
        enableEdgeToEdge()
        setContent {
            MetronomeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val viewModel: MetronomeViewModel = viewModel()
                        
                        // Store reference to ViewModel for lifecycle management
                        metronomeViewModel = viewModel
                        
                        MetronomeScreen(viewModel = viewModel)
                    }
                }
            }
        }
        
        Log.d(TAG, "Activity created")
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        
        // Save current playing state
        val isCurrentlyPlaying = metronomeViewModel?.isCurrentlyPlaying() ?: false
        outState.putBoolean(KEY_WAS_PLAYING, isCurrentlyPlaying)
        
        Log.d(TAG, "Saved state: isCurrentlyPlaying = $isCurrentlyPlaying")
    }
    
    override fun onPause() {
        super.onPause()
        
        metronomeViewModel?.let { viewModel ->
            // Remember if metronome was playing before pause
            wasPlayingBeforePause = viewModel.isCurrentlyPlaying()
            
            if (wasPlayingBeforePause) {
                Log.d(TAG, "Pausing metronome due to activity pause")
                viewModel.stopMetronome()
            }
            
            // Validate state before pausing
            if (!viewModel.validateState()) {
                Log.w(TAG, "Invalid state detected during pause, attempting recovery")
                viewModel.recoverFromInvalidState()
            }
        }
        
        Log.d(TAG, "Activity paused, wasPlayingBeforePause = $wasPlayingBeforePause")
    }
    
    override fun onResume() {
        super.onResume()
        
        metronomeViewModel?.let { viewModel ->
            // Validate state after resume
            if (!viewModel.validateState()) {
                Log.w(TAG, "Invalid state detected during resume, attempting recovery")
                if (!viewModel.recoverFromInvalidState()) {
                    Log.e(TAG, "Failed to recover from invalid state")
                    return
                }
            }
            
            // Test audio system after resume
            if (!viewModel.testAudio()) {
                Log.w(TAG, "Audio system not ready after resume")
            }
            
            // Restore playing state if it was playing before pause
            if (wasPlayingBeforePause && !viewModel.isCurrentlyPlaying()) {
                Log.d(TAG, "Resuming metronome after activity resume")
                if (!viewModel.startMetronome()) {
                    Log.e(TAG, "Failed to resume metronome playback")
                }
            }
            
            // Reset the flag
            wasPlayingBeforePause = false
        }
        
        Log.d(TAG, "Activity resumed")
    }
    
    override fun onStop() {
        super.onStop()
        
        metronomeViewModel?.let { viewModel ->
            // Stop metronome when activity is no longer visible
            if (viewModel.isCurrentlyPlaying()) {
                Log.d(TAG, "Stopping metronome due to activity stop")
                viewModel.stopMetronome()
            }
        }
        
        Log.d(TAG, "Activity stopped")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        metronomeViewModel?.let { viewModel ->
            // Ensure metronome is stopped
            if (viewModel.isCurrentlyPlaying()) {
                Log.d(TAG, "Stopping metronome due to activity destroy")
                viewModel.stopMetronome()
            }
            
            // ViewModel will handle resource cleanup in onCleared()
        }
        
        // Clear reference
        metronomeViewModel = null
        
        Log.d(TAG, "Activity destroyed")
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        
        metronomeViewModel?.let { viewModel ->
            // Stop metronome to free up resources during low memory
            if (viewModel.isCurrentlyPlaying()) {
                Log.d(TAG, "Stopping metronome due to low memory")
                viewModel.stopMetronome()
            }
        }
        
        Log.d(TAG, "Low memory situation handled")
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        
        metronomeViewModel?.let { viewModel ->
            when (level) {
                TRIM_MEMORY_UI_HIDDEN -> {
                    // App UI is no longer visible
                    Log.d(TAG, "UI hidden - stopping metronome")
                    viewModel.onAppGoingToBackground()
                }
                TRIM_MEMORY_BACKGROUND,
                TRIM_MEMORY_MODERATE,
                TRIM_MEMORY_COMPLETE -> {
                    // App is in background and system needs memory
                    Log.d(TAG, "Memory pressure (level: $level) - stopping metronome")
                    if (viewModel.isCurrentlyPlaying()) {
                        viewModel.stopMetronome()
                    }
                }
                TRIM_MEMORY_RUNNING_CRITICAL,
                TRIM_MEMORY_RUNNING_LOW,
                TRIM_MEMORY_RUNNING_MODERATE -> {
                    // App is running but system is under memory pressure
                    Log.d(TAG, "Running under memory pressure (level: $level)")
                    // Don't stop metronome but validate state
                    if (!viewModel.validateState()) {
                        viewModel.recoverFromInvalidState()
                    }
                }
            }
        }
        
        Log.d(TAG, "Memory trim handled for level: $level")
    }
}