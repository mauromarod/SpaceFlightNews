# Stage 04 — Core Database Module

**Status:** ✅ Complete
**Depends on:** Stage 01, Stage 02
**Estimated effort:** 2–3h
**Progress:** 10 / 10 tasks (100%)

---

## Objective

Implement the local persistence layer using Room. The database is the Single Source of Truth (SSOT) — the UI always reads from Room, never directly from the network. This stage also includes the entity-to-domain mapper that transforms Room entities into domain models.

---

## Tasks

### Entities
- [x] Create `ArticleEntity.kt` — `@Entity(tableName = "articles")` with all persisted fields; `@PrimaryKey val id: Int`; store `publishedAt` as a `Long` (epoch millis) for SQLite compatibility
- [x] Create `RemoteKeysEntity.kt` — `@Entity(tableName = "remote_keys")`; fields: `articleId: Int` (PK), `prevKey: Int?`, `nextKey: Int?`

### DAOs
- [x] Create `ArticleDao.kt` — `@Dao` with:
  - `@Query` returning `PagingSource<Int, ArticleEntity>` for list
  - `@Query` returning `PagingSource<Int, ArticleEntity>` filtered by search term (`LIKE '%' || :query || '%'`)
  - `@Query suspend fun getArticleById(id: Int): ArticleEntity?`
  - `@Insert(onConflict = REPLACE) suspend fun insertAll(articles: List<ArticleEntity>)`
  - `@Query suspend fun clearAll()`
- [x] Create `RemoteKeysDao.kt` — `@Dao` with: `getRemoteKeyByArticleId`, `insertAll`, `clearAll`

### Database
- [x] Create `AppDatabase.kt` — `@Database(entities = [ArticleEntity::class, RemoteKeysEntity::class], version = 1)`; expose `articleDao()` and `remoteKeysDao()`

### Mapper
- [x] Create `ArticleEntityMapper.kt` — `ArticleEntity.toDomain(): Article`; parse `publishedAt Long` back to `Instant`

### Hilt Module
- [x] Create `DatabaseModule.kt` — `@Module @InstallIn(SingletonComponent::class)` providing `AppDatabase` (in-memory for tests, file-backed for production), `ArticleDao`, `RemoteKeysDao`

### Quality
- [x] Create `ArticleDaoTest.kt` — instrumented test using `Room.inMemoryDatabaseBuilder`; verify `insertAll`, `clearAll`, search query filtering, and `PagingSource` emissions

---

## Acceptance Criteria

- `AppDatabase` compiles with Room's schema validation enabled (`room.schemaLocation` exported)
- `ArticleDao` search query is tested: inserting articles and querying with a term returns only matching rows
- `ArticleEntityMapper` correctly converts `Long` epoch millis back to `Instant` without data loss
- `./gradlew :core:database:connectedAndroidTest` passes on API 26+ emulator (or Robolectric equivalent)
- No domain model imports in `ArticleEntity` — the entity is a pure persistence model

---

## Implementation Notes

**`publishedAt` storage:** Room has no native `Instant` type converter. Store as `Long` (epoch milliseconds via `Instant.toEpochMilli()`). Add a `TypeConverter` class and register it via `@TypeConverters` on `AppDatabase`.

**Schema export:** Enable `room.schemaLocation` in `:core:database`'s `build.gradle.kts`:
```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
```
Commit the exported schema JSON. If the schema changes without a migration, the build fails — this is intentional.

**Migration strategy:** For this project, `fallbackToDestructiveMigration()` is acceptable in development. Add a comment explaining why, and note that production apps would require explicit `Migration` objects.

**`PagingSource` return type in DAO:** Room generates a `PagingSource<Int, ArticleEntity>` automatically when the `@Query` return type is declared as such. Do not implement `PagingSource` manually.

**`LIKE` query and SQL injection:** Room's `@Query` with `:query` parameter uses parameterized queries — there is no SQL injection risk. The `%` wildcards are concatenated in SQL, not in Kotlin.

**Test isolation:** Each test function in `ArticleDaoTest` should call `database.clearAllTables()` in `@Before` to ensure test independence.
