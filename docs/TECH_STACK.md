# Tech Stack

All versions should be verified against [Maven Central](https://search.maven.org) before adding to `gradle/libs.versions.toml`. Versions marked with `*` are already declared in the version catalog.

---

## Core Language & Runtime

| Library | Version | Module | Rationale |
|---|---|---|---|
| Kotlin | `2.2.10`* | all | Language of the project. Null safety, sealed classes, coroutines, and value classes are used throughout. |
| KSP (Kotlin Symbol Processing) | `2.2.10-1.0.29` | all | Replaces kapt for annotation processing. Significantly faster incremental compilation. Required by Hilt and Room. |
| Coroutines Core | `1.9.0` | all | Structured concurrency. `Flow`, `StateFlow`, `Channel`, and `suspend` functions used across all layers. |
| Coroutines Android | `1.9.0` | `:app`, `:features:*` | Provides `Dispatchers.Main` for the Android main thread. |

---

## UI

| Library | Version | Module | Rationale |
|---|---|---|---|
| Compose BOM | `2026.02.01`* | `:features:*`, `:core:ui-components`, `:core:designsystem` | Bill of Materials ensures version consistency across all Compose artifacts. |
| Compose UI | BOM-managed* | same | Core Compose runtime and layout engine. |
| Compose Material3 | BOM-managed* | same | Design system implementation. Provides `MaterialTheme`, adaptive color schemes, and component defaults that our design tokens override. |
| Compose UI Tooling Preview | BOM-managed* | same (debug) | `@Preview` support in Android Studio. No impact on release binary. |
| Coil Compose | `3.1.0` | `:core:ui-components` | Coroutine-native image loading with a Compose-first API (`AsyncImage`). Supports subsampling for thumbnails, dual cache (memory + disk), and placeholder/error slots. Chosen over Glide because Glide requires a `RequestManager` tied to the `Activity`/`Fragment` lifecycle — Coil works natively with Compose's coroutine scope. |

---

## Architecture & Navigation

| Library | Version | Module | Rationale |
|---|---|---|---|
| Hilt Android | `2.56` | `:app`, `:features:*`, `:core:*` | Official Android DI framework built on Dagger 2. Provides `@HiltViewModel`, `@HiltAndroidApp`, and `@InstallIn` scoping. Chosen over Koin because compile-time validation catches misconfigured bindings before runtime. |
| Hilt Navigation Compose | `1.2.0` | `:features:*` | `hiltViewModel()` composable factory that resolves Hilt-injected ViewModels within a Compose `NavBackStackEntry` scope. |
| Navigation Compose | `2.8.9` | `:app` | Jetpack Compose navigation graph with typed routes. `NavHost` lives in `:app` as the composition root. |
| Lifecycle ViewModel KTX | `2.6.1`* | `:features:*` | `viewModelScope`, `SavedStateHandle`, and `collectAsStateWithLifecycle()`. |
| Lifecycle Runtime KTX | `2.6.1`* | `:features:*` | `repeatOnLifecycle` for safe `UiEffect` collection in Composables. |
| Activity Compose | `1.13.0`* | `:app` | `ComponentActivity` with Compose support. |

---

## Networking

| Library | Version | Module | Rationale |
|---|---|---|---|
| Retrofit | `2.11.0` | `:core:network` | Type-safe HTTP client. Suspend function support via its Kotlin adapter. |
| OkHttp | `4.12.0` | `:core:network` | HTTP engine under Retrofit. Provides `Interceptor` chain for logging and retry logic. |
| OkHttp Logging Interceptor | `4.12.0` | `:core:network` (debug) | Logs request/response bodies in debug builds only. Excluded from release via `debugImplementation`. |
| Moshi | `1.15.2` | `:core:network` | JSON serializer. Chosen over Gson because Moshi is Kotlin-native (no reflection for data classes via `moshi-kotlin-codegen`), supports non-null safety, and has a smaller method count. |
| Moshi Kotlin Codegen | `1.15.2` | `:core:network` | KSP-based code generation for `@JsonClass(generateAdapter = true)` — eliminates reflection at runtime. |

---

## Persistence

| Library | Version | Module | Rationale |
|---|---|---|---|
| Room Runtime | `2.7.1` | `:core:database` | Jetpack SQLite ORM. Provides compile-time SQL validation, `Flow`-returning queries, and direct `PagingSource` support. |
| Room KTX | `2.7.1` | `:core:database` | Coroutine extensions for Room transactions (`withTransaction`). |
| Room Compiler | `2.7.1` | `:core:database` | KSP annotation processor for DAO and entity code generation. |

---

## Pagination

| Library | Version | Module | Rationale |
|---|---|---|---|
| Paging 3 Runtime | `3.3.6` | `:core:data`, `:core:database` | Handles pagination state, load triggers, retry logic, and `RemoteMediator` orchestration. |
| Paging Compose | `3.3.6` | `:features:news` | `collectAsLazyPagingItems()` and `LazyPagingItems` for Compose integration. Provides load state accessors (`loadState.refresh`, `loadState.append`) for loading/error UI. |

---

## Quality & Static Analysis

| Library | Version | Module | Rationale |
|---|---|---|---|
| Ktlint (Gradle Plugin) | `12.2.0` | root | Enforces consistent Kotlin code style across all modules. Applied as a Git pre-commit check and CI step. Zero configuration drift — the style is defined once. |
| Detekt | `1.23.8` | root | Static analysis for code smells, complexity, and potential bugs. Custom rules can enforce project-specific conventions (e.g., no `Thread.sleep` in production code). |
| ArchUnit | `1.3.0` | `:core:domain` (test) | Architectural rule enforcement as unit tests. Primary rule: no class in `:core:domain` imports `android.*`. Prevents silent erosion of the pure Kotlin domain boundary. |

---

## Testing

| Library | Version | Module | Rationale |
|---|---|---|---|
| JUnit 4 | `4.13.2`* | all (test) | Standard Android test runner compatible with `@HiltAndroidTest` and Compose UI Test. |
| Mockk | `1.13.17` | `:features:*`, `:core:data` (test) | Kotlin-first mocking framework. Handles Kotlin-specific constructs (object mocking, coroutine `coEvery`, top-level function mocking). |
| Turbine | `1.2.0` | `:features:*` (test) | Flow testing library. Provides `Flow.test { }` DSL to assert emissions, timeouts, and completion without manual coroutine orchestration. Used to test all ViewModel `StateFlow` and `Channel<UiEffect>` emissions. |
| Coroutines Test | `1.9.0` | all (test) | `StandardTestDispatcher`, `TestScope`, and `runTest` for deterministic coroutine execution in tests. |
| Compose UI Test JUnit4 | BOM-managed* | `:features:*` (androidTest) | Compose semantic tree querying (`onNodeWithText`, `onNodeWithTag`) and interaction APIs for Robot Pattern tests. |
| Espresso Core | `3.7.0`* | `:features:*` (androidTest) | Interoperability with Compose UI Test for cases requiring `ActivityScenario`. |
| Roborazzi | `1.44.0` | `:core:ui-components` (test) | Screenshot/snapshot testing running on Robolectric (no emulator needed for component tests). Captures pixel-accurate golden images per component state. Chosen over Paparazzi because Roborazzi integrates with Compose UI Test semantics and the accessibility tree — Paparazzi diverges from real device rendering for complex Compose layouts. |
| Hilt Android Testing | `2.56` | all (androidTest) | `@HiltAndroidTest`, `@UninstallModules`, and `HiltAndroidRule` for replacing production modules with test doubles in instrumented tests. |

---

## Performance

| Library | Version | Module | Rationale |
|---|---|---|---|
| Baseline Profiles (Gradle Plugin) | `1.3.4` | `:app` | Pre-compiles critical code paths (navigation, list scroll, image loading) ahead of first user interaction. Reduces startup jank on first install. Generated by running a Macrobenchmark flow and embedded in the release AAB. |
| R8 (built into AGP) | AGP-managed | `:app` | Enabled for release builds. Shrinks, obfuscates, and optimizes the bytecode. Custom ProGuard rules in `proguard-rules.pro` preserve Retrofit interfaces, Moshi adapters, and Room entities from aggressive shrinking. |

---

## Firebase (Stretch Goal)

| Library | Version | Module | Rationale |
|---|---|---|---|
| Firebase BOM | `33.14.0` | `:app` | Version alignment for all Firebase artifacts. |
| Firebase Crashlytics KTX | BOM-managed | `:app` | Automatic crash and ANR reporting. Provides structured exception grouping and stack trace deobfuscation via the Crashlytics Gradle plugin. |
| Firebase Remote Config KTX | BOM-managed | `:app` | Feature flags without app releases. Used for `cache_ttl_minutes` and potential future rollout controls. |
| Firebase Analytics KTX | BOM-managed | `:app` | User behavior and funnel analytics for business-level events (search performed, article viewed, detail opened). |
| Firebase Performance KTX | BOM-managed | `:app` | Automatic HTTP trace monitoring and custom `Trace` spans for critical paths (search response time, image load duration). |

---

## Build & CI

| Tool | Version | Rationale |
|---|---|---|
| Android Gradle Plugin (AGP) | `9.2.1`* | Build system for all Android modules. Kotlin DSL (`build.gradle.kts`) throughout for type safety and IDE autocompletion. |
| Gradle | `9.4.1` | Gradle wrapper version. Configuration cache enabled for faster incremental builds. |
| `libs.versions.toml` | — | Version catalog centralizes all dependency versions. Eliminates version duplication across module build files and enables `./gradlew dependencyUpdates` checks. |
| GitHub Actions | — | CI/CD. Jobs: `ktlint-check`, `detekt`, `unit-tests`, `roborazzi-snapshots` (on main). See `.github/workflows/ci.yml`. |

---

## Dependency Decision Log

| Decision | Chosen | Rejected | Reason |
|---|---|---|---|
| Image loading | Coil 3 | Glide | Compose-first coroutine-native API; no lifecycle coupling needed |
| Serialization | Moshi + Codegen | Gson | Kotlin null safety; KSP codegen avoids reflection; smaller binary |
| DI | Hilt | Koin | Compile-time binding validation; IDE navigation support; official Android recommendation |
| Snapshot testing | Roborazzi | Paparazzi | Compose UI Test integration; accessibility tree capture; device-accurate rendering via Robolectric |
| Annotation processing | KSP | kapt | Incremental compilation; faster builds; no Java stubs generation |
| Flow testing | Turbine | Manual `collect` + `CompletableDeferred` | Purpose-built DSL; built-in timeout handling; dramatically less boilerplate |
