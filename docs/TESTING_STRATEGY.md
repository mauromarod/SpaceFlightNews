# Testing Strategy

## Philosophy

- Test behaviour, not implementation details
- A test that breaks when renaming a private method is a bad test
- Fakes over Mocks at module boundaries — prefer a `FakeArticleRepository` over `mockk<ArticleRepositoryImpl>()`
- One assertion per test block when possible; name the test to describe the scenario, not the method

---

## Testing Pyramid

```
              ▲
             /E2E\
            /─────\          Maestro flows + android-cli Journey tests
           /  UI   \         Compose UI Test + Robot Pattern
          /─────────\
         /Integration\       Repository + Room + RemoteMediator + Paging
        /─────────────\
       /  Unit Tests   \     Use Cases · ViewModels · Mappers · NetworkResult
      /─────────────────\
```

| Layer | Tool | Count target | Runner |
|---|---|---|---|---|
| Unit | JUnit4 + Mockk + Turbine | 70–80% of test suite | JVM |
| Integration | JUnit4 + Room in-memory | `:core:data`, `:core:database` | JVM |
| UI (component) | Roborazzi snapshots | All states of all design system components | Robolectric (JVM) |
| UI (flow) | Compose UI Test + Robot Pattern | Critical user paths | Emulator / device |
| E2E | Maestro | Happy path + rotation state persistence | Device / Tablet |
| Macrobenchmark | Benchmark library | Startup timing + frame timing | Device |

---

## Coverage Targets

| Layer | Target | Rationale |
|---|---|---|
| Use Cases | 90% | Pure business logic — no UI side effects, easy to test exhaustively |
| ViewModels | 90% | State machine; every `UiState` transition must be verified |
| Mappers | 100% | Pure functions with no branching complexity — no excuse for gaps |
| Repositories / Mediators | 90% | `persist()` and error paths tested directly without framework coupling |
| UI Composables | States, not lines | All loading / empty / error / content states snapshotted |

Coverage is measured per module. See `docs/COVERAGE.md` for current numbers, thresholds, and history.

```bash
# Android modules (JaCoCo via AGP)
./gradlew :core:data:createDebugUnitTestCoverageReport \
          :features:news:createDebugUnitTestCoverageReport \
          :features:detail:createDebugUnitTestCoverageReport

# Pure Kotlin module (Kover)
./gradlew :core:domain:koverHtmlReport
```

Reports land at `<module>/build/reports/coverage/test/debug/index.html`.

---

## Patterns

### Robot Pattern (Compose UI Tests)

Robot classes separate the *what* (test intent) from the *how* (UI interaction). Feature tests read as behavior specifications; interaction details are encapsulated.

**Structure:**

```kotlin
class ArticleListRobot(private val rule: ComposeTestRule) {

    fun perform(block: ArticleListRobot.() -> Unit) = apply(block)
    fun check(block: ArticleListRobot.() -> Unit) = apply(block)

    fun searchFor(query: String) {
        rule.onNodeWithTag("search_bar").performTextInput(query)
    }

    fun tapFirstResult() {
        rule.onAllNodesWithTag("article_card").onFirst().performClick()
    }

    fun firstResultVisible() {
        rule.onAllNodesWithTag("article_card").onFirst().assertIsDisplayed()
    }

    fun emptyStateVisible() {
        rule.onNodeWithTag("empty_state").assertIsDisplayed()
    }

    fun errorStateVisible() {
        rule.onNodeWithTag("error_state").assertIsDisplayed()
    }
}
```

**Usage in a test:**

```kotlin
@Test
fun searchQuery_updatesArticleList() {
    ArticleListRobot(composeTestRule)
        .perform { searchFor("SpaceX") }
        .check { firstResultVisible() }
}
```

All `testTag` values are defined as constants in a companion object per screen — never inline strings in tests.

---

### Flow Testing with Turbine

ViewModels expose `StateFlow<UiState>` tested via Turbine's `test { }` DSL. One-shot navigation effects are driven through state (`UiState.Success`, `isSignedOut`) rather than `Channel<UiEffect>`, eliminating the need for a separate effect test in most ViewModels.

```kotlin
@Test
fun onSearch_emitsContentState() = runTest {
    val viewModel = NewsViewModel(
        getArticlesUseCase = FakeGetArticlesUseCase(articles = testArticles),
        savedStateHandle = SavedStateHandle()
    )

    viewModel.uiState.test {
        assertIs<NewsUiState.Loading>(awaitItem())
        viewModel.onEvent(NewsUiEvent.SearchQueryChanged("SpaceX"))
        assertIs<NewsUiState.Content>(awaitItem())
        cancelAndIgnoreRemainingEvents()
    }
}
```

**Rules:**
- Always use `cancelAndIgnoreRemainingEvents()` at the end of a test to avoid hanging coroutines
- Use `awaitItem()` rather than `expectNoEvents()` when a state change is expected — the latter does not assert anything

---

### Snapshot Testing with Roborazzi

Snapshot tests capture pixel-accurate golden images of Compose components in all states. They run on the JVM via Robolectric — no emulator required.

**Target components:**

| Component | States covered |
|---|---|
| `ArticleCard` | Content (light), Content (dark), Loading skeleton |
| `SearchBar` | Empty, Focused, With text |
| `LoadingState` | Full-screen skeleton |
| `EmptyState` | No results illustration |
| `ErrorState` | Network error, Not found |
| `NetworkImage` | Loading placeholder, Loaded, Error fallback |

**Test structure:**

```kotlin
@RunWith(RobolectricTestRunner::class)
class ArticleCardSnapshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun articleCard_contentState_lightTheme() {
        composeTestRule.setContent {
            SpaceFlightNewsTheme {
                ArticleCard(article = FakeArticle.default)
            }
        }
        composeTestRule.onRoot().captureRoborazzi("article_card_content_light")
    }
}
```

Golden images are stored in `snapshots/roborazzi/`. They are committed to the repository and diff'd by CI on PRs targeting `main`. A PR that changes a golden image requires deliberate approval.

**Update goldens:** `./gradlew recordRoborazziDebug`
**Verify goldens:** `./gradlew verifyRoborazziDebug`

---

### Maestro E2E Tests

Maestro `.yaml` flow files live in `maestro/` at the project root. They run against a deployed APK on a device or emulator.

**Flows:**

| Flow | File | Covers |
|---|---|---|
| Happy path | `maestro/happy_path.yaml` | Login → list → search → open article → back |
| Rotation persistence | `maestro/rotation_article_persistence.yaml` | Portrait → landscape → portrait rotation with article selected, back, empty state |

**Run:**

```bash
# Debug
maestro test maestro/happy_path.yaml
maestro test maestro/rotation_article_persistence.yaml

# Release (edit appId in YAML to com.mauromarod.spaceflightnews)
```

`appId` defaults to `com.mauromarod.spaceflightnews.debug`. To run on release, change `appId` to `com.mauromarod.spaceflightnews`.

Maestro flows are run manually on physical devices — not in CI (no emulator matches real rotation behavior).

---

### Macrobenchmark

Located in `:macrobenchmark` module. Two test classes:

| Class | Tool | What it measures |
|---|---|---|
| `StartupBenchmark` | `MacrobenchmarkRule` | Cold startup time (TTID), scroll frame timing (FPS) |
| `BaselineProfileGenerator` | `BaselineProfileRule` | Generates `baseline-prof.txt` for AOT compilation |

**Run:**

```bash
# Measure startup + scroll on connected device
./gradlew :macrobenchmark:connectedBenchmarkAndroidTest

# Generate baseline profile (requires device)
./gradlew :app:generateReleaseBaselineProfile
```

Baseline profile is generated once per release cycle and committed to `app/src/main/baseline-prof.txt`. Not run in CI — requires physical device.

| Scenario | Approach | Reason |
|---|---|---|
| `ArticleRepository` in ViewModel tests | Mockk (`mockk<ArticleRepository>()`) | Interface is small; Mockk `every`/`coEvery` keeps setup local to each test |
| `ArticleApi` (Retrofit) in mediator/repository tests | Mockk | Retrofit generates final classes at runtime; Mockk handles final class mocking |
| `AppDatabase` in mediator tests | Mockk (relaxed) + `mockkStatic("androidx.room.RoomDatabaseKt")` | Avoids instrumented test setup; `persist()` extracted as `internal fun` and tested directly |
| `ArticleDao` / `RemoteKeysDao` in integration tests | In-memory Room DB (`:core:database` androidTest) | Tests real SQL queries and FTS behaviour without filesystem I/O |

---

## Coroutine Test Infrastructure

```kotlin
class MainDispatcherRule : TestWatcher() {
    val testDispatcher = StandardTestDispatcher()

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

Applied via `@get:Rule val mainDispatcherRule = MainDispatcherRule()` in every ViewModel test class.

---

## Hilt Test Setup

```kotlin
@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
class NewsScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    val articleRepository: ArticleRepository = FakeArticleRepository()

    @Before
    fun setup() { hiltRule.inject() }
}
```

---

## CI Integration

| Job | Trigger | Command |
|---|---|---|
| `ktlint-check` | Every push | `./gradlew ktlintCheck` |
| `detekt` (per module) | Every push | Loops through all modules: `./gradlew ":$mod:detekt"` |
| `lint` | Every push | `./gradlew lintDebug` |
| `unit-tests` | Every push | `./gradlew testDebugUnitTest :core:domain:test` |
| `instrumented-tests` | Every push | `./gradlew :app:connectedDebugAndroidTest` on emulator API 34 |
| `build-debug` | After tests | `./gradlew :app:assembleDebug` |
| `build-release` | Push to main | `./gradlew :app:bundleRelease` (signed, with proguard) |
| `roborazzi-verify` | PR to main | `./gradlew verifyRoborazziDebug` |
| `maestro-e2e` | Manual | `maestro test maestro/happy_path.yaml` on physical device |

See `.github/workflows/ci.yml` for the full pipeline definition.
