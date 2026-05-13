# Stage 14 — Firebase Integration

**Status:** ✅ Complete
**Depends on:** Stage 13
**Estimated effort:** 0h remaining
**Progress:** 39 / 39 tasks (100%)

---

## Objective

Full Firebase integration following senior/tech-lead architecture principles: all Firebase SDKs are confined to `:app`. Feature modules and `:core:*` modules interact exclusively with domain interfaces defined in `:core:domain`. This keeps Firebase as a plugin, not a dependency of the domain layer.

---

## Architecture

```
:core:domain          (interfaces only — zero Firebase imports)
      │
      ├── AuthRepository
      ├── AnalyticsRepository
      ├── RemoteConfigRepository
      ├── UserPreferencesRepository
      ├── CrashReporter
      └── PerformanceTracer / AppTrace

:app                  (Firebase implementations + Hilt bindings)
      ├── auth/FirebaseAuthRepository
      ├── analytics/FirebaseAnalyticsRepository
      ├── config/FirebaseRemoteConfigRepository
      ├── prefs/DataStoreUserPreferencesRepository
      ├── crash/FirebaseCrashReporter
      ├── perf/FirebasePerformanceTracer
      └── di/FirebaseModule (binds all 6 interfaces)
```

Feature modules (`features/news`, `features/detail`) inject domain interfaces via Hilt — no Firebase SDK imports.

---

## Tasks

### 14.0 — Gradle & Dependencies
- [x] Add `com.google.firebase.crashlytics` plugin to root `build.gradle.kts` classpath
- [x] Apply `com.google.firebase.crashlytics` plugin in `app/build.gradle.kts`
- [x] Add Firebase BOM + Auth, Crashlytics, Remote Config, Performance to `app/build.gradle.kts`
- [x] Add DataStore + AppCompat to `gradle/libs.versions.toml` and `app/build.gradle.kts`
- [x] Add `google-services.json` to `.gitignore`

### 14.1 — Auth + Login Screen
- [x] Define `AuthRepository` interface + `AuthUser` model in `:core:domain`
- [x] Implement `FirebaseAuthRepository` — `callbackFlow` for `currentUser`, sets Crashlytics userId + Analytics property on login
- [x] Bind `AuthRepository → FirebaseAuthRepository` in `FirebaseModule`
- [x] Create `LoginViewModel` — anonymous + email/password sign-in, `LoginUiState` / `LoginUiEffect` sealed interfaces
- [x] Create `LoginScreen` — space-themed dark background, rocket icon, app name + "by mauromarod" branding, email/password fields, "Sign In" + "Continue as Guest" buttons
- [x] Add `Screen.Login` route to navigation
- [x] Auth-gate `AppNavHost` start destination — synchronous `isLoggedIn()` check (Firebase caches auth state)
- [x] Clear login from back stack after successful auth (`popUpTo(Login.route) { inclusive = true }`)

### 14.2 — User Preferences (DataStore per user)
- [x] Define `UserPreferencesRepository` interface + `ThemePreference` / `LanguagePreference` enums in `:core:domain`
- [x] Implement `DataStoreUserPreferencesRepository` — keys `"theme_${uid}"` / `"language_${uid}"` so each Firebase UID gets isolated preferences
- [x] Provide `DataStore<Preferences>` as Hilt singleton via `Context.userPrefsDataStore` extension
- [x] Replace `rememberSaveable` theme state in `MainActivity` — inject `AuthRepository` + `UserPreferencesRepository`, observe via `flatMapLatest` on current user UID
- [x] Language switching via `AppCompatDelegate.setApplicationLocales()` triggered by DataStore flow

### 14.3 — Analytics
- [x] Define `AnalyticsRepository` interface in `:core:domain` with 8 typed event methods
- [x] Implement `FirebaseAnalyticsRepository` — event names follow Firebase snake_case convention (≤40 chars), title truncated to 40 chars for event params
- [x] Wire into `NewsViewModel` — debounced search → `trackSearchPerformed`, `ArticleTapped` → `trackArticleOpened` with source ("search"/"feed"), `trackSearchConverted` when article opened from active search
- [x] Wire into `DetailViewModel` — `OpenUrlClicked` → `trackExternalUrlOpened`
- [x] Wire into `ArticleRemoteMediator` — `APPEND` load type → `trackFeedPageLoaded("append")` (injected as nullable `AnalyticsRepository?` to preserve testability)

### 14.4 — Remote Config + A/B Feature Flags
- [x] Define `RemoteConfigRepository` interface in `:core:domain`
- [x] Implement `FirebaseRemoteConfigRepository` — in-app defaults: `cache_ttl_minutes=5`, `feature_theme_toggle_enabled=true`, `feature_language_selection_enabled=false`; fetch interval 3600s
- [x] Create `RemoteConfigLifecycleObserver` — `DefaultLifecycleObserver.onResume()` calls `fetchAndActivate()` on each foreground
- [x] Expose flags via `StateFlow` in `MainActivity`; provide via `CompositionLocal` so `NewsScreen` can conditionally show theme toggle

### 14.5 — Localization (EN + ES)
- [x] Migrate all hardcoded strings in `features/news` to `R.string.*` (14 strings + Spanish translations)
- [x] Migrate all hardcoded strings in `features/detail` to `R.string.*` (5 strings + Spanish translations)
- [x] Migrate shared strings in `core/ui-components` to `R.string.*` (4 strings + Spanish translations)
- [x] Add `app/src/main/res/values-es/strings.xml` for app name
- [x] Refactor `buildOfflineMessage` to accept `Context` and use `context.getString()`

### 14.6 — Crashlytics
- [x] Define `CrashReporter` interface in `:core:domain` (`recordNonFatal`, `log`, `setUserId`)
- [x] Implement `FirebaseCrashReporter` — `setCustomKey` for each extra in the map
- [x] Wire into `RetryInterceptor` via nullable callback lambda `onMaxRetriesExhausted: ((url, code) -> Unit)?` — keeps `:core:network` Firebase-free; lambda provided from `NetworkModule` in `:app`
- [x] Wire into `DetailViewModel` — `recordNonFatal` for non-404 load failures; `log` breadcrumb on article load start

### 14.7 — Performance Monitoring
- [x] Define `PerformanceTracer` / `AppTrace` interfaces in `:core:domain`
- [x] Implement `FirebasePerformanceTracer` with private `FirebaseAppTrace` inner class wrapping `com.google.firebase.perf.metrics.Trace`
- [x] Wire `PerformanceTracer` into `ArticleRemoteMediator` — custom trace `"articles_network_fetch"` wraps the API call + Room insert; attributes: `load_type`; metric: `articles_count`; injected as nullable `PerformanceTracer?`

---

## Acceptance Criteria

- [x] Zero Firebase SDK imports in `:core:*` and `features/*` modules
- [x] App compiles clean (`./gradlew :app:compileDebugKotlin` — zero errors, zero warnings)
- [x] Login screen appears on first install; anonymous auth routes to news list
- [x] Subsequent launches skip login (Firebase caches auth state)
- [x] Theme preference persists across kills (DataStore per UID)
- [x] Language switches to Spanish via AppCompatDelegate (Activity recreates)
- [x] `google-services.json` not committed (`.gitignore` entry confirmed)
- [x] Analytics events fire (verified via logcat `FirebaseAnalytics` tag in debug builds)
- [x] Non-fatal Crashlytics events logged for retry exhaustion and detail load failures
- [x] Custom `"articles_network_fetch"` trace runs on each mediator load

---

## Implementation Notes

**Firebase BOM 34.13.0 artifact names:** The `-ktx` suffix does not exist for Crashlytics, Remote Config, or Performance in BOM 34.x. Use `firebase-crashlytics`, `firebase-config`, `firebase-perf` (no `-ktx`).

**DataStore singleton requirement:** The `preferencesDataStore()` delegate must be a top-level property on `Context`, not inside a class. Hilt provides the resulting `DataStore<Preferences>` instance via `@Provides @Singleton`.

**`stringResource` inside `semantics {}`:** `contentDescription` inside a `semantics {}` block is not `@Composable`, so `stringResource()` can't be called there. Solution: read the string into a local `val` before the `semantics` block.

**`flatMapLatest` for per-user preferences:** When the Firebase auth user changes (e.g., sign-in, sign-out), `currentUserFlow.filterNotNull().flatMapLatest { getThemePreference(uid) }` automatically switches to the new user's DataStore keys.

**CrashReporter in `:core:network` without Firebase dependency:** `RetryInterceptor` accepts an optional `onMaxRetriesExhausted: ((url: String, code: Int) -> Unit)?` callback. `NetworkModule` in `:app` passes a lambda that calls `crashReporter.recordNonFatal(...)`. This keeps `:core:network` entirely Firebase-free.

**A/B experiment setup (Firebase Console):**
- Parameter `feature_language_selection_enabled`: Group A → `true` (language selector visible), Group B → `false` (control)
- Parameter `feature_theme_toggle_enabled`: can be toggled per-audience

**CI/CD — `google-services.json` as secret:**
```yaml
- name: Decode google-services.json
  run: echo "${{ secrets.GOOGLE_SERVICES_JSON }}" | base64 --decode > app/google-services.json
```

**Firebase Analytics DebugView:**
```
adb shell setprop debug.firebase.analytics.app com.mauromarod.spaceflightnews
```
