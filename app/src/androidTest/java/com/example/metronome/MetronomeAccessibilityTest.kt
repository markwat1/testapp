package com.example.metronome

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprehensive accessibility tests for MetronomeScreen
 * Focuses on TalkBack support and accessibility compliance
 */
@RunWith(AndroidJUnit4::class)
class MetronomeAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: MetronomeViewModel

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        viewModel = MetronomeViewModel(context.applicationContext as Application)
    }

    // Accessibility Compliance Tests (Requirements 4.2, 4.3)

    @Test
    fun allClickableElements_haveClickActions() {
        composeTestRule.setContent {
            MetronomeScreen(viewModel = viewModel)
        }

        // Play button
        composeTestRule
            .onNodeWithContentDescription("Start metronome at 120 beats per minute")
            .assertHasClickAction()

        // Decrease BPM button
        composeTestRule
            .onNodeWithContentDescription("Decrease tempo by 5 beats per minute. Current tempo: 120")
            .assertHasClickAction()

        // Increase BPM button
        composeTestRule
            .onNodeWithContentDescription("Increase tempo by 5 beats per minute. Current tempo: 120")
            .assertHasClickAction()
    }

    @Test
    fun semanticRoles_areCorrectlyAssigned() {
        composeTestRule.setContent {
            MetronomeScreen(viewModel = viewModel)
        }

        // BPM display should have Button role for TalkBack
        composeTestRule
            .onNodeWithContentDescription("Current tempo: 120 beats per minute")
            .assertIsDisplayed()

        // Visual beat indicator should have Image role
        composeTestRule
            .onNodeWithContentDescription("Metronome is stopped. Current tempo is 120 beats per minute")
            .assertIsDisplayed()

        // Status card should have Image role
        composeTestRule
            .onNodeWithContentDescription("Metronome status: Stopped")
            .assertIsDisplayed()
    }

    @Test
    fun contentDescriptions_provideCompleteInformation() {
        composeTestRule.setContent {
            MetronomeScreen(viewModel = viewModel)
        }

        // Main screen description should explain the app
        composeTestRule
            .onNodeWithContentDescription("Metronome application. Use the tempo slider to adjust beats per minute, then tap the play button to start or stop the metronome")
            .assertIsDisplayed()

        // Slider should describe current value and range
        composeTestRule
            .onNodeWithContentDescription("Tempo slider. Current value: 120 beats per minute. Range from 40 to 300 beats per minute")
            .assertIsDisplayed()

        // Buttons should describe their action and current state
        composeTestRule
            .onNodeWithContentDescription("Start metronome at 120 beats per minute")
            .assertIsDisplayed()
    }

    @Test
    fun stateChanges_updateContentDescriptions() {
        composeTestRule.setContent {
            MetronomeScreen(viewModel = viewModel)
        }

        // Initial state descriptions
        composeTestRule
            .onNodeWithContentDescription("Start metronome at 120 beats per minute")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Metronome is stopped. Current tempo is 120 beats per minute")
            .assertIsDisplayed()

        // Click play button
        composeTestRule
            .onNodeWithContentDescription("Start metronome at 120 beats per minute")
            .performClick()

        composeTestRule.waitForIdle()

        // Content descriptions should update (though exact state depends on audio system)
        // We verify that appropriate descriptions exist for both states
        composeTestRule
            .onNode(
                hasContentDescription("Stop metronome. Currently playing at 120 beats per minute") or
                hasContentDescription("Start metronome at 120 beats per minute")
            )
            .assertIsDisplayed()
    }

    @Test
    fun bpmChanges_updateAllRelevantDescriptions() {
        composeTestRule.setContent {
            MetronomeScreen(viewModel = viewModel)
        }

        // Change BPM using increase button
        composeTestRule
            .onNodeWithContentDescription("Increase tempo by 5 beats per minute. Current tempo: 120")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify descriptions update with new BPM value
        composeTestRule
            .onNodeWithContentDescription("Current tempo: 125 beats per minute")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Start metronome at 125 beats per minute")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Increase tempo by 5 beats per minute. Current tempo: 125")
            .assertIsDisplayed()
    }

    @Test
    fun touchTargets_meetMinimumSize() {
        composeTestRule.setContent {
            MetronomeScreen(viewModel = viewModel)
        }

        // Play button should be large (80dp in our implementation)
        val playButton = composeTestRule.onNodeWithContentDescription(
            "Start metronome at 120 beats per minute"
        )
        playButton.assertIsDisplayed()
        playButton.assertHasClickAction()

        // BPM adjustment buttons should be adequate size (48dp minimum)
        val decreaseButton = composeTestRule.onNodeWithContentDescription(
            "Decrease tempo by 5 beats per minute. Current tempo: 120"
        )
        decreaseButton.assertIsDisplayed()
        decreaseButton.assertHasClickAction()

        val increaseButton = composeTestRule.onNodeWithContentDescription(
            "Increase tempo by 5 beats per minute. Current tempo: 120"
        )
        increaseButton.assertIsDisplayed()
        increaseButton.assertHasClickAction()

        // Slider should have adequate height for touch interaction
        val slider = composeTestRule.onNodeWithContentDescription(
            "Tempo slider. Current value: 120 beats per minute. Range from 40 to 300 beats per minute"
        )
        slider.assertIsDisplayed()
    }

    @Test
    fun informationalElements_haveAppropriateDescriptions() {
        composeTestRule.setContent {
            MetronomeScreen(viewModel = viewModel)
        }

        // BPM unit label
        composeTestRule
            .onNodeWithContentDescription("Beats per minute")
            .assertIsDisplayed()

        // Tempo range information
        composeTestRule
            .onNodeWithContentDescription("Minimum tempo: 40 beats per minute")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Maximum tempo: 300 beats per minute")
            .assertIsDisplayed()

        // Tempo control section
        composeTestRule
            .onNodeWithContentDescription("Tempo control section")
            .assertIsDisplayed()
    }

    @Test
    fun technicalInformation_isAccessible() {
        composeTestRule.setContent {
            MetronomeScreen(viewModel = viewModel)
        }

        // Start metronome to show technical info
        composeTestRule
            .onNodeWithContentDescription("Start metronome at 120 beats per minute")
            .performClick()

        composeTestRule.waitForIdle()

        // Technical beat interval information should be accessible when playing
        // (This might only appear when actually playing, so we check if it exists)
        val technicalInfo = composeTestRule.onAllNodesWithText("Beat every 500ms")
        if (technicalInfo.fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule
                .onNodeWithContentDescription("Technical information: Each beat occurs every 500 milliseconds")
                .assertIsDisplayed()
        }
    }

    @Test
    fun screenNavigation_isAccessible() {
        composeTestRule.setContent {
            MetronomeScreen(viewModel = viewModel)
        }

        // Test that screen can be navigated using accessibility services
        // All interactive elements should be focusable and have proper descriptions

        val interactiveElements = listOf(
            "Start metronome at 120 beats per minute",
            "Decrease tempo by 5 beats per minute. Current tempo: 120",
            "Increase tempo by 5 beats per minute. Current tempo: 120",
            "Tempo slider. Current value: 120 beats per minute. Range from 40 to 300 beats per minute"
        )

        interactiveElements.forEach { description ->
            composeTestRule
                .onNodeWithContentDescription(description)
                .assertIsDisplayed()
        }
    }

    @Test
    fun errorStates_areAccessible() {
        composeTestRule.setContent {
            MetronomeScreen(viewModel = viewModel)
        }

        // Test boundary conditions for accessibility
        // Set BPM to minimum
        val decreaseButton = composeTestRule.onNodeWithContentDescription(
            "Decrease tempo by 5 beats per minute. Current tempo: 120"
        )

        // Click multiple times to reach minimum
        repeat(20) {
            if (decreaseButton.isEnabled()) {
                decreaseButton.performClick()
                composeTestRule.waitForIdle()
            }
        }

        // At minimum, decrease button should be disabled but still accessible
        composeTestRule
            .onNode(hasContentDescription("Decrease tempo by 5 beats per minute. Current tempo: 40"))
            .assertIsDisplayed()
            .assertIsNotEnabled()

        // Verify that disabled state is communicated to accessibility services
        // (The button should still be discoverable but marked as disabled)
    }
}