package com.mauromarod.spaceflightnews.core.uicomponents

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
class ArticleCardSnapshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun articleCard_light() {
        composeRule.setContent {
            SpaceFlightNewsTheme(darkTheme = false) {
                ArticleCard(
                    onClick = {},
                    image = { ShimmerBox() },
                    headline = {
                        Text(
                            text = "NASA reveals findings from James Webb Telescope",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    supporting = {
                        Text(
                            text = "Space.com · 2h ago",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            }
        }
        composeRule.onRoot().captureRoboImage("snapshots/roborazzi/article_card_light.png")
    }

    @Test
    fun articleCard_dark() {
        composeRule.setContent {
            SpaceFlightNewsTheme(darkTheme = true) {
                ArticleCard(
                    onClick = {},
                    image = { ShimmerBox() },
                    headline = {
                        Text(
                            text = "SpaceX Starship completes orbital test",
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    supporting = {
                        Text(
                            text = "SpaceNews · 5h ago",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            }
        }
        composeRule.onRoot().captureRoboImage("snapshots/roborazzi/article_card_dark.png")
    }
}
