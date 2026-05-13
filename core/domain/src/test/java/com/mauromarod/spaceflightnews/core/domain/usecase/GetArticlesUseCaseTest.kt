package com.mauromarod.spaceflightnews.core.domain.usecase

import androidx.paging.PagingData
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
    fun `invoke delegates to repository and returns same flow`() {
        val expected = flowOf(PagingData.empty<Article>())
        every { repository.getArticles() } returns expected

        val result = useCase()

        assertSame(expected, result)
        verify(exactly = 1) { repository.getArticles() }
    }
}
