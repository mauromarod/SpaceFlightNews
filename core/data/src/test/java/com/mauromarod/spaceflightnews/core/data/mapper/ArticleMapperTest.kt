package com.mauromarod.spaceflightnews.core.data.mapper

import com.mauromarod.spaceflightnews.core.network.dto.ArticleDto
import com.mauromarod.spaceflightnews.core.network.dto.EventRefDto
import com.mauromarod.spaceflightnews.core.network.dto.LaunchRefDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ArticleMapperTest {

    private fun makeDto(
        id: Int = 1,
        title: String = "Test Title",
        url: String = "https://example.com/article",
        imageUrl: String? = "https://example.com/img.jpg",
        newsSite: String = "SpaceNews",
        summary: String = "Test summary",
        publishedAt: String = "2024-01-15T12:00:00Z",
        updatedAt: String = "2024-01-15T12:00:00Z",
        featured: Boolean = false
    ) = ArticleDto(
        id = id, title = title, url = url, imageUrl = imageUrl,
        newsSite = newsSite, summary = summary, publishedAt = publishedAt,
        updatedAt = updatedAt, featured = featured,
        launches = emptyList<LaunchRefDto>(), events = emptyList<EventRefDto>()
    )

    @Test
    fun `toDomain maps all fields correctly`() {
        val dto = makeDto()
        val domain = dto.toDomain()

        assertEquals(dto.id, domain.id)
        assertEquals(dto.title, domain.title)
        assertEquals(dto.summary, domain.summary)
        assertEquals(dto.imageUrl, domain.imageUrl)
        assertEquals(dto.newsSite, domain.newsSite)
        assertEquals(dto.url, domain.url)
        assertEquals(dto.featured, domain.featured)
        assertEquals(Instant.parse(dto.publishedAt), domain.publishedAt)
    }

    @Test
    fun `toDomain maps null imageUrl to null`() {
        val domain = makeDto(imageUrl = null).toDomain()
        assertNull(domain.imageUrl)
    }

    @Test
    fun `toDomain maps empty imageUrl to null`() {
        val domain = makeDto(imageUrl = "").toDomain()
        assertNull(domain.imageUrl)
    }

    @Test
    fun `toDomain maps blank imageUrl to null`() {
        val domain = makeDto(imageUrl = "   ").toDomain()
        assertNull(domain.imageUrl)
    }

    @Test
    fun `toEntity stores publishedAt as epoch millis`() {
        val publishedAt = "2024-01-15T12:00:00Z"
        val entity = makeDto(publishedAt = publishedAt).toEntity()

        assertEquals(Instant.parse(publishedAt).toEpochMilli(), entity.publishedAt)
    }

    @Test
    fun `toEntity maps null imageUrl to null`() {
        val entity = makeDto(imageUrl = null).toEntity()
        assertNull(entity.imageUrl)
    }

    @Test
    fun `toEntity maps blank imageUrl to null`() {
        val entity = makeDto(imageUrl = "").toEntity()
        assertNull(entity.imageUrl)
    }
}
