# Stage 02 — Core Domain Module

**Status:** ⬜ Pending
**Depends on:** Stage 01
**Estimated effort:** 1–2h
**Progress:** 0 / 8 tasks (0%)

---

## Objective

Implement the domain layer: pure Kotlin entities, repository interface, and use cases. This module has zero Android framework imports and zero dependency on `:core:data`, `:core:network`, or `:core:database`. It is the innermost layer — it depends on nothing and everything depends on it.

---

## Tasks

### Domain Model
- [ ] Create `Article.kt` — data class with fields: `id`, `title`, `summary`, `imageUrl`, `newsSite`, `publishedAt`, `url`, `featured` (use `java.time.Instant` for `publishedAt`)

### Repository Interface
- [ ] Create `ArticleRepository.kt` — interface with:
  - `fun getArticles(): Flow<PagingData<Article>>`
  - `suspend fun getArticleDetail(id: Int): Result<Article>`
  - `fun searchArticles(query: String): Flow<PagingData<Article>>`

### Use Cases
- [ ] Create `GetArticlesUseCase.kt` — delegates to `ArticleRepository.getArticles()`; `operator fun invoke()`
- [ ] Create `SearchArticlesUseCase.kt` — delegates to `ArticleRepository.searchArticles(query)`; `operator fun invoke(query: String)`
- [ ] Create `GetArticleDetailUseCase.kt` — delegates to `ArticleRepository.getArticleDetail(id)`; `operator fun invoke(id: Int)`

### Quality
- [ ] Add `archunit` dependency to `build.gradle.kts` (test scope)
- [ ] Create `DomainLayerArchTest.kt` — ArchUnit rule asserting no class in `core.domain` imports `android.*`
- [ ] Verify `./gradlew :core:domain:test` passes

---

## Acceptance Criteria

- `:core:domain` `build.gradle.kts` uses `org.jetbrains.kotlin.jvm` plugin — no Android plugin
- `Article` has no Moshi annotations, no Room annotations, no `@Parcelize`
- `ArticleRepository` is an `interface` — zero implementation code
- All three use cases compile with only `:core:domain` as a dependency
- `DomainLayerArchTest` passes and would fail if `android.util.Log` were imported in any domain class

---

## Implementation Notes

**Use case pattern:**
```kotlin
class GetArticlesUseCase @Inject constructor(
    private val repository: ArticleRepository
) {
    operator fun invoke(): Flow<PagingData<Article>> = repository.getArticles()
}
```

**Result type:** `suspend fun getArticleDetail` returns `kotlin.Result<Article>` (stdlib) — not a custom sealed class. The repository implementation in `:core:data` maps `NetworkResult<T>` to `Result<T>` before returning.

**Paging 3 in domain:** `PagingData` from `androidx.paging:paging-common` is the only AndroidX dependency allowed in `:core:domain`. Add it as an `api` dependency (not `implementation`) so feature modules that receive `PagingData` can use it transitively. `paging-common` is a pure Kotlin artifact with no Android framework code.

**`java.time.Instant`:** Available on Android API 26+. Since `minSdk = 24`, enable core library desugaring in modules that use `java.time.*`. Add `coreLibraryDesugaring` to `:app` and `:core:database` (where parsing happens), not to `:core:domain`.
