# Stage 10 — Navigation & App Wiring

**Status:** ✅ Complete
**Depends on:** Stage 08, Stage 09
**Estimated effort:** 2–3h
**Progress:** 9 / 9 tasks (100%)

---

## Objective

Wire all feature screens into a single navigation graph, finalize `MainActivity`, and ensure `SpaceFlightNewsApplication` is correctly configured. This stage produces a working, end-to-end runnable app for the first time. All Hilt modules from previous stages must resolve correctly.

---

## Tasks

### Navigation
- [x] Create `Screen.kt` — sealed class with route definitions:
  ```kotlin
  sealed class Screen(val route: String) {
      data object NewsList : Screen("news_list")
      data class ArticleDetail(val articleId: Int) : Screen("article_detail/{$ARG_ARTICLE_ID}") {
          companion object {
              const val ARG_ARTICLE_ID = "articleId"
              fun createRoute(id: Int) = "article_detail/$id"
          }
      }
  }
  ```
- [x] Create `AppNavHost.kt` — `NavHost` composable with two destinations:
  - `composable(Screen.NewsList.route)` → renders `NewsScreen` with `onNavigateToDetail` lambda that calls `navController.navigate(Screen.ArticleDetail.createRoute(it))`
  - `composable(Screen.ArticleDetail(...).route, arguments = listOf(navArgument(ARG_ARTICLE_ID) { type = NavType.IntType }))` → renders `DetailScreen` with `onBack = { navController.popBackStack() }` and `onOpenUrl` lambda

### MainActivity
- [x] Refactor `MainActivity.kt`:
  - Annotate with `@AndroidEntryPoint`
  - Call `SpaceFlightNewsTheme` from `:core:designsystem` (remove old theme import)
  - Set content to `AppNavHost()`
  - Ensure `WindowCompat.setDecorFitsSystemWindows(window, false)` for edge-to-edge display

### Application Class
- [x] Verify `SpaceFlightNewsApplication.kt` has `@HiltAndroidApp` and is declared in `AndroidManifest.xml` via `android:name`

### Configuration Changes
- [x] Add `android:configChanges="orientation|screenSize|screenLayout"` to `<activity>` in `AndroidManifest.xml` to ensure the ViewModel handles rotation without Activity recreation side effects in specific edge cases. Note: with ViewModels this is technically not required, but declaring it is a defensive practice for complex Compose navigation stacks.
- [x] Verify `android:windowSoftInputMode="adjustResize"` on `<activity>` so the `SearchBar` is not obscured by the keyboard

### Smoke Test
- [x] Build and install on emulator or device: `./gradlew :app:installDebug`
- [x] Manually verify: app launches → list loads → search filters → tap article → detail loads → back returns to list
- [x] Manually verify: rotate on list with search query → query preserved; rotate on detail → content preserved
- [x] Verify `./gradlew assembleDebug` completes with zero warnings about unresolved Hilt bindings

---

## Acceptance Criteria

- The app launches without crashing from `adb shell am start`
- Both screens are reachable via navigation
- The back stack is correctly managed: back from detail returns to list with prior state intact
- No `Unresolved reference` or Hilt `MissingBinding` errors at compile time
- Rotation works on both screens without data loss

---

## Implementation Notes

**`@AndroidEntryPoint` on `MainActivity`:** Required for Hilt to inject into the Activity and to enable `hiltViewModel()` in composables hosted by this Activity's navigation graph.

**Deep link consideration:** The `ArticleDetail` route uses an `Int` argument. Ensure `NavType.IntType` is explicitly declared in `navArgument` — without this, Navigation Compose defaults to String and crashes on `toInt()` conversion.

**Edge-to-edge:** `WindowCompat.setDecorFitsSystemWindows(window, false)` combined with `Modifier.systemBarsPadding()` in the top-level composable ensures content renders under the status bar correctly. Apply `systemBarsPadding()` in `AppNavHost` or in `MainActivity`'s content slot.

**`popBackStack()` return value:** `navController.popBackStack()` returns `false` if the back stack is empty (the user is on the start destination). Do not call it blindly — the `onBack` lambda in `DetailScreen` should be wired to `onBackPressedDispatcher` or rely on the system's default behavior, which Compose Navigation handles automatically.

**Hilt binding verification order:** If `assembleDebug` fails with `MissingBinding`, check this order:
1. Is the providing module annotated with `@InstallIn(SingletonComponent::class)` or the appropriate component?
2. Is the module included in `:app`'s `build.gradle.kts` dependencies (directly or transitively)?
3. Is `@HiltAndroidApp` on the Application class?
4. Is `@AndroidEntryPoint` on the Activity?
