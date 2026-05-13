# Stage 13.3 — Adaptive Two-Pane Layout & Theme Toggle

## Objective

Improve the tablet landscape experience by splitting the screen into a master-detail layout, and provide a single, app-wide light/dark theme toggle.

## Tasks

- [x] Detect landscape tablet (≥600dp wide, landscape orientation) via `LocalConfiguration`
- [x] Implement `SinglePaneLayout` — existing phone/portrait flow unchanged
- [x] Implement `TwoPaneLayout` — `Row { NewsScreen | VerticalDivider | NavHost(detail) }`
- [x] Empty detail pane placeholder — Article icon + "Select an article to read"
- [x] `popUpTo(placeholder) { inclusive = true }` on article tap — no back-stack buildup
- [x] Create `LocalThemeToggle` CompositionLocal in `:core:ui-components`
- [x] Hold theme state in `MainActivity` via `rememberSaveable { mutableStateOf<Boolean?>(null) }`
- [x] Add theme toggle `IconButton` (sun/moon) in NewsScreen search row — single source of truth
- [x] Add `material-icons-extended` dependency to `:features:news` and `:app` modules

## Acceptance Criteria

- Landscape tablet (≥600dp): list left pane, placeholder right pane on launch
- Tapping an article loads detail in right pane; list stays visible and scrollable
- Tapping a second article updates right pane without back-stack buildup
- Tapping back in detail restores placeholder
- Rotating to portrait switches to single-pane with standard navigation
- Theme toggle lives only in NewsScreen — one button, entire app switches theme
- Portrait phones and tablets: single-pane flow identical to pre-13.3

## Implementation Notes

**Two-pane NavHost pattern:** The detail right pane has its own `NavHost`. This gives `DetailViewModel` a real `NavBackStackEntry`, so `hiltViewModel()` and `SavedStateHandle` work without any ViewModel changes.

**SharedTransition in two-pane:** `LocalSharedTransitionScope` and `LocalAnimatedVisibilityScope` are not provided in `TwoPaneLayout`. Both default to `null`; screens handle null gracefully, so the shared element animation is silently skipped in two-pane mode (correct behavior — elements are always visible, no enter/exit to animate).

**Theme toggle location:** Toggle is intentionally placed only in NewsScreen. In two-pane the left pane is always visible, so it's always reachable. In single-pane, the user can navigate back to change the theme. Placing it in DetailScreen would create two independent-looking controls for the same state.

**`rememberSaveable` resets on process kill:** Theme choice reverts to system default after the OS kills the process. This is acceptable for a showcase app.
