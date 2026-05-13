# Stage 03 — Core Network Module

**Status:** ✅ Complete
**Depends on:** Stage 01
**Estimated effort:** 2–3h
**Progress:** 9 / 9 tasks (100%)

---

## Objective

Implement the HTTP layer: Retrofit API interface, Moshi DTOs, typed error handling via `NetworkResult<T>`, and OkHttp interceptors for logging and exponential backoff retry. This module exposes data to `:core:data` only — it never surfaces into domain or presentation layers.

---

## Tasks

### Typed Error Handling
- [x] Create `NetworkResult.kt` — sealed class with `Success<T>`, `HttpError(code, message)`, `NetworkError(cause)`, `UnknownError(cause)`
- [x] Create `NetworkResultCall.kt` + `NetworkResultCallAdapter.kt` — Retrofit `CallAdapter` that wraps every response into `NetworkResult<T>` automatically (no try/catch in every suspend function)

### Data Transfer Objects
- [x] Create `ArticleDto.kt` — `@JsonClass(generateAdapter = true)` with all API fields; handle nullable `image_url` with `@Json(name = "image_url")`
- [x] Create `ArticleListResponseDto.kt` — wraps `count`, `next`, `previous`, `results: List<ArticleDto>`

### API Interface
- [x] Create `ArticleApi.kt` — Retrofit interface with:
  - `suspend fun getArticles(limit: Int, offset: Int, ordering: String): NetworkResult<ArticleListResponseDto>`
  - `suspend fun searchArticles(limit: Int, offset: Int, search: String): NetworkResult<ArticleListResponseDto>`
  - `suspend fun getArticleById(id: Int): NetworkResult<ArticleDto>`

### OkHttp Interceptors
- [x] Create `LoggingInterceptor.kt` — wraps `HttpLoggingInterceptor`; `BODY` level in debug, `NONE` in release (via `BuildConfig.DEBUG` check)
- [x] Create `RetryInterceptor.kt` — exponential backoff: max 3 retries on 429 or 5xx; delays of 1s, 2s, 4s; propagates failure after max retries

### Hilt Module
- [x] Create `NetworkModule.kt` — `@Module @InstallIn(SingletonComponent::class)` providing `OkHttpClient`, `Retrofit`, `ArticleApi`; base URL from `BuildConfig.BASE_URL`

---

## Acceptance Criteria

- `ArticleApi` functions return `NetworkResult<T>` — no raw `Response<T>` or `Call<T>` at call sites
- `LoggingInterceptor` logs nothing in a release build (verified by checking `BuildConfig.DEBUG` logic)
- `RetryInterceptor` retries exactly 3 times with correct delays before propagating `NetworkError`
- `./gradlew :core:network:test` passes (unit test for `RetryInterceptor` logic using `MockWebServer`)
- No `try/catch` blocks for `IOException` inside `ArticleApi` implementations — handled by the `CallAdapter`

---

## Implementation Notes

**`NetworkResultCallAdapter`:** Implementing a custom `CallAdapter.Factory` is the correct approach to avoid repetitive try/catch. The adapter intercepts the Retrofit response before it reaches the suspend function and converts it to `NetworkResult<T>`. This keeps `ArticleApi` functions clean.

**Base URL via BuildConfig:** Define `BASE_URL` in `app/build.gradle.kts` under `buildConfigField`:
```kotlin
buildConfigField("String", "BASE_URL", "\"https://api.spaceflightnewsapi.net/v4/\"")
```
This avoids hardcoding the URL in source code and allows overriding per build variant.

**Moshi codegen:** `ArticleDto` must use `@JsonClass(generateAdapter = true)`. Do not use `KotlinJsonAdapterFactory` (reflection-based) — it increases binary size and slows deserialization. The KSP processor generates a `ArticleDtoJsonAdapter` at compile time.

**`image_url` normalization:** `ArticleDto.imageUrl` should be declared as `String?`. The API can return `null`, an empty string, or a valid URL. Normalization (empty string → null) happens in `ArticleMapper` in `:core:data`, not here.

**MockWebServer for tests:** Use `com.squareup.okhttp3:mockwebserver` to simulate API responses in `RetryInterceptor` unit tests. Add it to `testImplementation` only.
