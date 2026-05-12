# Copilot instructions for AiChat

This file helps future Copilot sessions understand how to build, test, and navigate this Android repository.

## 1) Build, test, and lint commands
- Install dependencies / build the project (root):
  - ./gradlew build
  - ./gradlew assembleDebug

- Run unit tests:
  - All unit tests: ./gradlew test
  - App module unit tests (debug): ./gradlew :app:testDebugUnitTest
  - Run a single unit test (example):
    ./gradlew :app:testDebugUnitTest --tests "dev.alexrincon.aichat.somepackage.MyTest.testMethod"
    or to match class: --tests "*MyTestClass*"

- Run instrumentation (Android) tests:
  - ./gradlew connectedAndroidTest
  - Or for the app module explicitly: ./gradlew :app:connectedDebugAndroidTest
  (requires a connected device or emulator)

- Lint / static analysis:
  - Android lint for app: ./gradlew :app:lint

- Codegen / KSP:
  - KSP runs as part of a build. To force generation: ./gradlew :app:assembleDebug

Notes:
- Prefix tasks with :app: to target the app module directly.
- Use Android Studio (Hedgehog+) for iterative development and emulator management.

## 2) High-level architecture (big picture)
- Single-module Android app (root includes `:app`).
- UI: Jetpack Compose (Material 3).
- Architecture: MVVM + Clean Architecture. Layers under `app/`:
  - data/: datasource (OpenAI API), local (Room), mapper, model, repository
  - di/: Hilt modules
  - ui/: composable screens (e.g., chat) and theme
  - util/: helpers (StringProvider, etc.)
- Dependency Injection: Hilt
- Persistence: Room (KSP for annotation processing)
- AI integration: OpenAI Kotlin client (openai-client BOM + ktor okhttp)
- OpenAI configuration:
  - API key read from `local.properties` and exposed as BuildConfig.OPENAI_API_KEY (see `app/build.gradle.kts`).
  - Default model selection is in `app/src/main/java/dev/alexrincon/aichat/data/datasource/OpenAIDataSource.kt` (contains `model = ModelId("gpt-4o-mini")`).

## 3) Key conventions and repository-specific patterns
- OpenAI API key handling:
  - Add `OPENAI_API_KEY=your-key` to `local.properties` (DO NOT commit).
  - Build exposes the key as `BuildConfig.OPENAI_API_KEY` (app/build.gradle.kts).

- Where to change model or token:
  - Change model string in `OpenAIDataSource.kt`.
  - Token is provided via Hilt module `app/src/main/java/dev/alexrincon/aichat/di/OpenAIModule.kt` (uses BuildConfig).

- Module/task naming:
  - Use `:app:` prefix for module-scoped gradle tasks (e.g., `:app:assembleDebug`, `:app:lint`).

- Code generation / annotation processing:
  - Project uses KSP for Room and Hilt codegen. Building will trigger KSP; avoid running tests that rely on generated code without a prior successful assemble/build.

- Versions and dependencies:
  - Centralized via `gradle/libs.versions.toml` (version catalogs). Prefer adding dependencies via the catalog.

- Tests:
  - Unit tests live under `app/src/test/java` and Android instrumented tests under `app/src/androidTest/java`.
  - Example gradle test task for a single test provided above.

## 4) Files & locations worth noting (quick pointers)
- Main app module: `app/`
- OpenAI data integration: `app/src/main/java/dev/alexrincon/aichat/data/datasource/OpenAIDataSource.kt`
- DI token & module: `app/src/main/java/dev/alexrincon/aichat/di/OpenAIModule.kt`
- Room entities/DAOs: `app/src/main/java/.../local/`
- UI: `app/src/main/java/.../ui/` (screens, theme)

## 5) Other AI assistant configs checked
- No CLAUDE.md, .cursorrules, AGENTS.md, .windsurfrules, CONVENTIONS.md, or similar assistant config files were present at repository root.

---

If anything above should be expanded (e.g., exact test class path examples, CI task names, or commands for emulator/device setup), say which area to extend and adjustments will be made.
