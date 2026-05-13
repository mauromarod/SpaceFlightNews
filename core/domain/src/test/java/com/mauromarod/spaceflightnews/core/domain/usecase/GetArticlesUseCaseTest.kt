package com.mauromarod.spaceflightnews.core.domain.usecase

import com.mauromarod.spaceflightnews.core.domain.model.Article
import com.mauromarod.spaceflightnews.core.domain.repository.ArticleRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertSame
import org.junit.Test

class GetArticlesUseCaseTest {
    private val repository: ArticleRepository = mockk()
    private val useCase = GetArticlesUseCase(repository)

    @Test
    fun `invoke with blank query delegates to observeArticles`() {
        val expected = flowOf(listOf<Article>())
        every { repository.observeArticles() } returns expected

        val result = useCase("")

        assertSame(expected, result)
        verify(exactly = 1) { repository.observeArticles() }
    }

    @Test
    fun `invoke with query delegates to observeSearchedArticles`() {
        val expected = flowOf(listOf<Article>())
        every { repository.observeSearchedArticles("SpaceX") } returns expected

        val result = useCase("SpaceX")

        assertSame(expected, result)
        verify(exactly = 1) { repository.observeSearchedArticles("SpaceX") }
    }
}
