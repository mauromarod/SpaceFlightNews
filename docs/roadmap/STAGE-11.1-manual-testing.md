# Stage 11.1 — Manual Testing & Fix Backlog

**Status:** ✅ Complete (cuarta sesión de testing concluida)
**Depends on:** Stage 11, Stage 12, Stage 13 (iterativo)
**Estimated effort:** 0h (sub-stage — observation only; fixes tracked in Stage 13)
**Progress:** 6 / 6 tasks (100%)

---

## Objective

Conduct structured manual testing on a physical device immediately after Stages 11 and 12 were completed. Capture every observed UX issue as an actionable fix item. This sub-stage produces the definitive fix backlog that feeds Stage 13 (Performance & Polish).

---

## Tasks

- [x] Define manual testing checklist across all user-facing flows
- [x] Execute checklist en dispositivo físico — Sesión 1 (pre Stage 13 fixes)
- [x] Document findings Session 1 y produce prioritized fix backlog
- [x] Apply fixes A, B, C, D, E, F, G, H, I en Stage 13
- [x] Execute checklist en dispositivo físico — Sesión 2 (post Stage 13 fixes)
- [x] Document findings Session 2 y actualizar fix backlog con estados finales

---

## Test Device

- **OS:** Android 14
- **Hardware:** Physical device (not emulator)
- **App variant:** Debug APK, fresh install

---

## Manual Test Checklist Executed

| # | Flow | Scenario | Result |
|---|---|---|---|
| 1 | Initial load | App launch → articles appear | ⚠️ UX issue (see Finding A) |
| 2 | Image loading | Images in list and detail | ⚠️ UX issue (see Findings E, F) |
| 3 | Search — network | Type query on fresh install | ⚠️ Functional issue (see Finding B) |
| 4 | Search — local | Type query after scroll | ✅ Works |
| 5 | Search — clear | Clear query → full list restored | ✅ Works |
| 6 | Search — empty | Query with no matches | ✅ Empty state shown |
| 7 | Article detail | Tap article → detail screen | ✅ Opens correctly |
| 8 | Detail — image | Hero image rendering | ⚠️ UX issue (see Finding E) |
| 9 | Detail — URL | Tap "Read Article" → browser | ✅ Works |
| 10 | Navigation | Back from detail → list | ✅ Works |
| 11 | Navigation transition | Tap → detail animation | ⚠️ UX gap (see Finding I) |
| 12 | Rotation | Portrait ↔ landscape | ✅ State preserved |
| 13 | Rotation — search | Search query survives rotation | ✅ Preserved via SavedStateHandle |
| 14 | Offline — no cache | Airplane mode, fresh install | ⚠️ UX gap (see Finding D) |
| 15 | Offline — with cache | Airplane mode after scroll | ⚠️ UX gap (see Finding D, H) |
| 16 | Pagination | Scroll to end → next page loads | ✅ Works |
| 17 | Search pagination | Scroll search results | ⚠️ Functional issue (see Finding B) |
| 18 | Pull to refresh | Pull down → fresh data | ✅ Works |
| 19 | Scroll performance | Fast scroll through large list | ⚠️ Minor issue (see Finding J) |

---

## Findings

### A — Initial Load UX (Shimmer Flicker) `Priority 1`

**Observed:** On first launch, the UI cycles through multiple states before settling on content:
`empty list` → `shimmer loading` → `"no articles found" empty state` → `shimmer loading again` → `data`

**Root cause:** `ArticleRemoteMediator` triggers `LoadType.REFRESH`, which clears Room before inserting the first page. During the clear+insert gap, the Paging `PagingSource` emits an empty page, briefly showing the empty state. The mediator then inserts data, causing another transition.

**Impact:** High — confusing first impression. Users may think the app is broken.

**Fix location:** `features/news/src/main/java/.../NewsScreen.kt` — gate the empty state behind `mediator.refresh is NotLoading` (not just `itemCount == 0`).

---

### B — Search Infinite Spinner (APPEND) `Priority 1`

**Observed:** After a search query loads the first page of results, scrolling to the bottom shows a loading spinner that never resolves. The second page never loads.

**Root cause:** `SearchRemoteMediator.load(LoadType.APPEND)` computes `offset = state.pages.sumOf { it.data.size }`. However, Room inserts the search results into the shared `articles` table without tagging them as search-specific. The `searchPagingSource(query)` may see more items than what was fetched for the current query, causing the offset calculation to overshoot. Additionally, `endOfPaginationReached` for APPEND may not propagate correctly when the response has `next != null` but the local filtered count is inconsistent.

**Impact:** High — search is non-functional for queries with more than one page of results.

**Fix location:** `core/data/src/main/java/.../mediator/SearchRemoteMediator.kt` — track search-specific offset in a local variable instead of deriving from `state.pages`.

---

### C — Search Quality (LIKE vs Tokenized) `Priority 3`

**Observed:** SQL `LIKE '%query%'` search is case-sensitive on some devices, doesn't support multi-word queries, and doesn't match partial tokens (e.g., "launch" doesn't match "launched").

**Root cause:** `ArticleDao.searchPagingSource()` uses `LIKE '%' || :query || '%'` on `title` and `summary`. SQLite `LIKE` is case-insensitive for ASCII but case-sensitive for non-ASCII characters. Multi-token queries (e.g., "SpaceX Starship") require both terms to appear consecutively.

**Impact:** Medium — search feels imprecise compared to user expectations.

**Fix location:** `core/database/src/main/java/.../dao/ArticleDao.kt` — explore Room FTS (Full-Text Search) with `MATCH` operator for tokenized multi-word search.

---

### D — Offline UX (Error Screen Instead of Cache) `Priority 4`

**Observed:** When the device goes offline after browsing, the app shows a full-screen error state instead of displaying the cached articles. No connectivity banner is shown.

**Root cause:** `ArticleRemoteMediator` returns `MediatorResult.Error` on `NetworkError`. Paging propagates this as `LoadState.Error` for `refresh`, which the `NewsContent` composable maps to `ErrorState`. The cached `PagingSource` data is ignored.

**Impact:** Medium — breaks the offline-first promise of the architecture.

**Fix location:** `features/news/src/main/java/.../NewsScreen.kt` — show cached data with a connectivity banner when `refresh` is `LoadState.Error` and `itemCount > 0`.

---

### E — Detail Image Rendering (ContentScale) `Priority 2`

**Observed:** The hero image in the detail screen appears cropped on some articles and pixelated/stretched on others. The image fills the full-width container without preserving the original aspect ratio gracefully.

**Root cause:** `ContentScale.Crop` is used in `DetailScreen.kt` hero image, which crops rather than letterboxing. For landscape API images displayed in a portrait container, significant content is lost.

**Fix location:** `features/detail/src/main/java/.../DetailScreen.kt` — change `ContentScale` to `ContentScale.FillWidth` (or `Fit`) and set a fixed `aspectRatio` on the image container.

---

### F — Image Placeholder (Blue Background Flash) `Priority 2`

**Observed:** While images load, a solid blue rectangle is shown as the placeholder. When an image fails to load, the same blue rectangle remains with no error indicator.

**Root cause:** `NetworkImage` composable in `:core:ui-components` uses a `Box` with `MaterialTheme.colorScheme.primary` as background placeholder. No `error` fallback is configured in the Coil `AsyncImage` call.

**Fix location:** `core/ui-components/src/main/java/.../NetworkImage.kt` — replace solid color with a shimmer placeholder and add an error fallback icon (e.g., `ImageBroken` from Material Icons).

---

### G — Page Size & Debounce Tuning `Priority 1`

**Observed:** 
- Page size of 20 items causes frequent append loads during normal scrolling (each page is consumed in ~3 swipes).
- Debounce of 300ms triggers search on fast typing, causing multiple network requests per character burst.

**Impact:** Minor UX friction but measurable network overhead.

**Fix location:**
- `core/data/src/main/java/.../mediator/ArticleRemoteMediator.kt` — increase `PAGE_SIZE` from 20 to 32.
- `features/news/src/main/java/.../NewsViewModel.kt` — increase debounce from `300ms` to `500ms`.

---

### H — Offline Banner Timestamp `Priority 4`

**Observed:** When showing cached data offline (post-fix D), there is no indication of how stale the cache is. Users cannot tell if they are seeing data from 5 minutes ago or 3 days ago.

**Root cause:** `RemoteKeysEntity` includes a `lastFetchedAt: Long` field (epoch millis) written by `ArticleRemoteMediator` during each successful REFRESH. This data is available in Room but never surfaced in the UI.

**Fix location:** After fix D is applied — add a `lastSyncedAt: Instant?` field to `NewsUiState` populated from `remoteKeysDao.getRemoteKeyForFirstPage()?.lastFetchedAt`. Display it in the connectivity banner.

---

### I — Navigation Transition (No Shared Element) `Priority 5`

**Observed:** Tapping an article in the list causes an instant screen replace with no animation. The hero image does not animate as a shared element between the list card and the detail hero.

**Root cause:** Navigation between `NewsScreen` and `DetailScreen` uses default `NavHost` transitions (none configured). `SharedTransitionLayout` is not implemented.

**Impact:** Low functional impact; high perceived quality impact.

**Fix location:** `:app` `NavHost` — wrap with `SharedTransitionLayout`; add `Modifier.sharedElement()` to `NetworkImage` in both `ArticleCard` (list) and `DetailScreen` (hero), keyed by article ID.

---

### J — Scroll Performance (Fast Scroll Jank) `Priority 6`

**Observed:** Fast scrolling through a large list (100+ items) produces occasional frame drops (jank) on a mid-range device. Not reproducible on the test device (high-end hardware) but likely an issue on lower-end hardware.

**Root cause:** Likely candidates: image decode on main thread, recomposition scope too wide (entire `LazyColumn` recomposes on each item), or `itemKey` not consistently used. Requires profiling in release build before attributing.

**Impact:** Low on high-end hardware; potentially medium on low-end (< 4 GB RAM) devices.

**Fix location:** Evaluate after release build profiling. Candidate fixes: `rememberAsyncImagePainter` with explicit size hints, `derivedStateOf` to gate recomposition, review `itemKey` consistency.

---

## Prioritized Fix Backlog

| ID | Finding | Priority | Stage | Estimated Effort |
|---|---|---|---|---|
| A | Initial load shimmer flicker | 1 — Critical | 13 | 1h |
| B | Search infinite spinner (APPEND offset bug) | 1 — Critical | 13 | 1.5h |
| G | Page size 20→32, debounce 300→500ms | 1 — Quick win | 13 | 0.5h |
| E | Detail hero ContentScale / aspect ratio | 2 — UX | 13 | 0.5h |
| F | Image placeholder shimmer + error fallback | 2 — UX | 13 | 1h |
| C | Tokenized search (Room FTS) | 3 — UX | 13 | 2h |
| D | Offline: show cache + connectivity banner | 4 — Offline-first | 13 | 2h |
| H | Offline banner last-synced timestamp | 4 — Offline-first | 13 | 1h (after D) |
| I | Shared element transition (list→detail) | 5 — Polish | 13 | 2h |
| J | Scroll jank (profile first, then fix) | 6 — Performance | 13 | 1–3h |

**Total estimated effort:** 12.5–14.5h (all in Stage 13)

---

## Acceptance Criteria

- All 10 findings are documented with root cause, impact, and fix location
- Every finding maps to a concrete Stage 13 task
- No new code changes in this sub-stage (observation only)

---

## Notes — Sesión 1

- Rotation tests passed without any fix — `SavedStateHandle` serialization of `searchQuery` works correctly.
- The `READ ARTICLE` button correctly opens the article URL in the external browser via an implicit Intent.
- Pagination (append) in the main feed (non-search) works correctly — only search APPEND is broken.
- This doc supersedes the informal fix list discussed during Stage 11/12 sessions.

---

## Segunda Sesión de Testing (post Stage 13 fixes A–I)

**App variant:** Debug APK con todos los fixes A–I aplicados  
**Device:** Android 14, dispositivo físico  
**Context:** DB migró de v1→v2 (FTS), lo que causó wipe completo de datos cacheados

### Checklist Sesión 2

| # | Flow | Scenario | Resultado Esperado | Resultado Real |
|---|---|---|---|---|
| 1 | Initial load | App launch, DB vacía post-migración | Shimmer → data | ⚠️ Muestra "not found" brevemente (Finding K) |
| 2 | Initial load speed | App launch con data en DB | Data inmediata | ⚠️ Lento — DB fue wiped por migración FTS (Finding L) |
| 3 | Navigation transition | Tap article → shared element image | Imagen vuela list→detail | ❌ Transición no visible (Finding M) |
| 4 | Detail hero image | Imagen hero en pantalla de detalle | Aspect ratio correcto | ⚠️ Sigue estirada/pixelada en algunos casos (Finding N) |
| 5 | Search — prefix | Escribir "arg" → aparece "Argentina" | Resultados parciales | ❌ FTS solo matchea palabras completas (Finding O) |
| 6 | Pull to refresh | Swipe down en listado | Recarga desde API | ❌ No existe pull-to-refresh (Finding P) |
| 7 | Pull to refresh | Swipe down en detalle | Recarga artículo | ❌ No existe pull-to-refresh (Finding P) |
| 8 | Offline — search | Búsqueda en modo avión | Empty state o banner | ⚠️ Muestra "unable to resolve host" luego data (Finding Q) |
| 9 | Offline — banner | App abierta en modo avión con cache | Banner con timestamp | ✅ Funciona — banner + timestamp correcto |
| 10 | Images — HTTPS | Imágenes con URL https:// | Se cargan normalmente | ⚠️ Algunas no cargan (Finding R) |
| 11 | Images — shimmer | Carga de imagen durante scroll | Shimmer placeholder | ✅ Shimmer visible durante loading |
| 12 | Images — error state | URL inválida o 404 | Box gris (surfaceVariant) | ✅ Funciona correctamente |
| 13 | Search — debounce | Tipeo rápido | Un solo request por burst | ✅ Debounce 500ms efectivo |
| 14 | Search — append | Scroll en resultados de búsqueda | Segunda página carga | ✅ Fix B funcionando |
| 15 | Rotation | Portrait ↔ landscape | Estado preservado | ✅ Sin regresiones |

---

### Findings — Sesión 2

#### K — "Not Found" Flash en Launch `Priority 1`

**Observed:** Al abrir la app, aparece brevemente el mensaje "No articles found" antes del shimmer, incluso cuando el mediator está a punto de ejecutar un REFRESH.

**Root cause:** Cuando `mediatorRefresh == null` (el mediator no ha sido invocado aún) y `itemCount == 0`, ninguna condición del `when` en `NewsContent` captura el estado correctamente. Cae al branch `else → ArticleList` con 0 items, pero si momentáneamente pasa por `mediatorRefresh is LoadState.NotLoading` (estado inicial de Paging), dispara `EmptyState`.

**Diferencia con Finding A:** Finding A era el flicker durante la carga (clear+insert). Finding K es el "not found" instantáneo antes de que el mediator arranque.

**Fix location:** `NewsScreen.kt` — agregar condición `mediatorRefresh == null && itemCount == 0 → LoadingState`.

**Impacto:** Alto — primera impresión negativa en cada apertura con DB vacía.

---

#### L — Performance Aparente (DB Wipe por Migración FTS) `Priority 2`

**Observed:** La app parece lenta al mostrar datos incluso cuando ya existían en la DB en sesiones anteriores.

**Root cause:** Al agregar `ArticleFts` (Fix C), la DB bumpeó de versión 1 a 2. `DatabaseModule` usa `fallbackToDestructiveMigration(dropAllTables = true)`, lo que destruye todos los datos existentes en la primera apertura post-update. El usuario experimenta la carga de red completa aunque haya usado la app antes.

**No es un bug de performance:** La app de por sí es rápida. El problema fue la migración destructiva inevitable al agregar FTS.

**Fix:** Una vez que el usuario scrollea y Room se repopula, la experiencia es fluida. Para evitar esto en el futuro: escribir una `Migration` explícita que solo agregue la tabla FTS en lugar de dropear todo.

**Fix location:** `DatabaseModule.kt` — reemplazar `fallbackToDestructiveMigration` por `Migration(1, 2)` que solo ejecuta `CREATE VIRTUAL TABLE articles_fts`.

---

#### M — Transición de Navegación No Visible `Priority 3`

**Observed:** Al tocar un artículo, la pantalla de detalle aparece instantáneamente sin ninguna animación. La imagen no vuela entre la card del listado y el hero del detalle.

**Root cause investigado:** El `SharedTransitionLayout` + `CompositionLocalProvider(LocalAnimatedVisibilityScope provides this)` fue implementado (Fix I). Sin embargo, la animación no es visible. Posibles causas:
1. El `ShimmerBox` overlay en `NetworkImage` cubre la imagen durante la transición, ocultando el shared element
2. `ArticleCard` puede tener `clip = true` en la superficie de la Card, impidiendo que el shared element "vuele" fuera del bounds de la card
3. La transición de Navigation Compose puede ser demasiado corta para apreciarla

**Fix location:** Investigar en detalle — revisar `ArticleCard` clipping, considerar usar `sharedBounds` en lugar de `sharedElement`, y agregar enter/exit transitions explícitas al NavHost.

---

#### N — Imagen Hero Sigue Estirada/Pixelada `Priority 2`

**Observed:** Fix E cambió `ContentScale.Crop` por `ContentScale.FillWidth` + `aspectRatio(16/9)`. Imágenes pequeñas siguen pixeladas (se upscalea para llenar el ancho). Imágenes con ratio distinto a 16:9 se ven mal.

**Root cause:** `ContentScale.FillWidth` escala la imagen para llenar el ancho completo, lo que fuerza upscaling en imágenes pequeñas. El `aspectRatio(16/9)` es un ratio fijo que no respeta el ratio original de la imagen.

**Fix:** Usar `ContentScale.Fit` con `heightIn(max = 280.dp)`. Esto muestra la imagen a su tamaño natural dentro del contenedor sin distorsión, con una altura máxima razonable.

---

#### O — FTS Solo Matchea Palabras Completas `Priority 2`

**Observed:** La búsqueda con FTS4 (Fix C) mejoró la tokenización y case-insensitivity, pero no soporta prefix matching. "arg" no trae resultados de "Argentina", "launch" no trae "launching".

**Root cause:** FTS4 por defecto solo matchea tokens exactos. Para prefix matching, SQLite FTS4 soporta el operador wildcard `*` (e.g., `"arg*"` matchea "argentina"). La query actual no aplica este transformación.

**Fix location:** Transformar la query antes de pasarla al DAO — dividir por espacios y agregar `*` a cada token: `"space x"` → `"space* x*"`.

---

#### P — Pull to Refresh No Implementado `Priority 2`

**Observed:** No existe gesto de swipe-to-refresh en ninguna pantalla. Los usuarios no tienen forma de forzar una recarga de datos sin cerrar y reabrir la app.

**Expected:** Swipe down en el listado → recarga desde API (llama `articles.refresh()`). Swipe down en detalle → recarga el artículo desde API.

**Fix location:**
- `NewsScreen.kt` — envolver contenido en `PullToRefreshBox` (Material3 experimental)
- `DetailScreen.kt` — ídem con `viewModel.onEvent(DetailUiEvent.RetryClicked)`

---

#### Q — Mensaje de Error Crudo en Búsqueda Offline `Priority 3`

**Observed:** Al buscar en modo avión, aparece brevemente "CLEARTEXT communication to..." o "Unable to resolve host 'api.spaceflightnewsapi.net'" antes de mostrar el estado offline con datos cacheados.

**Root cause:** `NewsContent` usa `mediatorRefresh.error.message` directamente como texto del `ErrorState`. El mensaje es la excepción de red cruda de OkHttp/Retrofit.

**Fix location:** `NewsScreen.kt` — detectar si el error es `IOException` (error de red) y mostrar un mensaje amigable: "No internet connection. Check your connection and try again."

---

#### R — Imágenes HTTPS Que No Cargan `Priority 4 (Informativo)`

**Observed:** Algunas imágenes en el listado muestran el estado de error (box gris) aunque su URL comienza con `https://`.

**Root cause investigado:** No es un bug de código. Las URLs de imágenes en la Space Flight News API provienen de distintas fuentes (news sites externos). Algunas URLs son inválidas, expiradas, o el host tiene SSL configurado de forma incompatible con algunos dispositivos. Coil 3 falla correctamente y muestra el error state (`surfaceVariant` box). Comportamiento esperado.

**Fix:** No requiere cambio de código. El error state actual (box gris) es correcto. Como mejora visual futura, se podría agregar un ícono de imagen rota sobre el surfaceVariant.

---

### Fix Backlog Actualizado (Sesión 2)

| ID | Finding | Prioridad | Estado | Stage |
|---|---|---|---|---|
| A | Shimmer flicker inicial (clear+insert) | 1 — Crítico | ✅ Fix aplicado | 13 |
| B | Search infinite spinner (APPEND offset) | 1 — Crítico | ✅ Fix aplicado | 13 |
| G | PAGE_SIZE 32, debounce 500ms | 1 — Quick win | ✅ Fix aplicado | 13 |
| E | Detail hero ContentScale | 2 — UX | ✅ Fix aplicado (parcial, ver N) | 13 |
| F | Shimmer placeholder + error fallback | 2 — UX | ✅ Fix aplicado | 13 |
| C | Room FTS tokenized search | 3 — UX | ✅ Fix aplicado (ver O para prefix) | 13 |
| D | Offline cache + connectivity banner | 4 — Offline-first | ✅ Fix aplicado | 13 |
| H | Offline banner timestamp | 4 — Offline-first | ✅ Fix aplicado | 13 |
| I | Shared element transition | 5 — Polish | ✅ Fix aplicado (ver M para revisión) | 13 |
| J | Scroll jank | 6 — Performance | ⏭️ Deferido (requiere profiling) | 13 |
| **K** | **"Not found" flash en launch** | **1 — Crítico** | **✅ Fix aplicado** | **13** |
| **L** | **DB wipe por migración FTS** | **2 — UX** | **✅ Fix aplicado (Migration 1→2 explícita)** | **13** |
| **M** | **Transición nav no visible** | **3 — Polish** | **⏭️ Deferido (ver notas)** | **13** |
| **N** | **Imagen hero sigue distorsionada** | **2 — UX** | **✅ Fix aplicado (ContentScale.Fit)** | **13** |
| **O** | **FTS sin prefix matching** | **2 — UX** | **✅ Fix aplicado (token* transform)** | **13** |
| **P** | **No hay pull-to-refresh** | **2 — UX** | **✅ Fix aplicado (PullToRefreshBox)** | **13** |
| **Q** | **Mensaje de error crudo offline** | **3 — UX** | **✅ Fix aplicado (IOException → friendly msg)** | **13** |
| **R** | **Imágenes HTTPS no cargan** | **4 — Informativo** | **✅ Comportamiento esperado** | **—** |
| **S** | **"Not found" flash persiste (sourceRefresh gap)** | **1 — Crítico** | **⬜ Pendiente** | **13** |
| **T** | **Snackbar "Showing cached data" en cada apertura** | **2 — UX** | **⬜ Pendiente** | **13** |
| **U** | **Banner offline sticky → reemplazar con Snackbar** | **2 — UX** | **⬜ Pendiente** | **13** |
| **V** | **Sin documentación de schema de DB** | **3 — Docs** | **⬜ Pendiente** | **13** |

---

## Tercera Sesión de Testing (post Stage 13 fixes K–Q)

**App variant:** APK release firmado con debug keystore (R8 activo)
**Device:** Android 14, dispositivo físico
**Context:** Primera prueba sobre APK release — requirió fix de ProGuard (`-keepattributes Signature`) para que `NetworkResultCallAdapterFactory` funcione con genéricos en runtime

### Findings — Sesión 3

#### S — "Not found" Flash Persiste `Priority 1`

**Observed:** Fix K no eliminó el flash. La secuencia sigue siendo: shimmer → "No articles found" → shimmer → data. Ocurre tanto en el arranque inicial como durante búsquedas.

**Root cause (refinado):** Fix K agregó `mediatorRefresh == null` a la condición de `LoadingState`. Sin embargo, hay una ventana más: después de que el mediator completa (`MediatorResult.Success`) y Room ejecuta el insert, SQLite invalida el PagingSource y dispara una nueva query. Durante esta re-query, `sourceRefresh = Loading`, pero `mediatorRefresh = NotLoading`. En ese frame: `itemCount = 0`, `mediatorRefresh = NotLoading`, `sourceRefresh = Loading` → cae al branch `EmptyState` instantáneamente.

**Diferencia con A y K:** Finding A era el flicker clear+insert durante REFRESH. Finding K era el frame antes de que el mediator arranque. Finding S es el frame entre que el mediator termina y Room emite los resultados.

**Fix location:** `NewsScreen.kt` — agregar `|| sourceRefresh is LoadState.Loading` a la condición de `LoadingState`.

---

#### T — Snackbar "Showing Cached Data" en Cada Apertura `Priority 2`

**Observed:** Al abrir la app, aparece siempre el Snackbar "Showing cached data — pull to refresh", incluso cuando hay conexión y la data es reciente.

**Root cause:** `NewsViewModel.checkStaleness()` se ejecuta en `init`. Llama a `repository.isDataStale()` que retorna `true` cuando `remoteKeysDao.getLastFetchedAt() == null` (primera instalación) o cuando el TTL venció. En la práctica, se dispara en casi todas las aperturas porque el TTL es corto o porque la data está "desactualizada" al momento del check sincrónico.

**Impact:** Alto — el mensaje aparece aunque el usuario tenga conexión perfecta, confundiendo si la app está offline o no.

**Fix location:** `NewsViewModel.kt` — eliminar `checkStaleness()`. El Snackbar de offline (Fix U) reemplaza esta señal con una más precisa.

---

#### U — Banner Offline Sticky Reemplazar con Snackbar Auto-dismiss `Priority 2`

**Observed:** El `ConnectivityBanner` es un item pegado en el top del `LazyColumn`. Aparece en algunas aperturas aunque haya conexión (race condition entre paging state y mediator refresh). Usuario prefiere notificación tipo globo/Snackbar que aparezca solo cuando la app detecta que está offline y se vaya sola.

**Root cause del problema de siempre-visible:** `ArticleList` muestra el banner cuando `offlineBannerTimestamp != null` (condición mal definida) o cuando `mediatorRefresh is LoadState.Error`. En hiccups momentáneos de red el mediator reporta Error → banner aparece → red se recupera → mediator refreshea → banner desaparece, pero el usuario ya lo vio.

**Fix:** Eliminar `ConnectivityBanner` composable. En `NewsScreen`, agregar `LaunchedEffect(articles.loadState.mediator?.refresh)` que muestra Snackbar cuando `mediatorRefresh is Error && itemCount > 0`. El Snackbar dura `SnackbarDuration.Short` (4s) y desaparece automáticamente.

---

#### V — Sin Documentación de Schema de DB `Priority 3`

**Observed:** No existe un documento que explique el diseño de la base de datos: tablas, columnas, relaciones, y especialmente qué es la tabla `articles_fts` y cómo funciona FTS4.

**Fix location:** Crear `docs/DATABASE.md` con schema completo de las 3 tablas, diagrama ER, historia de migraciones, y explicación detallada de FTS4 (tokenización, MATCH, prefix wildcard `*`).

---

### Checklist Sesión 3

| # | Flow | Scenario | Resultado Esperado | Resultado Real |
|---|---|---|---|---|
| 1 | Initial load | App launch — APK release | Shimmer → data sin flash | ✅ Sin flash (fix S aplicado) |
| 2 | Initial load | "Not found" flash eliminado | Sin EmptyState intermedio | ✅ Resuelto |
| 3 | Offline Snackbar | Modo avión con cache | Snackbar breve, no sticky | ✅ Snackbar auto-dismiss (fix U) |
| 4 | Offline Snackbar | App abierta con conexión | Sin Snackbar en apertura normal | ✅ No aparece (fix T) |
| 5 | Images — release | Imágenes con http/https | Se cargan normalmente | ❌ Sin imágenes en release (Finding X) |
| 6 | App launch — release | Cold start | Sin crash | ❌ Crash inmediato (Finding W) |
| 7 | ProGuard — Signature | `NetworkResult<T>` en release | Retrofit decodifica correctamente | ❌ `IllegalStateException` (Finding W) |
| 8 | Images — error state | URL inválida / host con error | Box gris visible | ⚠️ Box no visible (Finding X-b) |
| 9 | Search — offline | Búsqueda en modo avión sin cache | EmptyState amigable | ✅ Fix Q funcionando |
| 10 | Shimmer height | Launch sin cache | Shimmer ocupa toda la pantalla | ⚠️ Solo mitad de pantalla (Finding Y) |
| 11 | Pagination scroll | Scroll al final de la lista | Carga más artículos seamless | ⚠️ Salto de posición al cargar (Finding Z) |
| 12 | Pagination APPEND | Scroll profundo en lista | Carga continua sin bloqueo | ❌ APPEND bloqueado sin pull-to-refresh (Finding AA) |

---

### Findings — Sesión 3 (nuevos)

#### W — Crash en Release: `NetworkResult<T>` con genéricos `Priority 1`

**Observed:** La app crashea inmediatamente al abrir el APK release con `IllegalStateException: NetworkResult must be parameterized` o `ParameterizedType expected`.

**Root cause:** R8 elimina el atributo `Signature` por defecto. `NetworkResultCallAdapterFactory.get()` inspecciona `ParameterizedType` en runtime para detectar `NetworkResult<T>`. Sin el atributo, la información de genéricos está borrada y la factory lanza excepción.

**Fix location:** `app/proguard-rules.pro` — agregar `-keepattributes Signature`.

---

#### X — Coil: Sin Imágenes en Release (ServiceLoader + R8) `Priority 1`

**Observed (X-a):** Después de fixear el crash, la app abre pero no carga ninguna imagen — todas muestran shimmer infinito.

**Root cause:** Coil 3 registra `OkHttpNetworkFetcherFactory` via `META-INF/services/` (ServiceLoader pattern). R8 ofusca los nombres de clase, el ServiceLoader resuelve el nombre original del `.services` file pero no encuentra la clase ofuscada → fetcher nunca se registra → todas las imágenes fallan silenciosamente con shimmer.

**Fix location:** `app/proguard-rules.pro` — agregar `-keep class coil3.** { *; }`, `-keepnames class coil3.**`, y las reglas de OkHttp necesarias para el fetcher.

**Observed (X-b):** Al mismo tiempo, el error state del placeholder no era visible. Causa secundaria: el `when` de `NetworkImage.kt` usaba sintaxis mixta `is Type, Singleton →` que en release podía no evaluar todos los casos correctamente. Fix: migrar a `when { state is X -> }` con condiciones individuales. Color de error cambiado de `surfaceVariant` a `surfaceContainerHighest` para que sea visible sobre el fondo de la card.

---

#### Y — Shimmer Ocupa Solo Mitad de Pantalla `Priority 3`

**Observed:** Los skeletons de carga (`LoadingState`) muestran 6 items, que ocupan aproximadamente la mitad del alto de pantalla. El área inferior queda en blanco durante la carga inicial.

**Root cause:** `LoadingState` tenía `items(6)` hardcodeado. En dispositivos con pantalla alta (>700dp) 6 items no alcanzan a cubrir el viewport completo.

**Fix location:** `core/ui-components/src/main/java/.../LoadingState.kt` — cambiar `items(6)` a `items(12)`.

---

#### Z — Salto de Posición al Cargar Página Siguiente `Priority 2`

**Observed:** Al scrollear hasta el final de la primera página (32 items) y esperar que cargue la siguiente, la lista vuelve ~32 posiciones hacia arriba abruptamente.

**Root cause:** `PagingConfig.prefetchDistance = 5` (default). El APPEND se dispara cuando el usuario está a solo 5 items del final. Si la red tarda >1s, el usuario llega al borde → PagingSource invalida → anchor-based restore → salto visible. Con `prefetchDistance = 5`, el buffer era demasiado pequeño.

**Fix location:** `core/data/src/main/java/.../repository/ArticleRepositoryImpl.kt` — aumentar `prefetchDistance` de 5 a 15. Esto dispara el APPEND 15 items antes del final, dando suficiente margen para que la red responda antes de que el usuario llegue al borde.

---

#### AA — APPEND Bloqueado: `endOfPaginationReached = true` Prematuro `Priority 1`

**Observed:** Al llegar al final de la lista inicial (32 items), no se carga la siguiente página. No hay llamada a la API, no hay spinner. El scroll queda bloqueado. Después de un pull-to-refresh, el scroll infinito funciona correctamente.

**Root cause:** `ArticleRemoteMediator.load(LoadType.APPEND)` tenía:
```kotlin
val lastItem = state.lastItemOrNull()
    ?: return MediatorResult.Success(endOfPaginationReached = true)  // ← bug
val remoteKey = remoteKeysDao.getByArticleId(lastItem.id)
    ?: return MediatorResult.Success(endOfPaginationReached = true)  // ← bug
```
Paging 3 puede disparar APPEND antes de que el PagingSource haya emitido sus primeros items (estado inicial vacío). En ese caso `lastItemOrNull()` es `null` → el mediator retorna `endOfPaginationReached = true` → Paging cachea ese estado y deja de llamar APPEND para el resto de la sesión. Pull-to-refresh dispara REFRESH que resetea el estado de paginación.

Este patrón incorrecto es un antipatrón documentado en la guía oficial de Paging 3: `null lastItem` o `null remoteKey` significa que el DB todavía no tiene datos (REFRESH en curso), no que se llegó al final.

**Fix location:** `core/data/src/main/java/.../mediator/ArticleRemoteMediator.kt` — cambiar ambos `?: return MediatorResult.Success(endOfPaginationReached = true)` a `endOfPaginationReached = false`. Solo el `remoteKey.nextKey == null` debe retornar `true`.

---

### Fix Backlog Final (Sesión 3)

| ID | Finding | Prioridad | Estado |
|---|---|---|---|
| A | Shimmer flicker inicial (clear+insert) | 1 — Crítico | ✅ Aplicado |
| B | Search infinite spinner (APPEND offset) | 1 — Crítico | ✅ Aplicado |
| G | PAGE_SIZE 32, debounce 500ms | 1 — Quick win | ✅ Aplicado |
| E | Detail hero ContentScale | 2 — UX | ✅ Aplicado (parcial, ver N) |
| F | Shimmer placeholder + error fallback | 2 — UX | ✅ Aplicado |
| C | Room FTS tokenized search | 3 — UX | ✅ Aplicado (ver O para prefix) |
| D | Offline cache + connectivity banner | 4 — Offline-first | ✅ Aplicado |
| H | Offline banner timestamp | 4 — Offline-first | ✅ Aplicado |
| I | Shared element transition | 5 — Polish | ✅ Aplicado (ver M) |
| J | Scroll jank | 6 — Performance | ⏭️ Deferido (requiere profiling) |
| K | "Not found" flash en launch | 1 — Crítico | ✅ Aplicado |
| L | DB wipe por migración FTS | 2 — UX | ✅ Aplicado (Migration 1→2 explícita) |
| M | Transición nav no visible | 3 — Polish | ⏭️ Deferido |
| N | Imagen hero distorsionada | 2 — UX | ✅ Aplicado (ContentScale.Fit) |
| O | FTS sin prefix matching | 2 — UX | ✅ Aplicado (token* transform) |
| P | No hay pull-to-refresh | 2 — UX | ✅ Aplicado (PullToRefreshBox) |
| Q | Mensaje de error crudo offline | 3 — UX | ✅ Aplicado (IOException → friendly msg) |
| R | Imágenes HTTPS no cargan | 4 — Informativo | ✅ Comportamiento esperado del servidor |
| S | "Not found" flash (sourceRefresh gap) | 1 — Crítico | ✅ Aplicado |
| T | Snackbar "Showing cached data" en cada apertura | 2 — UX | ✅ Aplicado (eliminado checkStaleness) |
| U | Banner offline sticky → Snackbar auto-dismiss | 2 — UX | ✅ Aplicado |
| V | Sin documentación de schema de DB | 3 — Docs | ✅ Aplicado (`docs/DATABASE.md`) |
| **W** | **Release crash: ProGuard Signature** | **1 — Crítico** | **✅ Aplicado** |
| **X** | **Coil sin imágenes en release (ServiceLoader + R8)** | **1 — Crítico** | **✅ Aplicado** |
| **Y** | **Shimmer ocupa solo mitad de pantalla** | **3 — UX** | **✅ Aplicado (items 6 → 12)** |
| **Z** | **Salto de posición al cargar página** | **2 — UX** | **✅ Aplicado (prefetchDistance 5 → 15)** |
| **AA** | **APPEND bloqueado: endOfPaginationReached prematuro** | **1 — Crítico** | **✅ Aplicado** |
