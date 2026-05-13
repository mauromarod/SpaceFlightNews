# Stage 00 — Documentation

**Status:** ✅ Complete
**Depends on:** —
**Estimated effort:** Complete
**Progress:** 6 / 6 tasks (100%)

---

## Objective

Establish the reference documentation set that serves as the engineering contract for all subsequent development. All files are in English and live in `/docs/` (except `README.md`).

---

## Tasks

- [x] Create `README.md` at project root (badges, setup, module structure, doc links)
- [x] Create `docs/ARCHITECTURE.md` (module graph, MVI contract, data flow diagrams, design decisions)
- [x] Create `docs/API_CONTRACT.md` (endpoints, DTOs, domain model, error handling, stale data policy)
- [x] Create `docs/TECH_STACK.md` (full dependency table with versions and rationale)
- [x] Create `docs/TESTING_STRATEGY.md` (pyramid, patterns, Robot Pattern, Roborazzi, Maestro, CI)
- [x] Create `docs/roadmap/` with `INDEX.md` and all stage files

---

## Acceptance Criteria

- [x] All five primary doc files exist and are non-empty
- [x] No Spanish text in any markdown file (PDF is exempt)
- [x] No references to job search, interview, or seniority in any file
- [x] `README.md` links resolve to correct relative paths in `/docs/`
- [x] `docs/roadmap/INDEX.md` lists all stages with status and estimates
