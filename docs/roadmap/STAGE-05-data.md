# Stage 05 — Core Data Module

**Status:** ✅ Complete
**Depends on:** Stage 02, Stage 03, Stage 04
**Estimated effort:** 3–4h
**Progress:** 9 / 9 tasks (100%)

---

## Objective

Implement the data layer: the `ArticleMapper` that converts network DTOs to domain models, the `ArticleRemoteMediator` that orchestrates Paging 3 network+database sync, and `ArticleRepositoryImpl` that ties everything together and fulfills the `ArticleRepository` interface contract.

---

## Tasks

### Mapper
- [x] Create `ArticleMapper.kt` — `ArticleDto.toDomain(): Article`; normalize `imageUrl` (empty string → `null`); parse `published_at` ISO 8601 string to `Instant` via `Instant.parse()`

### Remote Mediator
- [x] Create `ArticleRemoteMediator.kt` — `RemoteMediator<Int, ArticleEntity>` implementation:
  - `LoadType.REFRESH`: clear `remote_keys` + `articles` tables, fetch page 0, insert both
  - `LoadType.APPEND`: read next key from `RemoteKeysDao`, fetch next page, insert both
  - `LoadType.PREPEND`: return `MediatorResult.Success(endOfPaginationReached = true)` (no backwards pagination)
  - Return `MediatorResult.Error(cause)` on `NetworkResult.HttpError` or `NetworkResult.NetworkError`
  - Use `database.withTransaction { }` for all DB writes in a single atomic operation

### Repository Implementation
- [x] Create `ArticleRepositoryImpl.kt` — implements `ArticleRepository`:
  - `getArticles()`: creates `Pager` with `PagingConfig(pageSize = 20, prefetchDistance = 5)`, `ArticleRemoteMediator`, and Room `PagingSource` from `ArticleDao`
  - `searchArticles(query)`: creates `Pager` with Room's filtered `PagingSource`; no `RemoteMediator` for search (search is local-first on cached data, with a network search triggered via a separate refresh)
  - `getArticleDetail(id)`: checks Room first → returns cached article; on cache miss, fetches from network, inserts into Room, returns domain model; maps `NetworkResult<T>` to `kotlin.Result<T>`

### Hilt Module
- [x] Create `RepositoryModule.kt` — `@Module @InstallIn(SingletonComponent::class)` binding `ArticleRepository` to `ArticleRepositoryImpl` via `@Binds`

### Stale Data Detection
- [x] Add `lastFetchedAt` field to `RemoteKeysEntity` (timestamp of last `REFRESH`); expose logic in `ArticleRepositoryImpl.isDataStale(ttlMinutes: Int): Boolean`

### Quality
- [x] Create `ArticleMapperTest.kt` — unit tests: valid DTO, null `image_url`, empty string `image_url`, valid ISO 8601 date, malformed date (should throw or return a sensible fallback)
- [x] Create `ArticleRemoteMediatorTest.kt` — using `MockWebServer` + in-memory Room; test `REFRESH` clears tables and inserts; test `APPEND` uses correct offset; test network error returns `MediatorResult.Error`
- [x] Create `ArticleRepositoryImplTest.kt` — unit test `getArticleDetail`: cache hit path and network fallback path using `FakeArticleDao` and `FakeArticleApi`

---

## Acceptance Criteria

- `ArticleRemoteMediator` uses `database.withTransaction` for all multi-table writes
- `ArticleMapper` handles `null`, empty string, and valid URL for `imageUrl` without throwing
- `getArticleDetail` returns cached data without a network call when the article is in Room
- `RepositoryModule` uses `@Binds` (not `@Provides`) to bind the interface — `@Binds` generates less code than `@Provides`
- All three test classes pass via `./gradlew :core:data:test`

---

## Implementation Notes

**Search strategy:** The `searchArticles(query)` flow uses Room's filtered `PagingSource` without a `RemoteMediator`. This means search operates over locally cached articles. This is an intentional design choice — the API's `search` parameter is used only during the initial `REFRESH` load (or manually triggered). For this app's scope it is acceptable. If the search needs to hit the network directly, `RemoteMediator` can be added with the `search` query passed at construction time.

**`PagingConfig` settings:**
```kotlin
PagingConfig(
    pageSize = 20,
    prefetchDistance = 5,
    enablePlaceholders = false,
    initialLoadSize = 20
)
```
`enablePlaceholders = false` simplifies the list adapter — no need to handle `null` items in the Compose list.

**Remote key calculation:**
```kotlin
val nextKey = if (response.next == null) null else (currentOffset + pageSize)
val prevKey = if (response.previous == null) null else (currentOffset - pageSize)
```
The Space Flight News API uses offset-based pagination, not cursor-based. The `next` and `previous` fields are full URLs; parse only their presence (non-null) to determine if there are more pages.

**`kotlin.Result<T>` mapping from `NetworkResult<T>`:**
```kotlin
NetworkResult.Success(data) → Result.success(data.toDomain())
NetworkResult.HttpError(404, _) → Result.failure(ArticleNotFoundException(id))
NetworkResult.NetworkError(cause) → Result.failure(cause)
NetworkResult.UnknownError(cause) → Result.failure(cause)
```
Define `ArticleNotFoundException` in `:core:domain` as a domain-specific exception.
