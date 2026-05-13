# Stage 07 — UI Components

**Status:** ✅ Complete
**Depends on:** Stage 06
**Estimated effort:** 3–5h
**Progress:** 12 / 12 tasks (100%)

---

## Objective

Build the atomic Compose component library in `:core:ui-components`. Every component uses Slot-based APIs for flexibility, consumes design tokens exclusively from `:core:designsystem`, and has a `@Preview` for each visual state. Roborazzi snapshot tests lock in the visual baseline for all components.

---

## Tasks

### Network Image
- [x] Create `NetworkImage.kt` — Coil `AsyncImage` wrapper with: `contentDescription` parameter, placeholder shimmer (animated `LoadingIndicator` or `Box` with background), error drawable fallback; `contentScale = ContentScale.Crop` for thumbnails

### Article Card
- [x] Create `ArticleCard.kt` — Slot-based API:
  ```kotlin
  fun ArticleCard(
      onClick: () -> Unit,
      modifier: Modifier = Modifier,
      image: @Composable () -> Unit,
      headline: @Composable () -> Unit,
      supporting: @Composable () -> Unit,
      badge: @Composable (() -> Unit)? = null
  )
  ```
  Card uses `MaterialTheme.shapes.medium`, `SpaceFlightNewsSpacing`, and `MaterialTheme.colorScheme.surface`

### Search Bar
- [x] Create `SearchBar.kt` — wraps Material3 `SearchBar` or `OutlinedTextField`; exposes `query`, `onQueryChange`, `onSearch`, `onClearQuery`, `placeholder` parameters; keyboard action `ImeAction.Search`; always uses `SpaceFlightNewsTheme` styling

### Full-Screen State Components
- [x] Create `LoadingState.kt` — full-screen skeleton using `Modifier.shimmer()` (or a simple animated placeholder); matches the `ArticleCard` layout shape so the transition to content is visually smooth
- [x] Create `EmptyState.kt` — centered illustration + title + subtitle; accepts `title: String`, `subtitle: String`, `action: (@Composable () -> Unit)?` (optional CTA button slot)
- [x] Create `ErrorState.kt` — centered illustration + message + retry button; accepts `message: String`, `onRetry: () -> Unit`; distinguishes visually between network error and not-found

### Previews
- [x] Add `@Preview` for each component covering: light theme, dark theme, and all meaningful states (loading, content, error, empty)

### Snapshot Tests (Roborazzi)
- [x] Set up Roborazzi in `:core:ui-components` `build.gradle.kts` (add `testImplementation` + `testImplementation(roborazzi-compose)`)
- [x] Create `ArticleCardSnapshotTest.kt` — captures `ArticleCard` in content/light, content/dark states
- [x] Create `StateComponentsSnapshotTest.kt` — captures `LoadingState`, `EmptyState`, `ErrorState` in light and dark
- [x] Run `./gradlew :core:ui-components:recordRoborazziDebug` to generate initial golden images; commit them to `snapshots/roborazzi/`

---

## Acceptance Criteria

- All components compile with zero direct references to `:core:data`, `:core:domain`, or `:core:network`
- `ArticleCard` slot API accepts any `@Composable` content for image, headline, and supporting slots
- All `@Preview` annotations are visible and render correctly in Android Studio
- `./gradlew :core:ui-components:verifyRoborazziDebug` passes against the committed golden images
- No `hardcoded` strings in components — all user-visible text comes from parameters or `stringResource()`

---

## Implementation Notes

**Slot-based API rationale:** `ArticleCard` receives `@Composable` lambdas for its image and text slots instead of a domain `Article` object. This keeps `:core:ui-components` independent of `:core:domain`, making the component reusable in any context without coupling to the data model.

**Shimmer placeholder:** Use a simple animated `alpha` between 0.3f and 0.7f via `infiniteTransition.animateFloat()` applied as `Modifier.background()`. Avoid external shimmer libraries to keep the dependency count low.

**`LoadingState` skeleton shape:** Mirror the `ArticleCard` layout: a rectangle for the image on the left, two lines (wide + narrow) for title and subtitle on the right. This creates a smooth visual transition when real content loads.

**Roborazzi test runner:** Tests must run with `@RunWith(RobolectricTestRunner::class)`. Configure Robolectric in `src/test/resources/robolectric.properties`:
```
sdk=35
```
This ensures rendering matches the target API level.

**`testTag` convention:** Every interactive or verifiable element in a component must have a `Modifier.testTag(...)`. Define tags as constants in a companion `object Tags` inside each component file. This prevents string typos in test code.

**Dark mode in snapshots:** Pass `darkTheme = true` to `SpaceFlightNewsTheme` in dark mode test cases. Roborazzi generates separate golden files per test function — dark and light tests must have distinct function names.
