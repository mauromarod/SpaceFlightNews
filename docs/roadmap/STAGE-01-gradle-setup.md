# Stage 01 — Gradle Multi-Module Setup

**Status:** ✅ Complete
**Depends on:** Stage 00
**Estimated effort:** 3–4h
**Progress:** 14 / 14 tasks (100%)

---

## Objective

Transform the single-module template into the full multi-module project structure. All modules must be declared, their `build.gradle.kts` files configured, and the version catalog updated with all dependencies needed across the entire project. The project must sync and compile cleanly after this stage.

---

## Tasks

### Module Directories & Build Files
- [x] Create module directory: `features/news/` with `build.gradle.kts` (Android Library, Compose enabled)
- [x] Create module directory: `features/detail/` with `build.gradle.kts` (Android Library, Compose enabled)
- [x] Create module directory: `core/domain/` with `build.gradle.kts` (pure Kotlin JVM Library — no Android plugin)
- [x] Create module directory: `core/data/` with `build.gradle.kts` (Android Library)
- [x] Create module directory: `core/network/` with `build.gradle.kts` (Android Library, KSP for Moshi)
- [x] Create module directory: `core/database/` with `build.gradle.kts` (Android Library, KSP for Room)
- [x] Create module directory: `core/designsystem/` with `build.gradle.kts` (Android Library, Compose enabled)
- [x] Create module directory: `core/ui-components/` with `build.gradle.kts` (Android Library, Compose enabled)
- [x] Create module directory: `core/common/` with `build.gradle.kts` (Android Library)

### settings.gradle.kts
- [x] Declare all 9 new modules in `settings.gradle.kts` alongside `:app`

### libs.versions.toml — New Entries
- [x] Add versions and libraries: Hilt 2.59, Hilt-Navigation-Compose, Navigation-Compose
- [x] Add versions and libraries: Retrofit, OkHttp, Moshi, Moshi-Kotlin-Codegen
- [x] Add versions and libraries: Room, Paging 3, Coil 3
- [x] Add versions and libraries: Coroutines 1.10.0, KSP 2.3.6, Turbine, Mockk, Roborazzi

### app/build.gradle.kts
- [x] Apply Hilt plugin (`com.google.dagger.hilt.android`)
- [x] Apply KSP plugin
- [x] Declare `:core:*` and `:features:*` as `implementation` dependencies
- [x] Create `SpaceFlightNewsApplication.kt` with `@HiltAndroidApp` and register in `AndroidManifest.xml`

---

## Acceptance Criteria

- All modules appear in the Gradle project structure (Android Studio sidebar)
- `./gradlew projects` lists all 10 modules (`:app` + 9 new)
- `./gradlew assembleDebug` completes without errors
- Each module's `build.gradle.kts` uses only version catalog references (`libs.*`) — no hardcoded version strings
- `SpaceFlightNewsApplication` is registered in `AndroidManifest.xml` via `android:name`

---

## Implementation Notes

**Module plugin conventions:** Use `com.android.library` for all Android modules. Use `org.jetbrains.kotlin.jvm` (no Android plugin) for `:core:domain` — this enforces pure Kotlin at the Gradle level.

**Compose enablement:** Only modules that contain Composables need `buildFeatures { compose = true }` and the Compose BOM. `:core:domain`, `:core:data`, `:core:network`, `:core:database`, and `:core:common` must NOT enable Compose.

**KSP plugin:** Apply KSP only to modules that need it (`:core:network` for Moshi codegen, `:core:database` for Room). Applying it globally adds unnecessary processing time.

**Namespace convention:**
```
:features:news         → com.mauromarod.spaceflightnews.features.news
:features:detail       → com.mauromarod.spaceflightnews.features.detail
:core:domain           → com.mauromarod.spaceflightnews.core.domain
:core:data             → com.mauromarod.spaceflightnews.core.data
:core:network          → com.mauromarod.spaceflightnews.core.network
:core:database         → com.mauromarod.spaceflightnews.core.database
:core:designsystem     → com.mauromarod.spaceflightnews.core.designsystem
:core:ui-components    → com.mauromarod.spaceflightnews.core.uicomponents
:core:common           → com.mauromarod.spaceflightnews.core.common
```

**Minimum viable source file:** Each module needs at least one `.kt` file for Gradle to recognize it as non-empty. Create a placeholder (e.g., a package-level comment file or an empty `package` declaration file) in modules that have no implementation yet.

**INTERNET permission:** Add `<uses-permission android:name="android.permission.INTERNET" />` to `app/src/main/AndroidManifest.xml` in this stage.
