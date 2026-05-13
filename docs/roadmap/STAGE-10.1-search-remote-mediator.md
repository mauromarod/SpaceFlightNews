# Stage 10.1 — Search RemoteMediator

**Status:** ✅ Complete
**Depends on:** Stage 10
**Estimated effort:** 0.5h
**Progress:** 3 / 3 tasks (100%)

---

## Objective

Fix a critical UX gap: on fresh install, a user who searches before scrolling sees zero results because `searchArticles` had no `RemoteMediator` — only a local `PagingSource`. This stage adds `SearchRemoteMediator` to wire the search path to the network, keeping Room as the SSOT.

---

## Root Cause

`ArticleRepositoryImpl.searchArticles()` created a `Pager` with only `pagingSourceFactory`, no `remoteMediator`. The local `searchPagingSource(query)` queries the `articles` Room table with `LIKE '%' || :query || '%'`. On an empty database, this always returns zero results with no network fallback.

The `getArticles()` (scroll) path was already correct: it uses `ArticleRemoteMediator` which loads from the API and inserts into Room, triggering the reactive `PagingSource`.

---

## Tasks

- [x] Verify `GET /articles/?search=<phrase>` is supported by the Space Flight News API (confirmed via OpenAPI schema at `/v4/schema/?format=json` — `search` param: "Search for documents with a specific phrase in the title or summary")
- [x] Create `SearchRemoteMediator.kt` in `:core:data`
- [x] Wire `SearchRemoteMediator` into `ArticleRepositoryImpl.searchArticles()`

---

## Implementation

### `SearchRemoteMediator.kt`

**File:** `core/data/src/main/java/com/mauromarod/spaceflightnews/core/data/mediator/SearchRemoteMediator.kt`

```kotlin
@OptIn(ExperimentalPagingApi::class)
internal class SearchRemoteMediator(
    private val query: String,
    private val api: ArticleApi,
    private val articleDao: ArticleDao,
    private val pageSize: Int = ArticleRemoteMediator.PAGE_SIZE
) : RemoteMediator<Int, ArticleEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        val offset = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> state.pages.sumOf { it.data.size }
        }

        return when (val result = api.searchArticles(query = query, limit = pageSize, offset = offset)) {
            is NetworkResult.Success -> {
                articleDao.insertAll(result.data.results.map { it.toEntity() })
                MediatorResult.Success(endOfPaginationReached = result.data.next == null)
            }
            is NetworkResult.HttpError ->
                MediatorResult.Error(Exception("HTTP ${result.code}: ${result.message}"))
            is NetworkResult.NetworkError -> MediatorResult.Error(result.cause)
            is NetworkResult.UnknownError -> MediatorResult.Error(result.cause)
        }
    }
}
```

### `ArticleRepositoryImpl.kt` — diff

```kotlin
// Before
override fun searchArticles(query: String): Flow<PagingData<Article>> =
    Pager(
        config = pagingConfig,
        pagingSourceFactory = { articleDao.searchPagingSource(query) }
    ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

// After
override fun searchArticles(query: String): Flow<PagingData<Article>> =
    Pager(
        config = pagingConfig,
        remoteMediator = SearchRemoteMediator(query, api, articleDao),
        pagingSourceFactory = { articleDao.searchPagingSource(query) }
    ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
```

---

## Design Decisions

**No `RemoteKeysDao` for search:** Search pagination state is ephemeral and query-specific. `APPEND` offset is derived from `state.pages.sumOf { it.data.size }` — no need to persist it across app restarts. Each new search query triggers a fresh `REFRESH` anyway.

**No `clearAll()` on REFRESH:** `ArticleRemoteMediator` owns the table lifecycle (it clears on REFRESH to sync the main feed). `SearchRemoteMediator` only inserts/replaces. Clearing on search would wipe the paginated feed the user was browsing before switching to search.

**Same `articles` table as SSOT:** Network search results are inserted into the same table that `searchPagingSource(query)` queries. Since Room's `PagingSource` is reactive, the UI updates automatically once the mediator writes — no additional plumbing needed.

**`internal` visibility:** Not exposed outside `:core:data`. The repository's public interface is the only entry point.

---

## Data Flow (after fix)

```
User types "SpaceX" → debounce(300ms) → searchArticles("SpaceX")
                                               │
                              ┌────────────────┴────────────────┐
                              │                                  │
                    PagingSource (Room)               SearchRemoteMediator
                    searchPagingSource("SpaceX")      api.searchArticles("SpaceX", 20, 0)
                    (empty on fresh install → triggers │
                     RemoteMediator REFRESH)           articleDao.insertAll(results)
                              │                                  │
                              └──────── Room emits ─────────────┘
                                              │
                                         LazyColumn updates
```

---

## Acceptance Criteria

- Fresh install + search → results appear (network fetched, stored in Room, UI updates)
- Offline + empty DB + search → empty state (no crash, `MediatorResult.Error` handled by Paging)
- Scroll after search → main feed data intact (no data wipe)
- `./gradlew :core:data:compileDebugKotlin` → BUILD SUCCESSFUL
- `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL, zero Hilt errors
