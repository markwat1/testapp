package com.example.metronome

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for MetronomeState data class
 * Tests BPM validation, calculation logic, and state creation methods
 */
class MetronomeStateTest {

    // BPM Validation Tests (Requirements 1.1, 1.2)

    @Test
    fun `isValidBpm returns true for valid BPM values`() {
        assertTrue("40 BPM should be valid", MetronomeState.isValidBpm(40))
        assertTrue("120 BPM should be valid", MetronomeState.isValidBpm(120))
        assertTrue("300 BPM should be valid", MetronomeState.isValidBpm(300))
        assertTrue("150 BPM should be valid", MetronomeState.isValidBpm(150))
    }

    @Test
    fun `isValidBpm returns false for invalid BPM values`() {
        assertFalse("39 BPM should be invalid", MetronomeState.isValidBpm(39))
        assertFalse("301 BPM should be invalid", MetronomeState.isValidBpm(301))
        assertFalse("0 BPM should be invalid", MetronomeState.isValidBpm(0))
        assertFalse("Negative BPM should be invalid", MetronomeState.isValidBpm(-10))
    }

    @Test
    fun `clampBpm returns correct values for boundary cases`() {
        assertEquals("Below minimum should clamp to minimum", 
            MetronomeState.MIN_BPM, MetronomeState.clampBpm(20))
        assertEquals("Above maximum should clamp to maximum", 
            MetronomeState.MAX_BPM, MetronomeState.clampBpm(400))
        assertEquals("Valid value should remain unchanged", 
            120, MetronomeState.clampBpm(120))
        assertEquals("Minimum boundary should remain unchanged", 
            MetronomeState.MIN_BPM, MetronomeState.clampBpm(MetronomeState.MIN_BPM))
        assertEquals("Maximum boundary should remain unchanged", 
            MetronomeState.MAX_BPM, MetronomeState.clampBpm(MetronomeState.MAX_BPM))
    }

    // Beat Interval Calculation Tests (Requirements 1.1)

    @Test
    fun `calculateInterval returns correct values for standard BPM`() {
        assertEquals("60 BPM should give 1000ms interval", 
            1000L, MetronomeState.calculateInterval(60))
        assertEquals("120 BPM should give 500ms interval", 
            500L, MetronomeState.calculateInterval(120))
        assertEquals("240 BPM should give 250ms interval", 
            250L, MetronomeState.calculateInterval(240))
    }

    @Test
    fun `calculateInterval handles boundary values correctly`() {
        assertEquals("Minimum BPM interval calculation", 
            1500L, MetronomeState.calculateInterval(MetronomeState.MIN_BPM))
        assertEquals("Maximum BPM interval calculation", 
            200L, MetronomeState.calculateInterval(MetronomeState.MAX_BPM))
    }

    @Test
    fun `calculateInterval precision for various BPM values`() {
        // Test precision for non-round numbers
        assertEquals("90 BPM should give 666ms interval", 
            666L, MetronomeState.calculateInterval(90))
        assertEquals("100 BPM should give 600ms interval", 
            600L, MetronomeState.calculateInterval(100))
        assertEquals("180 BPM should give 333ms interval", 
            333L, MetronomeState.calculateInterval(180))
    }

    // State Creation and Modification Tests (Requirements 2.1)

    @Test
    fun `default constructor creates valid initial state`() {
        val state = MetronomeState()
        
        assertEquals("Default BPM should be 120", MetronomeState.DEFAULT_BPM, state.bpm)
        assertFalse("Default playing state should be false", state.isPlaying)
        assertEquals("Default beat interval should match default BPM", 
            500L, state.beatInterval)
    }

    @Test
    fun `constructor with parameters creates correct state`() {
        val state = MetronomeState(bpm = 100, isPlaying = true)
        
        assertEquals("BPM should be set correctly", 100, state.bpm)
        assertTrue("Playing state should be set correctly", state.isPlaying)
        assertEquals("Beat interval should be calculated correctly", 600L, state.beatInterval)
    }

    @Test
    fun `withBpm creates new state with updated BPM and interval`() {
        val originalState = MetronomeState(bpm = 120, isPlaying = true)
        val newState = originalState.withBpm(100)
        
        assertEquals("New BPM should be set", 100, newState.bpm)
        assertEquals("Beat interval should be recalculated", 600L, newState.beatInterval)
        assertTrue("Playing state should be preserved", newState.isPlaying)
        
        // Original state should be unchanged
        assertEquals("Original state BPM should be unchanged", 120, originalState.bpm)
    }

    @Test
    fun `withBpm clamps invalid BPM values`() {
        val state = MetronomeState()
        
        val belowMinState = state.withBpm(20)
        assertEquals("Below minimum BPM should be clamped", 
            MetronomeState.MIN_BPM, belowMinState.bpm)
        
        val aboveMaxState = state.withBpm(400)
        assertEquals("Above maximum BPM should be clamped", 
            MetronomeState.MAX_BPM, aboveMaxState.bpm)
    }

    @Test
    fun `withPlaying creates new state with updated playing status`() {
        val originalState = MetronomeState(bpm = 100, isPlaying = false)
        val playingState = originalState.withPlaying(true)
        
        assertTrue("Playing state should be updated", playingState.isPlaying)
        assertEquals("BPM should be preserved", 100, playingState.bpm)
        assertEquals("Beat interval should be preserved", 600L, playingState.beatInterval)
        
        // Original state should be unchanged
        assertFalse("Original state playing should be unchanged", originalState.isPlaying)
    }

    @Test
    fun `state immutability is maintained`() {
        val originalState = MetronomeState(bpm = 120, isPlaying = false)
        val modifiedState = originalState.withBpm(100).withPlaying(true)
        
        // Original state should be completely unchanged
        assertEquals("Original BPM should be unchanged", 120, originalState.bpm)
        assertFalse("Original playing state should be unchanged", originalState.isPlaying)
        assertEquals("Original beat interval should be unchanged", 500L, originalState.beatInterval)
        
        // Modified state should have new values
        assertEquals("Modified BPM should be updated", 100, modifiedState.bpm)
        assertTrue("Modified playing state should be updated", modifiedState.isPlaying)
        assertEquals("Modified beat interval should be updated", 600L, modifiedState.beatInterval)
    }

    @Test
    fun `constants have correct values`() {
        assertEquals("MIN_BPM should be 40", 40, MetronomeState.MIN_BPM)
        assertEquals("MAX_BPM should be 300", 300, MetronomeState.MAX_BPM)
        assertEquals("DEFAULT_BPM should be 120", 120, MetronomeState.DEFAULT_BPM)
    }

    @Test
    fun `beat interval consistency with BPM changes`() {
        val state = MetronomeState(bpm = 60)
        assertEquals("Initial interval should match BPM", 1000L, state.beatInterval)
        
        val newState = state.withBpm(120)
        assertEquals("Updated interval should match new BPM", 500L, newState.beatInterval)
        
        val finalState = newState.withBpm(240)
        assertEquals("Final interval should match final BPM", 250L, finalState.beatInterval)
    }
}