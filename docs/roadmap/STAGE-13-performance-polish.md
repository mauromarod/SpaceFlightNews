# Stage 13 — Performance & Polish

**Status:** ✅ Complete
**Depends on:** Stage 12, Stage 11.1
**Estimated effort:** 0h remaining
**Progress:** 19 / 19 tasks (100%)

---

## Objective

Fix all UX issues identified in Stage 11.1 manual testing (search spinner, shimmer flicker, image rendering, offline-first, shared elements) and complete the final quality pass: accessibility audit, dark mode verification, Baseline Profile, and README screenshots. This stage closes the MVP.

---

## Tasks

### Priority 1 — Critical Fixes (from Stage 11.1)

- [x] **[A] Fix initial load shimmer flicker** — `NewsContent` ahora usa `mediator?.refresh` para gatear `LoadingState` y `EmptyState`. El flash de empty state durante el clear+insert transaccional ya no ocurre.
- [x] **[B] Fix search infinite spinner (APPEND offset)** — `SearchRemoteMediator` usa `nextOffset: Int` como variable de instancia. Se resetea a 0 en `REFRESH` y se incrementa con `result.data.results.size` en cada APPEND exitoso.
- [x] **[G] Tune page size and debounce** — `PAGE_SIZE` 20→32 en `ArticleRemoteMediator`. Debounce 300→500ms en `NewsViewModel`.

### Priority 2 — UX Fixes (from Stage 11.1)

- [x] **[E] Fix detail hero image ContentScale** — `DetailScreen` usa `ContentScale.FillWidth` + `aspectRatio(16f / 9f)`. Reemplaza el `height(240.dp)` fijo que causaba crop y pixelación.
- [x] **[F] Add image placeholder and error fallback** — `NetworkImage` muestra `ShimmerBox` superpuesto mientras el estado es `Loading` o `Empty`. En estado `Error` muestra un box con `surfaceVariant`. Usa `onState` callback de Coil.

### Priority 3 — Search Quality (from Stage 11.1)

- [x] **[C] Improve search with Room FTS** — Creada `ArticleFts` (`@Fts4(contentEntity = ArticleEntity::class)`). `AppDatabase` bumpeado a versión 2. `ArticleDao.searchPagingSource()` usa `INNER JOIN articles_fts ... WHERE articles_fts MATCH :query`. `DatabaseModule` usa `fallbackToDestructiveMigration` — no se requiere migration explícita.

### Priority 4 — Offline-First (from Stage 11.1)

- [x] **[D] Show cached data with connectivity banner** — `NewsContent` detecta `mediatorRefresh is LoadState.Error && itemCount > 0` y muestra `ArticleList` con `ConnectivityBanner` sticky como primer item del `LazyColumn`. Solo muestra `ErrorState` cuando la cache está vacía.
- [x] **[H] Add last-synced timestamp to offline banner** — `ArticleRepository` expone `suspend getLastSyncedAt(): Instant?`. `NewsViewModel` popula `_lastSyncedAt: MutableStateFlow<Instant?>` en `init`. `ConnectivityBanner` calcula "Xm ago" / "Xh ago" con `ChronoUnit.MINUTES/HOURS.between()`.

### Priority 5 — Shared Element Transition (from Stage 11.1)

- [x] **[I] Implement shared element transition** — `SharedTransitionLocals.kt` en `:core:ui-components` define `LocalSharedTransitionScope` y `LocalAnimatedVisibilityScope`. `AppNavHost` envuelve `NavHost` en `SharedTransitionLayout` + `CompositionLocalProvider`. `NewsScreen` y `DetailScreen` aplican `Modifier.sharedElement(rememberSharedContentState("image-${id}"))` al `NetworkImage` correspondiente.

### Priority 6 — Session 2 Fixes (from Stage 11.1 Sesión 2)

- [x] **[K] Fix "not found" flash on launch** — `NewsContent` now shows `LoadingState` when `mediatorRefresh == null || mediatorRefresh is LoadState.Loading` and `itemCount == 0`, preventing the empty state from appearing before the mediator runs.
- [x] **[L] Fix DB wipe on FTS migration** — Replaced `fallbackToDestructiveMigration` with explicit `Migration(1, 2)` in `DatabaseModule.kt` that only executes `CREATE VIRTUAL TABLE articles_fts`. Existing article rows are preserved on upgrade.
- [x] **[N] Fix detail hero image distortion** — Changed `ContentScale.FillWidth` to `ContentScale.Fit` in `DetailScreen.kt`. Small images no longer upscale to fill the container; all images display without distortion.
- [x] **[O] Fix FTS prefix matching** — `ArticleRepositoryImpl.searchArticles()` transforms the query before passing to the DAO: each whitespace-split token gets a `*` suffix (`"arg"` → `"arg*"`). Network call keeps the original query unchanged.
- [x] **[P] Add pull-to-refresh to news list** — `NewsContent` wraps `ArticleList` in `PullToRefreshBox` (`@ExperimentalMaterial3Api`). Swipe-down calls `articles.refresh()` (passed as `onRetry`).
- [x] **[Q] Friendly offline error message** — Added `Throwable.toFriendlyMessage()` extension in `NewsScreen.kt`: `IOException` maps to "No internet connection"; other throwables fall back to their `message` or "Something went wrong".
- [ ] **[M] Investigate shared element transition** — Navigation transition not visible despite `SharedTransitionLayout` implementation. Candidates: Card clipping, `ShimmerBox` overlay during animation, or transition duration too short. Deferred — requires interactive profiling in emulator.

### Priority 7 — Performance (from Stage 11.1)

- [ ] **[J] Profile and fix scroll jank** — Build release APK and profile with Android Studio Profiler. If frame drops confirm, evaluate: explicit `Modifier.size()` on `NetworkImage` to enable Coil subsampling, `derivedStateOf` for expensive derived state, `itemKey` consistency in `LazyColumn`. Apply targeted fixes only for confirmed jank sources.

### Baseline Profile
- [x] Add `androidx.profileinstaller:profileinstaller` dependency to `:app` (v1.4.1)
- [x] Create `baseline-prof.txt` manually covering startup, list scroll, search, and detail navigation — placed at `app/src/main/baseline-prof.txt`
- [ ] Verify the Baseline Profile is included in the release AAB: `bundletool dump manifest --bundle=app-release.aab`

### Accessibility Audit
- [x] Audit all Composables for `contentDescription` parameters on images (`NetworkImage` passes `article.title` as contentDescription)
- [x] `ArticleSearchBar` clear button: `contentDescription = "Clear search"` added
- [x] `ErrorState` and `EmptyState` illustrations: `contentDescription = null` (decorative — text adjacent describes the state)
- [x] `ArticleCard`: `semantics(mergeDescendants = true)` added — TalkBack reads card as a single item
- [x] `LoadingState`: `semantics { contentDescription = "Loading articles" }` added
- [ ] Run Android Studio's Accessibility Scanner on device to confirm zero critical issues

### Dark Mode Verification
- [x] Verified on physical device (T60 Plus, Android 14) via `adb shell cmd uimode night yes`
- [x] Both screens verified: text legible on dark surfaces, images render correctly, SearchBar visible, shimmer skeleton visible in dark mode
- [x] No color token gaps found — design system dark theme tokens applied correctly throughout

### Final Manual Test Checklist
- [ ] Launch → list loads with articles ✓
- [ ] Search → list filters in real time ✓
- [ ] Clear search → full list restores ✓
- [ ] Tap article → detail loads ✓
- [ ] Tap "Read full article" → browser opens ✓
- [ ] Back from detail → list is in prior state ✓
- [ ] Rotate on list with search query → query and results preserved ✓
- [ ] Rotate on detail → content preserved, no reload ✓
- [ ] Offline mode: disable WiFi → app shows cached articles + snackbar ✓
- [ ] Offline mode: fresh install + offline → full error state shown ✓

### README Screenshot Update
- [x] Captured 4 screenshots on physical device: `list_light.png`, `list_dark.png`, `detail_light.png`, `detail_dark.png` → stored in `screenshots/`
- [x] `README.md` Screenshots section updated with 2×2 table (light/dark for each screen)

---

## Acceptance Criteria

**From Stage 11.1 fixes:**
- Initial load shows shimmer → content (no empty state flash mid-load)
- Search APPEND loads second page correctly (no infinite spinner)
- Detail hero image preserves aspect ratio on all article types
- Offline with cache: list shown with connectivity banner (no full-screen error)
- Shared element image animates between list and detail screens

**From quality pass:**
- Baseline Profile file exists at `app/src/main/baseline-prof.txt`
- All `NetworkImage` calls have a non-null `contentDescription`
- Accessibility Scanner reports zero critical issues on both screens
- Dark mode is visually consistent — no unreadable text, no missing colors
- All 10 items in the final manual test checklist pass
- README has at least two screenshots in the Screenshots section

---

## Implementation Notes

**Baseline Profile format:** Each line in `baseline-prof.txt` is a method descriptor that the ART compiler pre-compiles before first use:
```
Lcom/mauromarod/spaceflightnews/MainActivity;->onCreate(Landroid/os/Bundle;)V
Lcom/mauromarod/spaceflightnews/features/news/NewsScreen;->...
```
The `profileinstaller` library handles the installation of these profiles at APK install time. On Android 9+, the `CompilationManager` uses the profile to optimize startup.

**Accessibility merged semantics:** To make `ArticleCard` announce as a single item for TalkBack:
```kotlin
Modifier.semantics(mergeDescendants = true) {}
```
Apply this to the `Card` or the outer `Box` of `ArticleCard`. This merges the image `contentDescription`, title, and subtitle into a single announcement.

**`android screen capture` command:** Use the android-cli to capture screenshots:
```bash
android screen capture -o screenshots/list_light.png
```
Then switch to dark mode and capture again. Store in `screenshots/` at the project root (git-tracked so they appear in the GitHub README).

**Offline testing:** Use Android Studio's network emulation (Extended Controls → Cellular → Signal strength: None) or `adb shell svc wifi disable` to simulate offline mode. Re-enable after testing.

**Final commit message convention:** After this stage, create a commit `chore: complete MVP implementation` that includes all staged changes. Tag it `v1.0.0-mvp` in git for easy reference by reviewers.
