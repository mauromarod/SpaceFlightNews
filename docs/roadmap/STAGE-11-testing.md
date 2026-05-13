# Stage 11 — Testing Suite

**Status:** ✅ Complete
**Depends on:** Stage 10
**Estimated effort:** 0h remaining
**Progress:** 16 / 18 tasks (89%)

---

## Objective

Complete the full testing suite across all layers. By end of this stage, every Use Case has 90%+ unit test coverage, every ViewModel has its state transitions tested with Turbine, integration tests verify the Room+Paging stack, Robot Pattern tests cover critical UI flows, Roborazzi snapshot verification passes, and the android-cli Journey test and Maestro flow run successfully on an emulator.

---

## Tasks

### Use Case Tests (`:core:domain`)
- [x] Create `GetArticlesUseCaseTest.kt` — verify `invoke()` delegates to `ArticleRepository.getArticles()` and returns the same `Flow`
- [x] Create `SearchArticlesUseCaseTest.kt` — verify `invoke(query)` passes the query through to the repository
- [x] Create `GetArticleDetailUseCaseTest.kt` — verify success path and failure path (`Result.failure`) propagation

### ViewModel Tests (`:features:news`, `:features:detail`)
- [x] Create / finalize `NewsViewModelTest.kt` (stubs from Stage 08):
  - `initialState_isLoading`
  - `searchQuery_debounce_suppressesRapidChanges`
  - `articleTapped_emitsNavigationEffect`
  - `retryClicked_refreshesArticles`
  - `staleData_emitsSnackbarEffect`
  - `searchQuery_restoredFromSavedStateHandle`
- [x] Create / finalize `DetailViewModelTest.kt` (stubs from Stage 09):
  - `init_success_emitsContentState`
  - `init_notFound_emitsNotFoundError`
  - `init_networkError_emitsGenericError`
  - `openUrlClicked_emitsOpenUrlEffect`
  - `retryClicked_reloadsArticle`

### Mapper Tests (`:core:data`)
- [x] Create / finalize `ArticleMapperTest.kt`:
  - `validDto_mapsCorrectly`
  - `nullImageUrl_mapsToNull`
  - `emptyStringImageUrl_mapsToNull`
  - `validIso8601Date_parsedToInstant`

### Integration Tests (`:core:database`)
- [x] Create / finalize `ArticleDaoTest.kt` using `Room.inMemoryDatabaseBuilder`:
  - `insertAll_thenQueryAll_returnsInserted`
  - `insertAll_onConflictReplace_updatesExisting`
  - `clearAll_emptiesTable`
  - `searchQuery_returnsOnlyMatchingArticles`

### Integration Tests (`:core:data` — `ArticleRemoteMediator`)
- [x] Create `ArticleRemoteMediatorTest.kt` using `MockWebServer` + in-memory Room:
  - `refresh_insertsFirstPageAndPersistsRemoteKeys`
  - `refresh_endOfPagination_detectedWhenNextIsNull`
  - `refresh_clearsExistingDataBeforeInserting`
  - `networkError_returnsMediatorError`

### Robot Pattern UI Tests (`:features:news`, `:features:detail`)
- [x] Create `ArticleListRobot.kt` in `androidTest/` — DSL for list screen interactions (searchFor, tapFirstResult, firstResultVisible, emptyStateVisible, errorStateVisible, retryButtonVisible)
- [x] Create `ArticleDetailRobot.kt` in `androidTest/` — DSL for detail screen (titleVisible, heroImageVisible, openUrlButtonVisible, tapOpenUrl, backPressed)
- [x] Create `NewsScreenTest.kt` using robots:
  - `launch_showsArticleList`
  - `search_filtersResults`
  - `emptySearch_showsEmptyState`
  - `errorState_showsRetryButton`
- [x] Create `DetailScreenTest.kt` using robots:
  - `launch_showsArticleContent`
  - `tapOpenUrl_opensExternalBrowser` (verify intent is fired)

### E2E Tests _(deferred to Stage 13 stretch)_
- [ ] Create `journeys/article_search_flow.xml` — android-cli journey (see TESTING_STRATEGY.md for format); run via `android run` on API 35 emulator
- [ ] Create `maestro/happy_path.yaml` — Maestro flow covering search → detail → back → verify list state

---

## Acceptance Criteria

- [x] `./gradlew testDebugUnitTest` passes across all modules
- [x] Use Case coverage ≥ 90% (measured via Jacoco report)
- [x] All ViewModel state transitions are covered by Turbine assertions
- [x] `ArticleMapper` tests cover all three `imageUrl` variants
- [x] `ArticleDaoTest` and `ArticleRemoteMediatorTest` pass with in-memory Room
- [ ] `./gradlew verifyRoborazziDebug` passes (no visual regression from Stage 07 goldens) _(deferred)_
- [x] `NewsScreenTest` and `DetailScreenTest` pass on API 35 emulator
- [ ] Journey XML test completes with all actions `PASSED` _(deferred to Stage 13 stretch)_
- [ ] Maestro happy path flow exits with status `PASSED` _(deferred to Stage 13 stretch)_

---

## Implementation Notes

**`FakeArticleRepository`:** Create a `FakeArticleRepository : ArticleRepository` in a shared `test/` source set in `:core:domain`. It can be configured to return success, failure, or a specific list. Reuse across ViewModel tests in both feature modules.

**Turbine assertion order:** Emissions are sequential. If a ViewModel emits `Loading → Content`, the test must `awaitItem()` twice:
```kotlin
viewModel.uiState.test {
    assertIs<NewsUiState.Loading>(awaitItem())
    assertIs<NewsUiState.Content>(awaitItem())
    cancelAndIgnoreRemainingEvents()
}
```
Forgetting `cancelAndIgnoreRemainingEvents()` causes the test to hang if there are unconsumed emissions.

**`MockWebServer` setup for RemoteMediator:**
```kotlin
private val mockWebServer = MockWebServer()

@Before fun setUp() { mockWebServer.start() }
@After fun tearDown() { mockWebServer.shutdown() }
```
Enqueue responses using `mockWebServer.enqueue(MockResponse().setBody(json))` where `json` is a test fixture string.

**Test fixtures:** Store JSON response fixtures in `src/test/resources/fixtures/articles_page_1.json`. Read via `javaClass.getResourceAsStream(...)` in test setup. This avoids inline JSON strings in test code.

**`@HiltAndroidTest` setup:** For instrumented tests using Hilt, the test `AndroidManifest.xml` in `src/androidTest/` must declare the `HiltTestApplication` as the application class. Add:
```xml
<application android:name="dagger.hilt.android.testing.HiltTestApplication" />
```
