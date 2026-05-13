package com.mauromarod.spaceflightnews.core.uicomponents

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.mauromarod.spaceflightnews.core.designsystem.SpaceFlightNewsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class StateComponentsSnapshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadingState_light() {
        composeRule.setContent {
            SpaceFlightNewsTheme(darkTheme = false) { LoadingState() }
        }
        composeRule.onRoot().captureRoboImage("snapshots/roborazzi/loading_state_light.png")
    }

    @Test
    fun loadingState_dark() {
        composeRule.setContent {
            SpaceFlightNewsTheme(darkTheme = true) { LoadingState() }
        }
        composeRule.onRoot().captureRoboImage("snapshots/roborazzi/loading_state_dark.png")
    }

    @Test
    fun emptyState_light() {
        composeRule.setContent {
            SpaceFlightNewsTheme(darkTheme = false) {
                EmptyState(title = "No results", subtitle = "Try a different search term")
            }
        }
        composeRule.onRoot().captureRoboImage("snapshots/roborazzi/empty_state_light.png")
    }

    @Test
    fun emptyState_dark() {
        composeRule.setContent {
            SpaceFlightNewsTheme(darkTheme = true) {
                EmptyState(title = "No results", subtitle = "Try a different search term")
            }
        }
        composeRule.onRoot().captureRoboImage("snapshots/roborazzi/empty_state_dark.png")
    }

    @Test
    fun errorState_light() {
        composeRule.setContent {
            SpaceFlightNewsTheme(darkTheme = false) {
                ErrorState(message = "No internet connection", onRetry = {})
            }
        }
        composeRule.onRoot().captureRoboImage("snapshots/roborazzi/error_state_light.png")
    }

    @Test
    fun errorState_dark() {
        composeRule.setContent {
            SpaceFlightNewsTheme(darkTheme = true) {
                ErrorState(message = "Something went wrong", onRetry = {})
            }
        }
        composeRule.onRoot().captureRoboImage("snapshots/roborazzi/error_state_dark.png")
    }
}
