# Space Flight News

An Android application for browsing and searching space flight news articles, built with a production-grade multi-module architecture.

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-Min%20SDK%2024-3DDC84?logo=android&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-2026.02.01-4285F4?logo=jetpackcompose&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVI-FF6D00)
![DI](https://img.shields.io/badge/DI-Hilt-FF6D00)
![CI](https://img.shields.io/badge/CI-GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)

---

## Overview

Space Flight News consumes the [Space Flight News API](https://api.spaceflightnewsapi.net/v4/) to present a searchable list of articles and a full article detail view. The app works offline by serving locally cached content while syncing in the background.

---

## Architecture at a Glance

The project is structured as a multi-module Gradle build. Each module has a well-defined responsibility and enforced dependency boundary.

```
:app
├── :features:news          Search + article list
├── :features:detail        Article detail
├── :core:domain            Pure Kotlin: entities, use cases, repository interfaces
├── :core:data              Repository implementations, Paging 3 RemoteMediator
├── :core:network           Retrofit, OkHttp, NetworkResult<T>
├── :core:database          Room, DAOs, PagingSource
├── :core:designsystem      Design tokens: color, typography, shape, spacing
├── :core:ui-components     Atomic Compose widgets (Slot-based APIs)
└── :core:common            Dispatchers, shared extensions
```

→ Full architecture documentation: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

---

## Screenshots

| News List — Light | News List — Dark |
|---|---|
| ![News list light mode](screenshots/list_light.png) | ![News list dark mode](screenshots/list_dark.png) |

| Article Detail — Light | Article Detail — Dark |
|---|---|
| ![Article detail light mode](screenshots/detail_light.png) | ![Article detail dark mode](screenshots/detail_dark.png) |

---

## Getting Started

### Prerequisites

- Android Studio Meerkat (2025.1) or later
- JDK 17
- Android SDK 36

### Clone

```bash
git clone https://github.com/mauromarod/SpaceFlightNews.git
cd SpaceFlightNews
```

### Local Properties

The Space Flight News API requires no authentication key. The `local.properties` file is used only for the Android SDK path (generated automatically by Android Studio) and is excluded from version control.

If the file does not exist, create it at the project root:

```properties
sdk.dir=/Users/<your-username>/Library/Android/sdk
```

### Build & Run

```bash
./gradlew :app:installDebug
```

Or open the project in Android Studio and run the `app` configuration.

### Run Tests

```bash
# Unit tests (all modules)
./gradlew testDebugUnitTest

# Snapshot tests — verify against golden images
./gradlew verifyRoborazziDebug

# Snapshot tests — record new goldens
./gradlew recordRoborazziDebug
```

### Lint & Static Analysis

```bash
./gradlew ktlintCheck
./gradlew detekt
```

---

## Documentation

| Document | Description |
|---|---|
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | Module graph, MVI contract, data flow diagrams, design decisions |
| [TECH_STACK.md](docs/TECH_STACK.md) | All dependencies with versions and rationale |
| [TESTING_STRATEGY.md](docs/TESTING_STRATEGY.md) | Testing pyramid, Robot Pattern, snapshot testing, E2E flows |
| [API_CONTRACT.md](docs/API_CONTRACT.md) | API endpoints, data models, error handling, pagination strategy |
| [roadmap/INDEX.md](docs/roadmap/INDEX.md) | Development roadmap — stage progress, estimates, dependency graph |

---

## Project Structure

```
SpaceFlightNews/
├── app/                    Application module (entry point, DI composition root)
├── features/
│   ├── news/               Article list + search screen
│   └── detail/             Article detail screen
├── core/
│   ├── domain/             Business logic (pure Kotlin)
│   ├── data/               Repository implementations
│   ├── network/            HTTP layer
│   ├── database/           Room persistence layer
│   ├── designsystem/       Design tokens and theme
│   ├── ui-components/      Shared Compose components
│   └── common/             Shared utilities
├── docs/                   Engineering documentation
├── journeys/               android-cli Journey test files
├── maestro/                Maestro E2E flow files
└── .github/workflows/      CI pipeline definitions
```

---

## License

```
Copyright 2026 Mauro Marod

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
