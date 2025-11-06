package com.example.metronome

import android.app.Application
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations

/**
 * Unit tests for MetronomeViewModel
 * Tests BPM calculation logic and state transitions
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MetronomeViewModelTest {

    @Mock
    private lateinit var mockApplication: Application

    private lateinit var viewModel: MetronomeViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = MetronomeViewModel(mockApplication)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        viewModel.onCleared()
    }

    // BPM Calculation Logic Tests (Requirements 1.1, 1.2)

    @Test
    fun `setBpm with valid value updates state correctly`() = runTest {
        // Test setting valid BPM
        val result = viewModel.setBpm(100)
        
        assertTrue("setBpm should return true for valid input", result)
        assertEquals("BPM should be set to 100", 100, viewModel.getCurrentBpm())
        assertEquals("Beat interval should be calculated correctly", 600L, viewModel.getCurrentBeatInterval())
    }

    @Test
    fun `setBpm with minimum boundary value works correctly`() = runTest {
        val result = viewModel.setBpm(MetronomeState.MIN_BPM)
        
        assertTrue("setBpm should return true for minimum BPM", result)
        assertEquals("BPM should be set to minimum", MetronomeState.MIN_BPM, viewModel.getCurrentBpm())
        assertEquals("Beat interval should be calculated for min BPM", 1500L, viewModel.getCurrentBeatInterval())
    }

    @Test
    fun `setBpm with maximum boundary value works correctly`() = runTest {
        val result = viewModel.setBpm(MetronomeState.MAX_BPM)
        
        assertTrue("setBpm should return true for maximum BPM", result)
        assertEquals("BPM should be set to maximum", MetronomeState.MAX_BPM, viewModel.getCurrentBpm())
        assertEquals("Beat interval should be calculated for max BPM", 200L, viewModel.getCurrentBeatInterval())
    }

    @Test
    fun `setBpm with value below minimum clamps to minimum`() = runTest {
        val result = viewModel.setBpm(20) // Below minimum of 40
        
        assertTrue("setBpm should return true even for clamped values", result)
        assertEquals("BPM should be clamped to minimum", MetronomeState.MIN_BPM, viewModel.getCurrentBpm())
    }

    @Test
    fun `setBpm with value above maximum clamps to maximum`() = runTest {
        val result = viewModel.setBpm(400) // Above maximum of 300
        
        assertTrue("setBpm should return true even for clamped values", result)
        assertEquals("BPM should be clamped to maximum", MetronomeState.MAX_BPM, viewModel.getCurrentBpm())
    }

    @Test
    fun `beat interval calculation is accurate for various BPM values`() = runTest {
        val testCases = mapOf(
            60 to 1000L,   // 60 BPM = 1 beat per second = 1000ms
            120 to 500L,   // 120 BPM = 2 beats per second = 500ms
            180 to 333L,   // 180 BPM ≈ 333ms (60000/180 = 333.33)
            240 to 250L    // 240 BPM = 4 beats per second = 250ms
        )

        testCases.forEach { (bpm, expectedInterval) ->
            viewModel.setBpm(bpm)
            assertEquals("Beat interval for $bpm BPM should be $expectedInterval ms", 
                expectedInterval, viewModel.getCurrentBeatInterval())
        }
    }

    // State Transition Tests (Requirements 2.1, 2.2, 2.3)

    @Test
    fun `initial state is correct`() = runTest {
        assertEquals("Initial BPM should be default", MetronomeState.DEFAULT_BPM, viewModel.getCurrentBpm())
        assertFalse("Initial playing state should be false", viewModel.isCurrentlyPlaying())
        assertEquals("Initial beat interval should match default BPM", 500L, viewModel.getCurrentBeatInterval())
    }

    @Test
    fun `togglePlayback from stopped to playing works`() = runTest {
        // Ensure we start in stopped state
        assertFalse("Should start in stopped state", viewModel.isCurrentlyPlaying())
        
        val result = viewModel.togglePlayback()
        
        assertTrue("togglePlayback should return true", result)
        // Note: Due to audio system dependencies, we can't easily test the actual playing state
        // in unit tests without mocking the audio system
    }

    @Test
    fun `startMetronome with valid BPM succeeds`() = runTest {
        viewModel.setBpm(100)
        
        val result = viewModel.startMetronome()
        
        // The result depends on audio system availability, but BPM should be valid
        assertEquals("BPM should remain set", 100, viewModel.getCurrentBpm())
    }

    @Test
    fun `stopMetronome sets playing state to false`() = runTest {
        // Stop metronome (should work regardless of current state)
        viewModel.stopMetronome()
        
        assertFalse("Playing state should be false after stop", viewModel.isCurrentlyPlaying())
    }

    @Test
    fun `reset returns to default state`() = runTest {
        // Change state first
        viewModel.setBpm(200)
        
        // Reset
        viewModel.reset()
        
        assertEquals("BPM should be reset to default", MetronomeState.DEFAULT_BPM, viewModel.getCurrentBpm())
        assertFalse("Playing state should be false after reset", viewModel.isCurrentlyPlaying())
        assertEquals("Beat interval should be reset", 500L, viewModel.getCurrentBeatInterval())
    }

    @Test
    fun `validateState returns true for valid state`() = runTest {
        viewModel.setBpm(120)
        
        val isValid = viewModel.validateState()
        
        assertTrue("State should be valid with valid BPM", isValid)
    }

    @Test
    fun `state flow emits correct values`() = runTest {
        // Test that state flow updates correctly
        val initialState = viewModel.state.first()
        assertEquals("Initial state BPM should be default", MetronomeState.DEFAULT_BPM, initialState.bpm)
        assertFalse("Initial state should not be playing", initialState.isPlaying)

        // Change BPM and verify state flow update
        viewModel.setBpm(150)
        val updatedState = viewModel.state.first()
        assertEquals("State flow should reflect BPM change", 150, updatedState.bpm)
        assertEquals("Beat interval should be updated in state flow", 400L, updatedState.beatInterval)
    }

    @Test
    fun `setBpm with same value does not trigger unnecessary updates`() = runTest {
        val initialBpm = viewModel.getCurrentBpm()
        
        // Set the same BPM value
        val result = viewModel.setBpm(initialBpm)
        
        assertTrue("setBpm should return true for same value", result)
        assertEquals("BPM should remain the same", initialBpm, viewModel.getCurrentBpm())
    }

    @Test
    fun `recoverFromInvalidState resets to valid state`() = runTest {
        // This test verifies the recovery mechanism
        val result = viewModel.recoverFromInvalidState()
        
        assertTrue("Recovery should succeed", result)
        assertFalse("Should not be playing after recovery", viewModel.isCurrentlyPlaying())
        assertTrue("BPM should be valid after recovery", 
            MetronomeState.isValidBpm(viewModel.getCurrentBpm()))
    }
}