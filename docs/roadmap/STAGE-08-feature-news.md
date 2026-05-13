# Stage 08 — Feature: News List Screen

**Status:** ✅ Complete
**Depends on:** Stage 02, Stage 05, Stage 07
**Estimated effort:** 4–6h
**Progress:** 13 / 13 tasks (100%)

---

## Objective

Implement the news list feature: a `SearchBar` at the top with a `LazyColumn` of article cards below. The list shows the latest articles on launch and filters them as the user types (with 300ms debounce). All four UI states (loading, content, empty, error) are handled. Rotation preserves the search query via `SavedStateHandle`.

---

## Tasks

### MVI Types
- [x] Create `NewsUiState.kt` — `sealed interface NewsUiState { Loading, Content(articles: LazyPagingItems<Article>), Error(message: String) }`. Note: `LazyPagingItems` cannot be held in a `data class` for snapshot testing — expose `Flow<PagingData<Article>>` from the ViewModel and call `collectAsLazyPagingItems()` in the Composable.
- [x] Create `NewsUiEvent.kt` — `sealed interface NewsUiEvent { SearchQueryChanged(query: String), ArticleTapped(articleId: Int), RetryClicked }`
- [x] Create `NewsUiEffect.kt` — `sealed interface NewsUiEffect { NavigateToDetail(articleId: Int), ShowSnackbar(message: String) }`

### ViewModel
- [x] Create `NewsViewModel.kt`:
  - Restore search query from `SavedStateHandle` on init
  - Expose `searchQuery: StateFlow<String>` for the `SearchBar`
  - Apply `debounce(300L)` to the search query `Flow` before passing to `SearchArticlesUseCase`
  - Expose `articles: Flow<PagingData<Article>>` (cached via `.cachedIn(viewModelScope)`)
  - Expose `uiEffect: Flow<NewsUiEffect>` via `Channel(BUFFERED).receiveAsFlow()`
  - Handle `NewsUiEvent.ArticleTapped` → emit `NewsUiEffect.NavigateToDetail`
  - Handle `NewsUiEvent.RetryClicked` → call `articles.refresh()`
  - Detect stale data from `ArticleRepository.isDataStale()` → emit `NewsUiEffect.ShowSnackbar`

### Screen Composable
- [x] Create `NewsScreen.kt` — top-level composable receiving `onNavigateToDetail: (Int) -> Unit` lambda (no `NavController` in composables)
- [x] Wire `UiEffect` collection via `LaunchedEffect(uiEffect)` with `repeatOnLifecycle(STARTED)` — ensures snackbar and navigation are not triggered when the app is backgrounded
- [x] Implement `LazyColumn` with `items(articles)` from `LazyPagingItems`:
  - `loadState.refresh is LoadState.Loading` → show `LoadingState` composable
  - `loadState.refresh is LoadState.Error` → show `ErrorState` composable with retry
  - `articles.itemCount == 0 && loadState.refresh is LoadState.NotLoading` → show `EmptyState`
  - Otherwise → render `ArticleCard` per item
  - `loadState.append is LoadState.Loading` → show a `CircularProgressIndicator` at the list bottom
- [x] Implement `SearchBar` wired to `viewModel.searchQuery` and `NewsUiEvent.SearchQueryChanged`

### State Preservation
- [x] Verify search query survives rotation: enter a query, rotate the device, confirm the `SearchBar` still shows the query and the list is filtered accordingly (manual test + automated ViewModel test)

### Unit Tests
- [x] Create `NewsViewModelTest.kt`:
  - `initialState_isLoading` — verify first emission is loading state
  - `searchQuery_debounced` — emit two rapid changes, verify only one upstream call after 300ms
  - `articleTapped_emitsNavigationEffect` — verify `NavigateToDetail` effect is emitted
  - `searchQuery_persistedAcrossRotation` — create ViewModel with pre-populated `SavedStateHandle`, verify query is restored
- [x] Create `NewsScreenTest.kt` (instrumented) using `ArticleListRobot`:
  - `search_filtersResults` — type query, verify article cards appear
  - `errorState_showsRetry` — inject error state, verify `ErrorState` and retry button

---

## Acceptance Criteria

- `NewsScreen` has no direct ViewModel or repository imports — only receives lambdas and state
- `NewsViewModel` has no direct Composable imports
- The 300ms debounce is implemented via `Flow.debounce(300L)` — not a manual `delay()` call
- `.cachedIn(viewModelScope)` is applied to the `PagingData` Flow to survive configuration changes
- All four states (loading, content, empty, error) are reachable and visually distinct
- Rotation: search query is preserved (verified by `savedStateHandle` key `"search_query"`)
- `./gradlew :features:news:testDebugUnitTest` passes

---

## Implementation Notes

**`PagingData` and `cachedIn`:** The `Flow<PagingData<Article>>` returned by the use case must be passed through `.cachedIn(viewModelScope)` in the ViewModel. Without this, rotating the device causes the Pager to restart from page 0.

**`collectAsLazyPagingItems()` placement:** Call this in the Composable, not in the ViewModel. `LazyPagingItems` is a Compose-lifecycle-aware object and cannot be held in a `StateFlow`.

**`loadState` logic ordering:** Check `loadState.refresh` first (for initial/refresh load), then `loadState.append` (for pagination). The empty state check requires both `itemCount == 0` AND `refresh is NotLoading` to avoid showing empty state while the first page is still loading.

**Snackbar for stale data:** Use `SnackbarHostState` in `NewsScreen` and collect `UiEffect.ShowSnackbar` in a `LaunchedEffect`. Do not use `Toast` — it is not lifecycle-aware and creates memory leaks if the Activity is destroyed mid-display.

**`repeatOnLifecycle(STARTED)` for effects:** Collecting the `UiEffect` flow inside `LaunchedEffect(Unit)` is insufficient if the app can be backgrounded. Use:
```kotlin
LaunchedEffect(Unit) {
    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiEffect.collect { effect -> /* handle */ }
    }
}
```

**`SavedStateHandle` key:** Use a named constant for the key to avoid typos:
```kotlin
companion object {
    private const val KEY_SEARCH_QUERY = "search_query"
}
```
