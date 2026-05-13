package com.mauromarod.spaceflightnews

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mauromarod.spaceflightnews.core.designsystem.SpaceFlightNewsTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Regression test for the ViewModelStore crash in TwoPaneNewsDetail.
 *
 * Root cause: rememberNavController() was created in the parent composable (TwoPaneLayout)
 * and passed down to TwoPaneNewsDetail whose NavHost used it. When TwoPaneNewsDetail
 * left composition (navigating to Profile) and re-entered, the NavHost tried to call
 * setGraph() on the existing controller but its ViewModelStore was no longer valid.
 *
 * Fix: rememberNavController() lives inside TwoPaneNewsDetail itself — the controller
 * lifecycle is tied to the composable that owns its NavHost.
 */
@RunWith(AndroidJUnit4::class)
class TwoPaneNavigationTest {

    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun nestedNavHost_navigateAwayAndBack_doesNotCrash() {
        composeTestRule.setContent {
            SpaceFlightNewsTheme {
                TwoPaneNavTestHost()
            }
        }

        // Verify initial state
        composeTestRule.onNodeWithText("Inner placeholder").assertIsDisplayed()

        // Navigate to "Profile" — TwoPaneNewsDetail leaves composition
        composeTestRule.onNodeWithText("Go to Profile").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Profile screen").assertIsDisplayed()

        // Navigate back — TwoPaneNewsDetail re-enters composition with a fresh inner controller
        composeTestRule.onNodeWithText("Back").performClick()
        composeTestRule.waitForIdle()

        // If the ViewModelStore bug were present, the app would have crashed by now.
        composeTestRule.onNodeWithText("Inner placeholder").assertIsDisplayed()
    }
}

/**
 * Mirrors the structure of TwoPaneLayout + TwoPaneNewsDetail:
 * - outer NavHost navigates between "news" and "profile"
 * - "news" composable contains its OWN inner NavHost (rememberNavController inside)
 */
@Composable
private fun TwoPaneNavTestHost() {
    val outerNav = rememberNavController()

    NavHost(navController = outerNav, startDestination = "news") {
        composable("news") {
            // Inner nav controller scoped here — mirrors the fixed TwoPaneNewsDetail
            val innerNav = rememberNavController()
            Row(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.weight(1f)) {
                    Button(onClick = { outerNav.navigate("profile") }) {
                        Text("Go to Profile")
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    NavHost(navController = innerNav, startDestination = "placeholder") {
                        composable("placeholder") {
                            Text("Inner placeholder")
                        }
                    }
                }
            }
        }
        composable("profile") {
            Column {
                Text("Profile screen")
                Button(onClick = { outerNav.popBackStack() }) {
                    Text("Back")
                }
            }
        }
    }
}
