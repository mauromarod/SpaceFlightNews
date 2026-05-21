package com.mauromarod.spaceflightnews.core.domain.model

import org.junit.Assert.assertTrue
import org.junit.Test

class ArticleNotFoundExceptionTest {
    @Test
    fun `message contains the article id`() {
        val exception = ArticleNotFoundException(99)
        assertTrue(exception.message!!.contains("99"))
    }

    @Test
    fun `is instance of Exception`() {
        assertTrue(ArticleNotFoundException(1) is Exception)
    }
}
