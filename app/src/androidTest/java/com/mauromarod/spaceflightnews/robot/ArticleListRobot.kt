package com.mauromarod.spaceflightnews.robot

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.mauromarod.spaceflightnews.core.uicomponents.ArticleCardTags
import com.mauromarod.spaceflightnews.core.uicomponents.ArticleSearchBarTags
import com.mauromarod.spaceflightnews.core.uicomponents.EmptyStateTags
import com.mauromarod.spaceflightnews.core.uicomponents.ErrorStateTags
import com.mauromarod.spaceflightnews.features.news.NewsTags

class ArticleListRobot(private val rule: ComposeContentTestRule) {

    fun perform(block: ArticleListRobot.() -> Unit) = apply { block() }
    fun check(block: ArticleListRobot.() -> Unit) = apply { block() }

    fun searchFor(query: String) {
        rule.onNodeWithTag(ArticleSearchBarTags.FIELD).performTextInput(query)
    }

    fun tapFirstArticle() {
        rule.onAllNodesWithTag(ArticleCardTags.CARD).onFirst().performClick()
    }

    fun tapRetry() {
        rule.onNodeWithTag(ErrorStateTags.RETRY_BUTTON).performClick()
    }

    fun assertListVisible() {
        rule.onNodeWithTag(NewsTags.ARTICLE_LIST).assertIsDisplayed()
    }

    fun assertArticleCount(count: Int) {
        rule.onAllNodesWithTag(ArticleCardTags.CARD).assertCountEquals(count)
    }

    fun assertEmptyStateVisible() {
        rule.onNodeWithTag(EmptyStateTags.CONTAINER).assertIsDisplayed()
    }

    fun assertErrorStateVisible() {
        rule.onNodeWithTag(ErrorStateTags.CONTAINER).assertIsDisplayed()
    }

    fun assertRetryButtonVisible() {
        rule.onNodeWithTag(ErrorStateTags.RETRY_BUTTON).assertIsDisplayed()
    }
}
