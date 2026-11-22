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

## Debugging on the TV

Some problems only happen on the user's real TV, and you must not test on a device. Do not guess: read `.claude/skills/android-debug-actions/SKILL.md` and write a debug action. It is a chain of steps that runs on the connected TV from the debug screen (remote screen, bug icon in the top bar) and gives the user a report to copy back to you. New actions go in `core/debug/DebugActionCatalog.kt`.

## Project structure

### What the app does

A remote control for an Android TV over ADB on the local network.

- Without a saved TV the app starts on the overview, where the user types the IP address of the TV.
- After a successful connection the address is saved (the last 10 TVs, most recent first) and the remote control opens.
- A TV that does not know the app yet shows "Allow USB debugging?" while the app connects. The first connection waits up to 60 seconds for the answer. A TV that already trusts the app shows nothing. When the user denies it, the overview shows that the TV did not allow the app.
- The overview lists the TVs that were connected before. Tapping one connects to it.
- With saved TVs the app starts on the remote control and connects to the most recent one by itself.
- The back button on the remote control disconnects and returns to the overview.
- When the ADB connection to the TV is lost, the next command reconnects to the same TV by itself and is sent again once. The remote stays on screen and only shows "Connection failed" when reconnecting fails too. A command that the TV refuses (non-zero exit) does not reconnect. Reboot and power off first check that the connection works, because a dead connection looks like a TV that shut down. The connection has a 15 second read timeout so a TV that vanished is noticed in time.
- The remote screen has a bug icon in its top bar that opens the debug tools: a list of debug actions to run on the TV. Each run gives a step by step report on screen with a copy button, and the same report is written to logcat with tag `DebugAction` (also in release builds).
- A round indicator next to the "Remote control" title shows the connection: red when not connected, green when connected. It blinks light blue for 500 ms (a new command restarts the blink) each time the TV accepted a command (a key or power action, text, an app launched, a system app info opened, an uninstall). Nothing blinks when the TV did not accept the command, because the remote then shows the failed connection. Changing a favorite or the default power action does not blink, the screen shows that change itself. Only problems get a toast (an uninstall the TV refused), and a new toast replaces the one that is still visible.
- The volume is two buttons on the remote, volume down and volume up, that each send one volume key. A slider was tried and removed: the TV sends its sound to a soundbar over HDMI ARC, and the soundbar owns the real volume, takes one step per volume key, drops keys that come too quickly, and undoes a volume that is set as an absolute level.
- The keyboard button next to the home button on the remote opens a dialog with a text field. Sending it types what the user typed on the phone into the field that has focus on the TV (`input text`, so ASCII only) and closes the dialog.
- Long pressing the power button on the remote opens a menu to reboot, put the TV to sleep or power it off. Reboot and power off end the connection, so the remote shows "Not connected" afterwards.
- Each action in that menu has a checkbox. The checked action becomes the default: a tap on the power button runs it instead of sending the plain power key. No checked action means a tap sends the power key.
- Below the remote the apps of the TV (the apps that have a TV launcher entry) are shown in a grid with their real name and icon. Tapping an app launches it. Failing to load the apps does not disconnect the remote.
- The apps are cached per TV in the app's cache folder (`core/cache`). The cached grid shows at once and is replaced by the fresh list when the TV answers. Only a list with real names and icons is cached, so the plain package-name fallback never replaces a good cached list.
- Long pressing an app tile opens a menu to add it to the favorites or remove it again, or to uninstall it (`pm uninstall`, after a confirmation). An uninstall the TV refuses (a system app) shows a toast and does not fail the connection. An uninstalled app leaves the grid and the cache at once. Favorites are shown in their own "Favorites" section above the "Apps" heading and are not repeated in the apps below. They are stored by package name for all TVs together.
- Below the apps a collapsible "System apps" entry (collapsed at first, tap the title to open or close it) lists every system app of the TV, also the ones that are in the grid above. Tapping one opens its app info page on the TV (`am start -a android.settings.APPLICATION_DETAILS_SETTINGS`). It has no long press menu. The helper prints these as `SYSTEM_APP` lines, the plain fallback uses `pm list packages -s` with package names. System apps are not cached, so they show up when the TV answers.
- The names and icons come from a helper: the app pushes its own APK to `/data/local/tmp` on the TV once and runs `TvHelperMain` (package `tvhelper`) there with `app_process`. It uses hidden Android APIs, so it can fail on some TVs. Then the grid falls back to package names with a generic icon. `TvHelperMain` runs on the TV, so it only uses Android and Kotlin classes and carries `@Keep` so the release build does not remove it.

### Packages (`app/src/main/java/net/biodiscus/androidcontroltv/`)

Each top-level package is shaped like a module of the skill. Keep the dependency direction: `feature` -> `core`, never the other way around, and never between two features.

```
core/               Pure Kotlin and data layers, one package per concern (model, repositories, ...). Depends on nothing in feature/ or ui/.
feature/overview/   Enter the TV address and connect.
feature/remote/     The remote control for the connected TV.
feature/debug/      The debug screen that runs debug actions on the TV.
ui/                 Theme, preview annotations and composables shared by features.
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
