# Database Schema

## Overview

The app uses **Room 2.x** on top of SQLite. The database is named `spaceflight_news.db` and is currently at **version 2**.

There are **3 tables**:

| Table | Type | Purpose |
|---|---|---|
| `articles` | Regular | Single Source of Truth for article data |
| `remote_keys` | Regular | Paging 3 offset tracking per article |
| `articles_fts` | Virtual (FTS4) | Full-text search index over `articles` |

---

## Table: `articles`

Defined by `ArticleEntity`. This is the main data table — all UI reads come from here via `ArticleDao`.

| Column | SQLite type | Nullable | Notes |
|---|---|---|---|
| `id` | INTEGER | No | Primary key — matches the API article ID |
| `title` | TEXT | No | Article headline |
| `summary` | TEXT | No | Article body excerpt |
| `imageUrl` | TEXT | Yes | Hero image URL from the news source; may be null or invalid |
| `newsSite` | TEXT | No | Publisher name (e.g. "NASA", "SpaceFlightNow") |
| `publishedAt` | INTEGER | No | Publication timestamp stored as epoch milliseconds |
| `url` | TEXT | No | Full URL to the original article |
| `featured` | INTEGER | No | Boolean stored as 0/1 |

**Insert strategy:** `OnConflictStrategy.REPLACE` — a fresh API page replaces stale rows for the same ID.

**Ordering:** All paginated queries sort by `publishedAt DESC`.

---

## Table: `remote_keys`

Defined by `RemoteKeysEntity`. Paging 3's `RemoteMediator` uses this table to track the pagination offset for each article row, enabling correct APPEND loads across app restarts.

| Column | SQLite type | Nullable | Notes |
|---|---|---|---|
| `articleId` | INTEGER | No | Primary key — foreign key reference to `articles.id` (not enforced by Room, but logically coupled) |
| `prevKey` | INTEGER | Yes | API offset for the previous page; null for the first page |
| `nextKey` | INTEGER | Yes | API offset for the next page; null when end of pagination is reached |
| `lastFetchedAt` | INTEGER | Yes | Epoch millis of the last successful REFRESH; used to display the offline banner timestamp |

**Written by:** `ArticleRemoteMediator` on each successful REFRESH or APPEND.  
**Read by:** `ArticleRepositoryImpl.getLastSyncedAt()` → surfaced in `NewsViewModel._lastSyncedAt` → shown in the offline Snackbar.

---

## Table: `articles_fts` (Virtual — FTS4)

Defined by `ArticleFts` with `@Fts4(contentEntity = ArticleEntity::class)`.

This is a **virtual table** — it has no physical rows of its own. Instead, it is a full-text search index backed by the `articles` table. SQLite automatically keeps it in sync via triggers whenever `articles` is modified.

| Indexed column | Source column in `articles` |
|---|---|
| `title` | `articles.title` |
| `summary` | `articles.summary` |

### How FTS4 works

FTS4 tokenizes text into individual words (tokens) and builds an inverted index: for each token, it stores the list of rows that contain it. Queries use the `MATCH` operator instead of `LIKE`.

```sql
-- FTS MATCH query used in ArticleDao.searchPagingSource()
SELECT articles.* FROM articles
INNER JOIN articles_fts ON articles.rowid = articles_fts.rowid
WHERE articles_fts MATCH :query
ORDER BY articles.publishedAt DESC
```

The `INNER JOIN` on `rowid` links each FTS index entry back to the corresponding row in `articles`.

### Prefix matching

FTS4 supports the `*` wildcard at the end of a token to match all words that start with that prefix:

```
"arg"    → matches only rows containing the exact token "arg"
"arg*"   → matches rows containing "arg", "argentina", "argument", etc.
"space* x*" → matches rows with any token starting with "space" AND any token starting with "x"
```

**Applied in code:** `ArticleRepositoryImpl.buildFtsQuery()` (extracted to `internal companion object` for testability) transforms the user query before passing it to the DAO:

```kotlin
internal companion object {
    fun buildFtsQuery(query: String): String =
        query.trim().split("\\s+".toRegex())
            .filter { it.isNotEmpty() }
            .joinToString(" ") { "$it*" }
            .ifEmpty { query }
}
```

The original (untransformed) query is sent to the network API via `SearchRemoteMediator` so the remote search remains exact.

### FTS vs LIKE

| | `LIKE '%arg%'` | `MATCH 'arg*'` |
|---|---|---|
| Case-sensitive | Yes (for non-ASCII) | No (FTS tokenizer is case-insensitive) |
| Prefix matching | Only with `arg%` form | Yes with `*` |
| Multi-word | Must appear consecutively | Tokens matched independently |
| Performance | Full table scan | Index lookup — O(log n) |

---

## Entity-Relationship Diagram

```
┌─────────────────────────────┐
│          articles           │
├─────────────────────────────┤
│ PK  id           INTEGER    │◄──┐
│     title        TEXT       │   │ (logical FK, not enforced)
│     summary      TEXT       │   │
│     imageUrl     TEXT?      │   │
│     newsSite     TEXT       │   │
│     publishedAt  INTEGER    │   │
│     url          TEXT       │   │
│     featured     INTEGER    │   │
└─────────────────────────────┘   │
                                  │
┌─────────────────────────────┐   │
│         remote_keys         │   │
├─────────────────────────────┤   │
│ PK  articleId    INTEGER    │───┘
│     prevKey      INTEGER?   │
│     nextKey      INTEGER?   │
│     lastFetchedAt INTEGER?  │
└─────────────────────────────┘

┌─────────────────────────────┐
│       articles_fts          │  ← virtual (FTS4)
├─────────────────────────────┤
│     title        (indexed)  │  auto-synced via SQLite triggers
│     summary      (indexed)  │  backed by articles table
└─────────────────────────────┘
```

---

## Migration History

| Version | Change | Strategy |
|---|---|---|
| 1 → 2 | Added `articles_fts` virtual table | Explicit `Migration(1, 2)` — only creates the FTS table, preserves existing article data |

Migration SQL executed:
```sql
CREATE VIRTUAL TABLE IF NOT EXISTS `articles_fts`
USING fts4(content=`articles`, `title`, `summary`)
```

The `content=articles` directive tells FTS4 to use `articles` as the content table, which avoids duplicating the text data on disk. SQLite creates insert/update/delete triggers on `articles` to keep the FTS index current.
