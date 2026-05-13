package com.mauromarod.spaceflightnews.robot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.mauromarod.spaceflightnews.core.uicomponents.ErrorStateTags
import com.mauromarod.spaceflightnews.features.detail.DetailTags

class ArticleDetailRobot(private val rule: ComposeContentTestRule) {

    fun perform(block: ArticleDetailRobot.() -> Unit) = apply { block() }
    fun check(block: ArticleDetailRobot.() -> Unit) = apply { block() }

    fun tapReadFullArticle() {
        rule.onNodeWithTag(DetailTags.READ_BUTTON).performClick()
    }

    fun tapBack() {
        rule.onNodeWithTag(DetailTags.BACK_BUTTON).performClick()
    }

    // Article title comes from the API — not a localized string, text match is appropriate here.
    fun assertTitleVisible(title: String) {
        rule.onNodeWithText(title).assertIsDisplayed()
    }

    fun assertContentVisible() {
        rule.onNodeWithTag(DetailTags.CONTENT).assertIsDisplayed()
    }

    fun assertReadButtonVisible() {
        rule.onNodeWithTag(DetailTags.READ_BUTTON).assertIsDisplayed()
    }

    fun assertErrorStateVisible() {
        rule.onNodeWithTag(ErrorStateTags.CONTAINER).assertIsDisplayed()
    }

    fun assertNotFoundErrorVisible() {
        rule.onNodeWithTag(ErrorStateTags.CONTAINER).assertIsDisplayed()
    }
}
