package com.mauromarod.spaceflightnews.core.domain.usecase

import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.domain.repository.ArticleRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertSame
import org.junit.Test

class SearchArticlesUseCaseTest {
    private val repository: ArticleRepository = mockk()
    private val useCase = SearchArticlesUseCase(repository)

    @Test
    fun `invoke passes query to repository and returns same flow`() {
        val expected = flowOf(listOf<Article>())
        every { repository.observeSearchedArticles("SpaceX") } returns expected

        val result = useCase("SpaceX")

        assertSame(expected, result)
        verify(exactly = 1) { repository.observeSearchedArticles("SpaceX") }
    }

    @Test
    fun `invoke with empty query delegates with empty string`() {
        val expected = flowOf(listOf<Article>())
        every { repository.observeSearchedArticles("") } returns expected

        val result = useCase("")

        assertSame(expected, result)
        verify(exactly = 1) { repository.observeSearchedArticles("") }
    }
}
