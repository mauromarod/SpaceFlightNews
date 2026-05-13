# Development Roadmap

## Progress Overview

| Stage | Name | Status | Est. Hours | Tasks | Done |
|---|---|---|---|---|---|
| [00](STAGE-00-documentation.md) | Documentation | ✅ Complete | 0h remaining | 6 | 6 |
| [01](STAGE-01-gradle-setup.md) | Gradle Multi-Module Setup | ✅ Complete | 0h remaining | 14 | 14 |
| [02](STAGE-02-domain.md) | Core Domain Module | ✅ Complete | 0h remaining | 8 | 8 |
| [03](STAGE-03-network.md) | Core Network Module | ✅ Complete | 0h remaining | 9 | 9 |
| [04](STAGE-04-database.md) | Core Database Module | ✅ Complete | 0h remaining | 10 | 10 |
| [05](STAGE-05-data.md) | Core Data Module | ✅ Complete | 0h remaining | 9 | 9 |
| [06](STAGE-06-design-system.md) | Design System | ✅ Complete | 0h remaining | 8 | 8 |
| [07](STAGE-07-ui-components.md) | UI Components | ✅ Complete | 0h remaining | 12 | 12 |
| [08](STAGE-08-feature-news.md) | Feature: News List | ✅ Complete | 0h remaining | 13 | 13 |
| [09](STAGE-09-feature-detail.md) | Feature: Article Detail | ✅ Complete | 0h remaining | 10 | 10 |
| [10](STAGE-10-navigation-wiring.md) | Navigation & App Wiring | ✅ Complete | 0h remaining | 9 | 9 |
| [10.1](STAGE-10.1-search-remote-mediator.md) | Search RemoteMediator | ✅ Complete | 0h remaining | 3 | 3 |
| [11](STAGE-11-testing.md) | Testing Suite | ✅ Complete | 0h remaining | 18 | 16 |
| [11.1](STAGE-11.1-manual-testing.md) | Manual Testing & Fix Backlog | ✅ Complete | 0h remaining | 6 | 6 |
| [12](STAGE-12-ci-quality.md) | CI/CD & Static Analysis | ✅ Complete | 0h remaining | 10 | 8 |
| [13](STAGE-13-performance-polish.md) | Performance & Polish | ✅ Complete | 0h remaining | 19 | 19 |
| [13.3](STAGE-13.3-adaptive-layout.md) | Adaptive Layout & Theme Toggle | ✅ Complete | 0h remaining | 9 | 9 |
| [14](STAGE-14-firebase.md) | Firebase Integration _(stretch)_ | ✅ Complete | 0h remaining | 39 | 39 |

---

## Summary

| Metric | Value |
|---|---|
| **Total stages** | 18 (17 MVP + 1 stretch) |
| **Total tasks** | 212 |
| **Completed tasks** | 212 |
| **Overall progress** | 100% |
| **MVP estimated effort** | 0h remaining — MVP complete |
| **With stretch (Firebase)** | 0h remaining — all stages complete |
| **Current status** | All stages complete ✅ — Stage 14 (Firebase) closed. |

---

## Dependency Graph

```
Stage 00 ──► Stage 01 ──► Stage 02 ──► Stage 03
                 │               │               │
                 │               └───────────────┤
                 │                               ▼
                 │               Stage 04 ──► Stage 05
                 │                               │
                 ├──► Stage 06                   │
                 │         │                     │
                 │         ▼                     │
                 └──► Stage 07 ◄─────────────────┘
                           │
                    ┌──────┴──────┐
                    ▼             ▼
               Stage 08      Stage 09
                    │             │
                    └──────┬──────┘
                           ▼
                      Stage 10
                           │
                    ┌──────┼──────┐
                    ▼      ▼      ▼
               Stage 11  Stage 12  Stage 13
                                      │
                                  Stage 14
```

**Critical path (longest dependency chain):**
`01 → 02 → 04 → 05 → 07 → 08 → 10 → 11 → 12 → 13`

Stages 03, 06, and 09 can be developed in parallel once their prerequisites are complete.

---

## Status Legend

| Symbol | Meaning |
|---|---|
| ✅ | Complete — all tasks checked, acceptance criteria met |
| 🔄 | In Progress — work has started |
| ⬜ | Pending — not yet started |
| ⏸️ | Blocked — waiting on a dependency or external factor |

---

## How to Update This Index

When a task is completed in a stage file, update that stage's `Done` count and recalculate `Overall progress` in this table. Update the stage `Status` symbol when all tasks in the stage are complete.

**Completion formula:** `Overall progress = Σ(Done tasks) / Σ(Total tasks) × 100`
