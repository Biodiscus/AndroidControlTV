# AndroidControlTV

Android app, package `net.biodiscus.androidcontroltv`, minSdk 34, targetSdk 37, Kotlin, Jetpack Compose, Hilt, Gradle Kotlin DSL with a version catalog (`gradle/libs.versions.toml`).

## Android skill

The `.claude/skills/claude-android-skill/` folder in this repo is the source of truth for Android architecture, Compose, modularization, Gradle and testing. Always follow it when creating or changing code.

@.claude/skills/claude-android-skill/SKILL.md

Before working on a topic, read the matching reference first:

| Working on | Read |
|---|---|
| Layers, repositories, ViewModels, UiState | `.claude/skills/claude-android-skill/references/architecture.md` |
| Screens, state hoisting, navigation, theming, previews | `.claude/skills/claude-android-skill/references/compose-patterns.md` |
| New modules, dependency rules between modules | `.claude/skills/claude-android-skill/references/modularization.md` |
| Build files, convention plugins, version catalog | `.claude/skills/claude-android-skill/references/gradle-setup.md` |
| Writing tests, test doubles | `.claude/skills/claude-android-skill/references/testing.md` |

`.claude/skills/claude-android-skill/scripts/generate_feature.py` scaffolds a feature module. Prefer it over writing the boilerplate by hand.

The project's own files win over the skill where they differ:
- The skill's example version numbers are placeholders. Take real versions from `gradle/libs.versions.toml`.
- The app is a single `:app` module. Compose, Hilt (with KSP) and Navigation are set up. Room, a networking library and a real multi-module split are not. The packages are laid out like the modules of the skill (see "Project structure"), so a split later is mostly moving packages. Check the build files before assuming a dependency exists, and add it to the version catalog when a task needs it.
- There are no convention plugins and no `build-logic/`. `generate_feature.py` creates api/impl modules and does not fit the single-module layout yet, so features are written by hand in `feature/<name>/`.

## Project structure

### What the app does

Not decided yet. The app is a skeleton: one placeholder Home screen that proves Compose, Navigation, Hilt and the theme are wired up. Describe the real behaviour here when it exists.

### Packages (`app/src/main/java/net/biodiscus/androidcontroltv/`)

Each top-level package is shaped like a module of the skill. Keep the dependency direction: `feature` -> `core`, never the other way around, and never between two features.

```
core/               Pure Kotlin and data layers, one package per concern (model, repositories, ...). Depends on nothing in feature/ or ui/.
feature/home/       The placeholder start screen: navigation destination, screen.
ui/                 Theme and preview annotations.
```

A feature package holds its navigation entry (`<Name>Navigation.kt`: a `@Serializable` destination plus a `NavGraphBuilder` extension), its route/screen, ViewModel and UiState. Each `*Module.kt` under a `di/` folder holds the Hilt bindings of its package. Everything is `internal` except what Android or Hilt must see (Application, Activity, Service, repositories used by ViewModels).

## Commands

```bash
./gradlew testDebugUnitTest          # JVM unit tests (run after every change)
./gradlew testDebugUnitTest --tests "net.biodiscus.androidcontroltv.SomeTest"
./gradlew connectedDebugAndroidTest  # instrumented tests, needs a device or emulator
./gradlew assembleDebug              # build
./gradlew lintDebug                  # lint
```

Releases: `scripts/create-release-secrets.sh` makes a keystore and one file per CI secret, `scripts/build-release-bundle.sh` builds the signed `.aab`, and `.github/workflows/release.yml` builds and publishes the signed APK on every push to `main`. The version is `VERSION` (major.minor) plus the commit count (`scripts/app-version.sh`). Never commit a keystore or the `release-secrets` folder.

Android Studio is installed as a Flatpak, which gives it a private `/tmp`. A Gradle daemon started from a normal terminal cannot see the init scripts the IDE writes there, so a later IDE sync can fail with `The specified initialization script '/tmp/ijMapper1.gradle' does not exist`. Run `./gradlew --stop` after terminal builds, or add `--no-daemon`.

## Clean Code

Clean Code is the main quality bar in this project. When in doubt, choose the more readable option.

**Write for a junior developer.** Every piece of code must be understandable by a developer with little experience who is new to this codebase. They should be able to read a method and know what it does without asking anyone.
- Choose the plain, obvious solution over the clever one. No dense one-liners, deeply chained operators, or tricks that need a second read. Use a named intermediate value or an extracted method instead.
- Use well-known patterns and the standard library before inventing abstractions. Add an abstraction only when it removes real duplication or complexity today, not for a future that may never come.
- Use domain words from the TV world (`Tv`, `RemoteKey`, `Channel`), not abbreviations or generic names like `data`, `item`, `manager`, `helper`, `util`.
- Avoid advanced language features when a simple one does the job: heavy generics, operator overloading, reflection, nested scope functions (`let`/`run`/`apply`/`also` stacked), and complex `Flow` operator chains without named steps.
- Keep nesting shallow. Use early returns and small methods instead of deep `if`/`when`/loop nesting.
- When you do use an advanced concept that the code needs (a coroutine operator, a Compose side effect, a Gradle convention plugin), isolate it in a small, well-named method so the rest of the code stays simple. Explain the *why* only if the decision is not obvious, following the comment rule below.
- Before finishing, reread your code as a newcomer would. If any line makes you pause, rename it or simplify it.

- **Names carry the meaning.** A name says exactly what the thing is or does: `isTvPaired()`, `formatVolumeAsPercentage()`, not `check()` or `process()`. If you cannot name a function precisely, it is doing too much. Split it.
- **Small methods with one purpose.** Each method has one clear job, a clear input and a clear output. Prefer pure functions: the same input gives the same output and nothing else changes. Keep side effects (I/O, state changes) at the edges, in thin methods of their own.
- **A comment is a missing method.** Before writing a comment, extract the commented block into a method whose name says what the comment would have said. Then delete the comment.
- **No dead code, no commented-out code, no TODOs left behind.**
- **Follow the Kotlin conventions:** immutable by default (`val`, `data class`, read-only collections), expression bodies for one-liners, sealed types for closed sets of states, no `!!`.
- **Keep changes minimal.** Do not refactor or reformat code that is unrelated to the task. Mention it instead.

### Comments

Write a comment only when it explains a decision that the code cannot show: why this approach, why not the obvious one, a workaround for a platform bug, a constraint from outside the code. Never write a comment that says what the code does or restates the method name. KDoc on public API is not needed unless the contract is not clear from the signature.

## Testing

A **unit** is one small method with a clear purpose, input and output. Each unit gets its own tests. Because the methods are small and pure, the tests stay short and easy to read.

- **Write the unit test together with the method.** A new or changed method is not done until its tests exist and pass. Fixing a bug starts with a failing test that reproduces it.
- **Design for testability.** If a method is hard to test, the method is the problem. Extract the logic into a small pure method and test that.
- **One behavior per test.** Test names describe the behavior in plain words, in backticks: ``fun `formats a volume of 0.5 as 50 percent`()``. The name should make a failure understandable without opening the test.
- **Arrange, act, assert.** Separate the three parts with blank lines, no comments needed. Prefer one assertion per test, or a few that check one result.
- **No mocking libraries.** Use hand-written test doubles that implement the real interface (see `testing.md`). Shared ones live in `app/src/test/java/net/biodiscus/androidcontroltv/testing/` (for example a `MainDispatcherRule` for ViewModel tests, added when the first one needs it) and move to a testing module if the project is split. Code that needs real hardware or a real network is kept thin and is not unit tested. The logic around it is, through small pure functions and fake connections.
- **Tests are deterministic.** No real network, no real clock, no sleeping. Inject a clock or dispatcher when needed and use Turbine for `Flow`.
- **Keep the tests as clean as the production code:** small helpers with good names for building test data, no copy-pasted setup.
- Unit tests go in `app/src/test/java/net/biodiscus/androidcontroltv/` (mirror the package of the class under test). Instrumented and Compose UI tests go in `app/src/androidTest/`. UI tests are for wiring, not for logic that can be a unit test.

## Workflow

1. Read the relevant skill reference.
2. Write the smallest method that does the job, with a precise name.
3. Write its unit tests.
4. Run `./gradlew testDebugUnitTest` and make sure it is green before reporting the work as done.

**Never test on a device or emulator.** The user does that. Do not run `connectedDebugAndroidTest`, `installDebug`, `adb` or anything else that needs a device. Do not run `assembleDebug` or `lintDebug` unless the user asks, because they are slow. When a change can only be verified on a real device (sockets, a real TV), say so in the report and list what the user should try.
