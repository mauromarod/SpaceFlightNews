# Stage 06 — Design System

**Status:** ✅ Complete
**Depends on:** Stage 01
**Estimated effort:** 2–3h
**Progress:** 8 / 8 tasks (100%)

---

## Objective

Build the custom design system in `:core:designsystem`. All visual tokens (color, typography, shape, spacing) are defined here. No raw color literals, hardcoded dimension values, or `MaterialTheme` direct references appear outside this module. Feature modules and UI components always consume tokens through `SpaceFlightNewsTheme`.

---

## Tasks

### Color Tokens
- [x] Create `Color.kt` — define all brand colors as `val` Color objects (primary, secondary, surface, background, error, on-* variants for both light and dark palettes). Include at least: a dark space-themed primary, a contrasting accent, neutral surfaces.
- [x] Create `ColorScheme.kt` (or inline in `Theme.kt`) — define `lightColorScheme()` and `darkColorScheme()` using Material3's factory functions with the brand color tokens

### Typography
- [x] Create `Typography.kt` — define `SpaceFlightNewsTypography` overriding Material3's `Typography` with custom `TextStyle` per scale (displayLarge, headlineMedium, titleLarge, bodyLarge, bodyMedium, labelSmall)

### Shape & Spacing
- [x] Create `Shape.kt` — define `SpaceFlightNewsShapes` using Material3's `Shapes`; use rounded corners consistent with the space/tech aesthetic
- [x] Create `Spacing.kt` — `object SpaceFlightNewsSpacing` with named spacing constants: `xSmall = 4.dp`, `small = 8.dp`, `medium = 16.dp`, `large = 24.dp`, `xLarge = 32.dp`

### Theme Composable
- [x] Create `Theme.kt` — `SpaceFlightNewsTheme(darkTheme: Boolean, content: @Composable () -> Unit)` composable that wraps `MaterialTheme` with the custom colors, typography, and shapes; detect system dark mode via `isSystemInDarkTheme()`

### Design System Extensions
- [x] Create `ThemeExtensions.kt` — extension properties on `MaterialTheme` companion for direct token access: `MaterialTheme.spacing` (returns `SpaceFlightNewsSpacing`), so feature modules never need to import `SpaceFlightNewsSpacing` directly

### Migration
- [x] Delete `app/src/main/java/com/mauromarod/spaceflightnews/ui/theme/` (Color.kt, Theme.kt, Type.kt) and update `MainActivity` to use `SpaceFlightNewsTheme` from `:core:designsystem`

---

## Acceptance Criteria

- No raw `Color(0xFF...)` or `colorResource()` calls exist outside `Color.kt`
- No `16.dp` or similar hardcoded values exist outside `Spacing.kt` in any Composable
- `SpaceFlightNewsTheme` correctly applies dark theme when the system is in dark mode (verified visually)
- The old `app/ui/theme/` directory is deleted — zero duplicate theme definitions
- `./gradlew :core:designsystem:assembleDebug` passes

---

## Implementation Notes

**Color palette approach:** Choose a space-themed dark palette with high contrast. Suggested base:
- Background: `#0A0E1A` (deep space dark)
- Primary: `#4FC3F7` (light blue, reminiscent of Earth from space)
- Secondary: `#FF8A65` (orange, rocket exhaust)
- Surface: `#12172A`
- On-Primary / On-Surface: `#FFFFFF` or near-white

Adapt as needed — the important thing is that all values are named constants in `Color.kt`.

**Material3 `ColorScheme`:** Use `lightColorScheme(primary = ..., secondary = ..., ...)` and `darkColorScheme(...)`. Map your brand colors to the M3 roles. Do not override every role — only the ones with a design decision behind them.

**`MaterialTheme.spacing` extension:** This pattern avoids importing `SpaceFlightNewsSpacing` in every composable file:
```kotlin
val MaterialTheme.spacing: SpaceFlightNewsSpacing
    get() = SpaceFlightNewsSpacing
```
Feature modules can then use `MaterialTheme.spacing.medium` with only a `MaterialTheme` import.

**Remove old theme files carefully:** The old `Theme.kt` in `:app` references `dynamicColor` from Material3. The custom design system replaces this. Dynamic color (system wallpaper-based theming) is intentionally not supported — the brand palette takes precedence.
