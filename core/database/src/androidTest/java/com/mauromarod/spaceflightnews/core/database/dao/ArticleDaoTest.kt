package com.mauromarod.spaceflightnews.core.database.dao

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mauromarod.spaceflightnews.core.database.AppDatabase
import com.mauromarod.spaceflightnews.core.database.entity.ArticleEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArticleDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ArticleDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.articleDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAll_thenGetById_returnsInserted() = runTest {
        dao.insertAll(listOf(fakeEntity(id = 1), fakeEntity(id = 2)))

        assertNotNull(dao.getById(1))
        assertNotNull(dao.getById(2))
    }

    @Test
    fun insertAll_onConflictReplace_updatesExistingRow() = runTest {
        dao.insertAll(listOf(fakeEntity(id = 1, title = "Old Title")))
        dao.insertAll(listOf(fakeEntity(id = 1, title = "New Title")))

        val entity = dao.getById(1)
        assertEquals("New Title", entity?.title)
    }

    @Test
    fun clearAll_removesAllRows() = runTest {
        dao.insertAll(listOf(fakeEntity(1), fakeEntity(2)))
        dao.clearAll()

        assertNull(dao.getById(1))
        assertNull(dao.getById(2))
    }

    @Test
    fun searchPagingSource_returnsOnlyRowsMatchingQuery() = runTest {
        dao.insertAll(
            listOf(
                fakeEntity(id = 1, title = "SpaceX Starship launch"),
                fakeEntity(id = 2, title = "NASA Moon mission"),
                fakeEntity(id = 3, title = "Rocket Lab launch", summary = "SpaceX competitor")
            )
        )

        val result = dao.searchPagingSource("SpaceX").load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        ) as PagingSource.LoadResult.Page

        assertEquals(2, result.data.size)
        assertTrue(result.data.all { it.title.contains("SpaceX") || it.summary.contains("SpaceX") })
    }

    @Test
    fun getById_unknownId_returnsNull() = runTest {
        val result = dao.getById(999)
        assertNull(result)
    }

    private fun fakeEntity(
        id: Int,
        title: String = "Article $id",
        summary: String = "Summary $id"
    ) = ArticleEntity(
        id = id,
        title = title,
        summary = summary,
        imageUrl = null,
        newsSite = "SpaceNews",
        publishedAt = System.currentTimeMillis(),
        url = "https://example.com/$id",
        featured = false
    )
}
