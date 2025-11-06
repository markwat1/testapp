package com.example.metronome

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.metronome.ui.theme.MetronomeTheme
import kotlinx.coroutines.delay

/**
 * Main screen composable for the metronome application
 * 
 * Displays BPM controls, play/stop button, and visual feedback
 * Handles user interactions and binds them to ViewModel actions
 * 
 * @param viewModel The MetronomeViewModel for state management
 * @param modifier Modifier for the composable
 */
@Composable
fun MetronomeScreen(
    viewModel: MetronomeViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    
    // Handle BPM changes with proper debouncing
    var localBpm by remember { mutableIntStateOf(state.bpm) }
    
    // Update local BPM when state changes (e.g., from external sources)
    LaunchedEffect(state.bpm) {
        localBpm = state.bpm
    }
    
    // Beat animation state
    var beatAnimationTrigger by remember { mutableIntStateOf(0) }
    val beatScale by animateFloatAsState(
        targetValue = if (beatAnimationTrigger % 2 == 0) 1f else 1.2f,
        animationSpec = tween(
            durationMillis = 100,
            easing = FastOutSlowInEasing
        ),
        label = "beatScale"
    )
    
    // Button color animation
    val buttonColor by animateColorAsState(
        targetValue = if (state.isPlaying) 
            MaterialTheme.colorScheme.error 
        else 
            MaterialTheme.colorScheme.primary,
        animationSpec = tween(300),
        label = "buttonColor"
    )
    
    // Beat pulse effect when playing
    LaunchedEffect(state.isPlaying, state.beatInterval) {
        if (state.isPlaying) {
            while (state.isPlaying) {
                beatAnimationTrigger++
                delay(state.beatInterval)
            }
        }
    }
    
    // Accessibility announcements for state changes
    LaunchedEffect(state.isPlaying) {
        // This will be announced by TalkBack when the state changes
        // The announcement is handled by the semantics properties on UI elements
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp)
            .semantics {
                contentDescription = "Metronome application. Use the tempo slider to adjust beats per minute, then tap the play button to start or stop the metronome"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Beat Visual Indicator with accessibility support
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(beatScale)
                .semantics {
                    contentDescription = if (state.isPlaying) {
                        "Metronome is playing at ${state.bpm} beats per minute"
                    } else {
                        "Metronome is stopped. Current tempo is ${state.bpm} beats per minute"
                    }
                    role = Role.Image
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val radius = size.minDimension / 2
                val beatColor = if (state.isPlaying) {
                    Color(0xFF4CAF50) // Green when playing
                } else {
                    Color(0xFF9E9E9E) // Gray when stopped
                }
                
                drawCircle(
                    color = beatColor,
                    radius = radius * 0.8f
                )
                
                // Inner circle for beat pulse
                if (state.isPlaying) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.3f),
                        radius = radius * 0.6f
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // BPM Display - show local BPM for immediate feedback with accessibility
        Text(
            text = "$localBpm",
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics {
                contentDescription = "Current tempo: $localBpm beats per minute"
                role = Role.Button
            }
        )
        
        Text(
            text = "BPM",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 48.dp)
                .semantics {
                    contentDescription = "Beats per minute"
                }
        )
        
        // BPM Slider with enhanced accessibility
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Tempo",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .semantics {
                        contentDescription = "Tempo control section"
                    }
            )
            
            // BPM adjustment buttons for better accessibility
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrease BPM button
                Button(
                    onClick = {
                        val newBpm = (localBpm - 5).coerceAtLeast(MetronomeState.MIN_BPM)
                        localBpm = newBpm
                        viewModel.setBpm(newBpm)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .semantics {
                            contentDescription = "Decrease tempo by 5 beats per minute. Current tempo: $localBpm"
                        },
                    enabled = localBpm > MetronomeState.MIN_BPM
                ) {
                    Text("-5", fontSize = 12.sp)
                }
                
                // Slider
                Slider(
                    value = localBpm.toFloat(),
                    onValueChange = { newValue ->
                        localBpm = newValue.toInt()
                    },
                    onValueChangeFinished = {
                        // Update ViewModel when user finishes dragging
                        viewModel.setBpm(localBpm)
                    },
                    valueRange = MetronomeState.MIN_BPM.toFloat()..MetronomeState.MAX_BPM.toFloat(),
                    steps = MetronomeState.MAX_BPM - MetronomeState.MIN_BPM - 1,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .height(48.dp) // Ensure minimum touch target height
                        .semantics {
                            contentDescription = "Tempo slider. Current value: $localBpm beats per minute. Range from ${MetronomeState.MIN_BPM} to ${MetronomeState.MAX_BPM} beats per minute"
                            stateDescription = "Adjustable"
                        }
                )
                
                // Increase BPM button
                Button(
                    onClick = {
                        val newBpm = (localBpm + 5).coerceAtMost(MetronomeState.MAX_BPM)
                        localBpm = newBpm
                        viewModel.setBpm(newBpm)
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .semantics {
                            contentDescription = "Increase tempo by 5 beats per minute. Current tempo: $localBpm"
                        },
                    enabled = localBpm < MetronomeState.MAX_BPM
                ) {
                    Text("+5", fontSize = 12.sp)
                }
            }
            
            // BPM Range Labels with accessibility
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        contentDescription = "Tempo range from ${MetronomeState.MIN_BPM} to ${MetronomeState.MAX_BPM} beats per minute"
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${MetronomeState.MIN_BPM}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics {
                        contentDescription = "Minimum tempo: ${MetronomeState.MIN_BPM} beats per minute"
                    }
                )
                Text(
                    text = "${MetronomeState.MAX_BPM}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics {
                        contentDescription = "Maximum tempo: ${MetronomeState.MAX_BPM} beats per minute"
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Play/Stop Button with enhanced accessibility and large touch target
        FloatingActionButton(
            onClick = { viewModel.togglePlayback() },
            modifier = Modifier
                .size(80.dp) // Large touch target (minimum 48dp recommended, 80dp exceeds this)
                .semantics {
                    contentDescription = if (state.isPlaying) {
                        "Stop metronome. Currently playing at $localBpm beats per minute"
                    } else {
                        "Start metronome at $localBpm beats per minute"
                    }
                    role = Role.Button
                    stateDescription = if (state.isPlaying) "Playing" else "Stopped"
                },
            containerColor = buttonColor,
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null, // Handled by parent semantics
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Status Text with enhanced visual feedback and accessibility
        Card(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .semantics {
                    contentDescription = if (state.isPlaying) {
                        "Metronome status: Currently playing at ${state.bpm} beats per minute"
                    } else {
                        "Metronome status: Stopped"
                    }
                    role = Role.Image
                },
            colors = CardDefaults.cardColors(
                containerColor = if (state.isPlaying) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = if (state.isPlaying) "♪ Playing" else "⏸ Stopped",
                style = MaterialTheme.typography.titleMedium,
                color = if (state.isPlaying) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .semantics {
                        contentDescription = null // Handled by parent Card
                    },
                textAlign = TextAlign.Center
            )
        }
        
        // Additional tempo information with accessibility
        if (state.isPlaying) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Beat every ${state.beatInterval}ms",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics {
                    contentDescription = "Technical information: Each beat occurs every ${state.beatInterval} milliseconds"
                }
            )
        }
    }
}

/**
 * Preview for MetronomeScreen in light theme
 */
@Preview(showBackground = true)
@Composable
fun MetronomeScreenPreview() {
    MetronomeTheme {
        // Create a mock ViewModel for preview
        val context = LocalContext.current
        val mockViewModel = remember { 
            MetronomeViewModel(context.applicationContext as android.app.Application) 
        }
        MetronomeScreen(viewModel = mockViewModel)
    }
}

/**
 * Preview for MetronomeScreen in dark theme
 */
@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MetronomeScreenDarkPreview() {
    MetronomeTheme {
        // Create a mock ViewModel for preview
        val context = LocalContext.current
        val mockViewModel = remember { 
            MetronomeViewModel(context.applicationContext as android.app.Application) 
        }
        MetronomeScreen(viewModel = mockViewModel)
    }
}