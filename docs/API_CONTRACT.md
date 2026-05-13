# API Contract — Space Flight News API

## Base URL

```
https://api.spaceflightnewsapi.net/v4/
```

## Authentication

No authentication is required. The API is public and open.

**Required Android permission:** `android.permission.INTERNET`
This is a normal permission and is granted automatically — no runtime request or rationale flow is needed.

---

## Endpoints

### `GET /articles/`

Returns a paginated list of articles. Used for the article list screen and search.

#### Query Parameters

| Parameter  | Type    | Default          | Description                                      |
|------------|---------|------------------|--------------------------------------------------|
| `limit`    | Int     | `10`             | Number of items per page (max 100)               |
| `offset`   | Int     | `0`              | Pagination offset                                |
| `search`   | String  | —                | Full-text search across `title` and `summary`    |
| `ordering` | String  | `-published_at`  | Sort field. Prefix with `-` for descending order |

#### Response Shape — 200 OK

```json
{
  "count": 14563,
  "next": "https://api.spaceflightnewsapi.net/v4/articles/?limit=20&offset=20",
  "previous": null,
  "results": [ /* List<ArticleDto> */ ]
}
```

---

### `GET /articles/{id}/`

Returns a single article by its identifier. Used for the article detail screen.

#### Path Parameters

| Parameter | Type | Description        |
|-----------|------|--------------------|
| `id`      | Int  | Article identifier |

#### Response Shape — 200 OK

Single `ArticleDto` object (same schema as items in the list response).

---

## `ArticleDto` Schema

| Field          | JSON Key       | Type            | Nullable | Notes                           |
|----------------|----------------|-----------------|----------|---------------------------------|
| id             | `id`           | Int             | No       | Unique identifier               |
| title          | `title`        | String          | No       |                                 |
| url            | `url`          | String          | No       | Original article URL            |
| imageUrl       | `image_url`    | String          | Yes      | May be empty string or null     |
| newsSite       | `news_site`    | String          | No       | Publisher name                  |
| summary        | `summary`      | String          | No       | Short article excerpt           |
| publishedAt    | `published_at` | String          | No       | ISO 8601 format                 |
| updatedAt      | `updated_at`   | String          | No       | ISO 8601 format                 |
| featured       | `featured`     | Boolean         | No       |                                 |
| launches       | `launches`     | List\<LaunchRef\> | No    | May be empty list               |
| events         | `events`       | List\<EventRef\> | No     | May be empty list               |

### Nested Types

```
LaunchRef { id: String, provider: String }
EventRef  { id: Int, provider: String }
```

---

## HTTP Error Handling

| Status | Meaning              | App Behavior                                              |
|--------|----------------------|-----------------------------------------------------------|
| 200    | Success              | Map to domain model, update Room, emit `UiState.Content`  |
| 400    | Bad request          | Log error, emit `UiState.Error` with generic message      |
| 404    | Article not found    | Emit `UiState.Error`, show dedicated not-found state      |
| 429    | Rate limited         | Apply exponential backoff (see below), retry silently     |
| 5xx    | Server error         | Retry with backoff, then emit `UiState.Error` + `UiEffect.ShowSnackbar` |
| No connection | Network error  | Serve cached data from Room + emit `UiEffect.ShowSnackbar("Offline mode")` |

### Exponential Backoff

Implemented via an OkHttp `Interceptor` in `:core:network`. Retries up to 3 times with delays of 1s, 2s, and 4s before propagating the failure as `NetworkResult.NetworkError`.

---

## Domain Model

```kotlin
data class Article(
    val id: Int,
    val title: String,
    val summary: String,
    val imageUrl: String?,
    val newsSite: String,
    val publishedAt: Instant,
    val url: String,
    val featured: Boolean
)
```

Clean types: `Instant` replaces raw ISO 8601 strings; `String?` for nullable image URL (handles both `null` and empty string from API).

---

## Mapping Strategy

```
Network:  ArticleDto        →  (ArticleMapper in :core:data)    →  Article
Database: ArticleEntity     →  (ArticleEntityMapper in :core:database) → Article
```

Rules:
- Mappers live exclusively in the data/database layer — domain model has zero import from `data` or `network`
- `null` or empty `image_url` from the API is normalized to `null` in `Article.imageUrl`
- `published_at` is parsed to `Instant` at mapping time, not lazily at display time

---

## Stale Data Policy

| Condition                      | Behavior                                                        |
|--------------------------------|-----------------------------------------------------------------|
| Cache age < 5 minutes          | Serve Room data, skip network fetch                             |
| Cache age ≥ 5 minutes          | Serve Room data immediately, trigger background network refresh  |
| Cache stale after refresh      | Emit `UiEffect.ShowSnackbar` ("Content may be outdated")        |
| No cache + offline             | Emit `UiState.Error` with offline illustration                  |

Cache TTL of 5 minutes is the default. It can be overridden via Firebase Remote Config (`cache_ttl_minutes` key) without a new release.

---

## Pagination Strategy

| Parameter         | Value                            |
|-------------------|----------------------------------|
| Implementation    | Paging 3 with `RemoteMediator`   |
| Page size         | 32 articles                      |
| Prefetch distance | 15 items before end of visible list |
| Source of truth   | Room `PagingSource` (offline-first) |
| Network trigger   | `RemoteMediator` on `REFRESH` and `APPEND` |

The `RemoteMediator` stores remote keys in a separate `RemoteKeysEntity` table to track `next`/`previous` cursors independently of the article data.
