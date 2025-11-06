package com.example.metronome

/**
 * Data class representing the state of the metronome
 * 
 * @param bpm Beats per minute (40-300 range)
 * @param isPlaying Whether the metronome is currently playing
 * @param beatInterval Calculated interval between beats in milliseconds
 */
data class MetronomeState(
    val bpm: Int = DEFAULT_BPM,
    val isPlaying: Boolean = false,
    val beatInterval: Long = calculateInterval(bpm)
) {
    companion object {
        const val MIN_BPM = 40
        const val MAX_BPM = 300
        const val DEFAULT_BPM = 120
        
        /**
         * Calculate the interval between beats in milliseconds based on BPM
         * 
         * @param bpm Beats per minute
         * @return Interval in milliseconds
         */
        fun calculateInterval(bpm: Int): Long {
            return 60000L / bpm
        }
        
        /**
         * Validate if the given BPM is within acceptable range
         * 
         * @param bpm Beats per minute to validate
         * @return true if BPM is valid, false otherwise
         */
        fun isValidBpm(bpm: Int): Boolean {
            return bpm in MIN_BPM..MAX_BPM
        }
        
        /**
         * Clamp BPM value to valid range
         * 
         * @param bpm Beats per minute to clamp
         * @return Clamped BPM value within valid range
         */
        fun clampBpm(bpm: Int): Int {
            return bpm.coerceIn(MIN_BPM, MAX_BPM)
        }
    }
    
    /**
     * Create a new state with updated BPM, automatically recalculating beat interval
     * 
     * @param newBpm New BPM value
     * @return New MetronomeState with updated BPM and beat interval
     */
    fun withBpm(newBpm: Int): MetronomeState {
        val validBpm = clampBpm(newBpm)
        return copy(
            bpm = validBpm,
            beatInterval = calculateInterval(validBpm)
        )
    }
    
    /**
     * Create a new state with updated playing status
     * 
     * @param playing New playing status
     * @return New MetronomeState with updated playing status
     */
    fun withPlaying(playing: Boolean): MetronomeState {
        return copy(isPlaying = playing)
    }
}