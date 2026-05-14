# Test Coverage Baseline

Coverage is measured with AGP's built-in JaCoCo integration (`enableUnitTestCoverage = true`).
Run reports with:

```bash
./gradlew :core:data:createDebugUnitTestCoverageReport \
          :features:news:createDebugUnitTestCoverageReport \
          :features:detail:createDebugUnitTestCoverageReport
```

HTML reports land at `<module>/build/reports/coverage/test/debug/index.html`.

---

## Why module totals look low for `:features:*`

`NewsScreenKt` / `DetailScreenKt` contain all Compose composables. JaCoCo counts them but
unit tests cannot exercise them — only instrumented UI tests can. The numbers below
split "business logic" from "UI layer" so regressions are visible regardless.

---

## Stage 10 — Baseline (2026-05-14)

### core:data · instruction coverage: **91%**

| Class | Lines | Branches | Notes |
|---|---|---|---|
| `ArticleRemoteMediator` | 98% | 96% | 1 branch: `UnknownError` path in `persist()` |
| `SearchRemoteMediator` | 100% | 100% | |
| `ArticleRepositoryImpl` | 94% | 100% | |
| `ArticleRepositoryImplKt` | 100% | n/a | top-level `pagingConfig` property |
| `ArticleMapperKt` | 100% | 100% | |
| **Total** | **96%** lines | **99%** branches | |

### features:news · ViewModel coverage: **100% lines / 100% branches**

| Class | Lines | Branches | Notes |
|---|---|---|---|
| `NewsViewModel` | 100% | 100% | RetryClicked removed (dead code), ArticleTapped simplified |
| `NewsScreenKt` | 0% | 0% | Compose UI — not unit-testable |
| **Module total** | 18% | 8% | diluted by Compose |

### features:detail · ViewModel coverage: **100% lines / 100% branches**

| Class | Lines | Branches | Notes |
|---|---|---|---|
| `DetailViewModel` | 100% | 100% | NavigateBack removed (dead code) |
| `DetailScreenKt` | 0% | 0% | Compose UI — not unit-testable |
| **Module total** | 22% | 25% | diluted by Compose |

### core:domain · **100%** (Kover)

All Use Cases and domain models covered. Enforced via ArchUnit (zero Android imports).

---

## Regression thresholds (enforce after each stage)

| Target | Threshold | How to check |
|---|---|---|
| `core:data` instruction | ≥ 90% | `createDebugUnitTestCoverageReport` → index.html Total |
| `NewsViewModel` lines | ≥ 90% | drill into package report |
| `DetailViewModel` lines | ≥ 90% | drill into package report |
| `core:domain` | ≥ 100% | `./gradlew :core:domain:koverHtmlReport` |

> If a stage adds new logic to any of the above classes, add corresponding tests
> before closing the stage. Coverage numbers go in the table below.

---

## Coverage history

| Stage | Date | core:data (instr.) | NewsViewModel (lines) | DetailViewModel (lines) | Notes |
|---|---|---|---|---|---|---|
| Stage 10 — baseline | 2026-05-11 | 91% | 97% | 96% | initial test suite |
| Release polish | 2026-05-14 | 91% | 100% | 100% | removed dead code paths (RetryClicked, NavigateBack) |
