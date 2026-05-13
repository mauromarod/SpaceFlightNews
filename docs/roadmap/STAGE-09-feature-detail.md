# Stage 09 — Feature: Article Detail Screen

**Status:** ✅ Complete
**Depends on:** Stage 02, Stage 05, Stage 07
**Estimated effort:** 2–3h
**Progress:** 10 / 10 tasks (100%)

---

## Objective

Implement the article detail feature. The screen receives an `articleId` from the navigation back-stack, loads the article (from Room cache first, network fallback), and displays the full content. Includes an action to open the original article URL in the browser. Rotation is supported with state preservation via `SavedStateHandle`.

---

## Tasks

### MVI Types
- [x] Create `DetailUiState.kt` — `sealed interface DetailUiState { Loading, Content(article: Article), Error(message: String, isNotFound: Boolean) }`
- [x] Create `DetailUiEvent.kt` — `sealed interface DetailUiEvent { OpenUrlClicked, RetryClicked, BackClicked }`
- [x] Create `DetailUiEffect.kt` — `sealed interface DetailUiEffect { OpenUrl(url: String), NavigateBack }`

### ViewModel
- [x] Create `DetailViewModel.kt`:
  - Receive `articleId: Int` from `savedStateHandle[ARTICLE_ID_KEY]` in `init`
  - Call `GetArticleDetailUseCase(articleId)` on init; map `Result<Article>` to `DetailUiState`
  - Handle `Result.failure(ArticleNotFoundException)` → `DetailUiState.Error(isNotFound = true)`
  - Handle `Result.failure(other)` → `DetailUiState.Error(isNotFound = false)`
  - Handle `DetailUiEvent.OpenUrlClicked` → emit `DetailUiEffect.OpenUrl(article.url)`
  - Handle `DetailUiEvent.RetryClicked` → re-call the use case and emit new state
  - Handle `DetailUiEvent.BackClicked` → emit `DetailUiEffect.NavigateBack`

### Screen Composable
- [x] Create `DetailScreen.kt`:
  - Accept `onBack: () -> Unit` and `onOpenUrl: (String) -> Unit` lambdas — no `NavController`
  - Show `LoadingState` while `DetailUiState.Loading`
  - Show `ErrorState` with distinct message for not-found vs network error, with retry button when applicable
  - Content layout: hero image (full width, 240dp height), `LazyColumn` with title, news site + date, summary text, "Read full article" button
  - Collect `UiEffect.OpenUrl` → call `onOpenUrl` lambda
  - Collect `UiEffect.NavigateBack` → call `onBack` lambda
- [x] Implement URL opening: use `Intent(Intent.ACTION_VIEW, Uri.parse(url))` via `LocalContext`; wrap in try/catch for `ActivityNotFoundException` (no browser installed edge case) → emit `ShowSnackbar` if no handler found

### State Preservation
- [x] Verify rotation on detail screen: scroll position is preserved by `LazyColumn` default behavior; article content does not reload from network (Room cache hit)

### Unit Tests
- [x] Create `DetailViewModelTest.kt`:
  - `onInit_loadsArticleFromUseCase` — verify `DetailUiState.Content` is emitted on success
  - `onInit_articleNotFound_emitsNotFoundError` — use fake that returns `Result.failure(ArticleNotFoundException)`, verify `isNotFound = true`
  - `onInit_networkError_emitsGenericError` — verify `isNotFound = false`
  - `openUrlClicked_emitsOpenUrlEffect` — verify `DetailUiEffect.OpenUrl` with correct URL
  - `retryClicked_reloadsArticle` — verify use case is called again

---

## Acceptance Criteria

- `DetailScreen` has no ViewModel or repository imports
- `DetailViewModel` reads `articleId` exclusively from `SavedStateHandle`
- Opening a URL does not cause a crash if no browser app is installed (`ActivityNotFoundException` handled)
- The not-found error state is visually different from the network error state
- `./gradlew :features:detail:testDebugUnitTest` passes

---

## Implementation Notes

**Hero image height:** Use `fillMaxWidth()` + `height(240.dp)` for the hero image. Apply `ContentScale.Crop` in `NetworkImage`. If `article.imageUrl` is null, show a themed placeholder illustration instead of a blank space.

**Date formatting:** Format `article.publishedAt` using `DateTimeFormatter` in a `StringExtensions.kt` utility in `:core:common`. Pattern: `"MMMM d, yyyy"` (e.g., "March 15, 2026"). Pass the formatted string to the composable — never format inside a composable directly.

**"Read full article" button:** Use `ButtonDefaults.buttonColors` with the design system's primary color. Place it at the bottom of the scrollable content. It triggers `DetailUiEvent.OpenUrlClicked`.

**`ActivityNotFoundException` handling:** Browsers are standard but theoretically absent in some Android environments. The try/catch is a defensive measure:
```kotlin
try {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
} catch (e: ActivityNotFoundException) {
    // emit ShowSnackbar effect
}
```

**`LazyColumn` for detail:** Wrap the hero image and all text content in a `LazyColumn` to handle long summaries on small screens. Use `item { }` blocks for the image, title, metadata, and body. This ensures proper scrolling behavior without `NestedScrollView` or `verticalScroll` conflicts.
