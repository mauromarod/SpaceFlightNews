package com.mauromarod.spaceflightnews.core.data.mapper

import com.mauromarod.spaceflightnews.core.database.entity.ArticleEntity
import com.mauromarod.spaceflightnews.core.database.mapper.toDomain
import com.mauromarod.spaceflightnews.core.database.mapper.toEntity
import com.mauromarod.spaceflightnews.core.domain.model.Article
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class ArticleEntityMapperTest {

    private val instant = Instant.parse("2024-03-10T08:30:00Z")
    private val epochMillis = instant.toEpochMilli()

    private val entity = ArticleEntity(
        id = 42,
        title = "SpaceX Launches Crew-8",
        summary = "Four astronauts are en route to the ISS.",
        imageUrl = "https://cdn.example.com/img.jpg",
        newsSite = "NASASpaceFlight",
        publishedAt = epochMillis,
        url = "https://nasaspaceflight.com/crew8",
        featured = true
    )

    private val domain = Article(
        id = 42,
        title = "SpaceX Launches Crew-8",
        summary = "Four astronauts are en route to the ISS.",
        imageUrl = "https://cdn.example.com/img.jpg",
        newsSite = "NASASpaceFlight",
        publishedAt = instant,
        url = "https://nasaspaceflight.com/crew8",
        featured = true
    )

    // --- ArticleEntity.toDomain() ---

    @Test
    fun `toDomain maps all fields correctly`() {
        val result = entity.toDomain()

        assertEquals(42, result.id)
        assertEquals("SpaceX Launches Crew-8", result.title)
        assertEquals("Four astronauts are en route to the ISS.", result.summary)
        assertEquals("https://cdn.example.com/img.jpg", result.imageUrl)
        assertEquals("NASASpaceFlight", result.newsSite)
        assertEquals(instant, result.publishedAt)
        assertEquals("https://nasaspaceflight.com/crew8", result.url)
        assertEquals(true, result.featured)
    }

    @Test
    fun `toDomain converts epochMillis to Instant correctly`() {
        val result = entity.toDomain()
        assertEquals(epochMillis, result.publishedAt.toEpochMilli())
    }

    @Test
    fun `toDomain preserves null imageUrl`() {
        val result = entity.copy(imageUrl = null).toDomain()
        assertNull(result.imageUrl)
    }

    @Test
    fun `toDomain preserves non-null imageUrl`() {
        val result = entity.toDomain()
        assertEquals("https://cdn.example.com/img.jpg", result.imageUrl)
    }

    @Test
    fun `toDomain preserves featured false`() {
        val result = entity.copy(featured = false).toDomain()
        assertEquals(false, result.featured)
    }

    // --- Article.toEntity() ---

    @Test
    fun `toEntity maps all fields correctly`() {
        val result = domain.toEntity()

        assertEquals(42, result.id)
        assertEquals("SpaceX Launches Crew-8", result.title)
        assertEquals("Four astronauts are en route to the ISS.", result.summary)
        assertEquals("https://cdn.example.com/img.jpg", result.imageUrl)
        assertEquals("NASASpaceFlight", result.newsSite)
        assertEquals(epochMillis, result.publishedAt)
        assertEquals("https://nasaspaceflight.com/crew8", result.url)
        assertEquals(true, result.featured)
    }

    @Test
    fun `toEntity converts Instant to epochMillis correctly`() {
        val result = domain.toEntity()
        assertEquals(epochMillis, result.publishedAt)
    }

    @Test
    fun `toEntity preserves null imageUrl`() {
        val result = domain.copy(imageUrl = null).toEntity()
        assertNull(result.imageUrl)
    }

    // --- Round-trip ---

    @Test
    fun `entity round-trip toDomain and back preserves all fields`() {
        val result = entity.toDomain().toEntity()

        assertEquals(entity.id, result.id)
        assertEquals(entity.title, result.title)
        assertEquals(entity.summary, result.summary)
        assertEquals(entity.imageUrl, result.imageUrl)
        assertEquals(entity.newsSite, result.newsSite)
        assertEquals(entity.publishedAt, result.publishedAt)
        assertEquals(entity.url, result.url)
        assertEquals(entity.featured, result.featured)
    }
}
