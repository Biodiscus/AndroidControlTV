---
name: android-debug-actions
description: Set up debug actions for AndroidControlTV, chains of steps that run on the connected TV and report what happened. Use when something works wrong on the user's TV or device and cannot be reproduced in a unit test, when the user says it fails on their TV, asks to investigate or debug behaviour on the device, or asks for a debug action.
---

# Debug actions

Some things only go wrong on the user's real TV: ADB commands, the app helper, launching apps, differences between TV models. CLAUDE.md forbids testing on a device or emulator, so **do not guess and do not run adb yourself**. Write a debug action, ask the user to run it, and read the report they paste back.

A debug action is a named list of steps. The steps run on the TV in order. Each step can look at the output of the command before it. The first failing step stops the chain and the steps after it are marked `SKIPPED`.

## What exists

| File | What it is |
|---|---|
| `core/debug/DebugStep.kt` | The step types (below) |
| `core/debug/DebugAction.kt` | `DebugAction(name, description, steps)` |
| `core/debug/DebugActionCatalog.kt` | **The list of actions the debug screen shows. New actions go here.** |
| `core/debug/DebugActionRunner.kt` | Runs the steps, turns every failure into a failed step |
| `core/debug/DebugReport.kt` | The result, and `toText()` which is what the user copies |
| `feature/debug/` | The debug screen: one card per action with a Run button, the last report with a Copy button |

The user opens it from the remote screen: bug icon in the top bar, then Debug tools. The report is also written to logcat with tag `DebugAction`.

## Steps

| Step | Does | Fails when |
|---|---|---|
| `RunCommand(command, title = command)` | Runs a shell command on the TV, records its output | The command exits with an error or the connection breaks |
| `ExpectOutputContains(text)` | Checks the output of the **most recent** `RunCommand` | The text is not in that output |
| `PushFile(localFile, remotePath)` | Sends a file from the phone to the TV | The push fails |
| `PauseFor(milliseconds)` | Waits, for example for a TV to settle | Never |

A command that is expected to be able to fail is not a step type. Wrap it in the shell so it always exits with 0, like `[ -f 'x' ] && echo yes || echo no`, then check the output with `ExpectOutputContains`.

## Workflow

1. **Understand the symptom.** Read the code path that the user's problem goes through. Write down the hypotheses. Each hypothesis should become one probe: a command whose output proves or kills it.
2. **Look at the catalog first.** Extend or reuse an existing action when it already covers the area. Do not add a second action for the same thing.
3. **Add the action** to `DebugActionCatalog` as a `private fun createXxx() = DebugAction(...)` and put it in `actions`. Build the steps with a plain `listOf(...)`. There is no DSL on purpose, a newcomer must be able to read it.
4. **Write the tests** (see below).
5. **Hand it over.** Tell the user exactly what to do: build and run the app, connect to the TV, open the remote screen, tap the bug icon, press Run on "<action name>", press Copy report, paste it here. Say what you expect to see in the report and what it will tell you.
6. **Read the report.** The first `FAILED` step and its output is the lead. `SKIPPED` steps never ran, so they say nothing. Form the next hypothesis and, if needed, extend the action and ask for another run. Never say a problem is fixed until a report or the user confirms it.
7. **Clean up.** When the problem is solved, remove actions that were only about that one bug. Keep actions that are generally useful (connection check, TV info). Update the catalog test if needed.

## Rules for good actions

- **Read-only first.** Start with commands that only look: `getprop`, `id`, `ls`, `cat`, `pm path`, `dumpsys` (with a filter). Order the steps from least to most invasive.
- **Never do anything destructive** without the user asking for it: no `reboot`, `rm` outside our own helper files in `/data/local/tmp`, `pm uninstall`, `pm clear`, `settings put`, `am force-stop`.
- **Use the production constants and functions** (`LIST_LAUNCHABLE_APPS_COMMAND`, `createRunHelperCommand(...)`, `createLaunchAppCommand(...)`) instead of copying the command text. The probe must test the real thing, and it keeps working when the command changes.
- **One purpose per step, with a title that says what the step tells you.** Give `title` when the command alone is not clear.
- **Put each `ExpectOutputContains` right behind the command it checks.** It only looks at the most recent `RunCommand`.
- **Keep the output small.** A report shows at most 2000 characters per step. Filter in the shell: `| head -n 20`, `| grep -i name`, `| cut -c1-120`. Never print the base64 icons of the app helper in full.
- **No secrets.** Never print or push the ADB key (`adbkey`), tokens or personal data. The report gets pasted into a chat.
- **A debug action never reconnects.** It runs on the connection that is open now, and a broken connection fails the step. That is on purpose: the report must show what the TV did, not what a retry hid. Normal remote actions do reconnect by themselves.
- **Commands run as the shell user** (uid 2000) through `sh`. Quote paths with single quotes. Many `dumpsys` and `cmd` calls differ between Android versions, so check the Android version first when it matters.

## Example

Problem: "Netflix does not launch from the grid on my TV."

(`netflixApp` stands for the `TvApp` of Netflix, here `TvApp("com.netflix.ninja", activityName, "Netflix")`.)

```kotlin
private fun createNetflixLaunchCheck() = DebugAction(
    name = "Netflix launch",
    description = "Checks that Netflix is installed, has a TV launcher activity and can be started.",
    steps = listOf(
        DebugStep.RunCommand("pm path com.netflix.ninja", title = "Is Netflix installed"),
        DebugStep.ExpectOutputContains("package:"),
        DebugStep.RunCommand("$LIST_LAUNCHABLE_APPS_COMMAND | grep netflix", title = "Does it have a TV launcher activity"),
        DebugStep.ExpectOutputContains("com.netflix.ninja/"),
        DebugStep.RunCommand(createLaunchAppCommand(netflixApp), title = "Start it"),
    ),
)
```

A report looks like this, and this is what you paste back and read:

```
Debug action: Netflix launch
Result: FAILED
1. [PASSED] Is Netflix installed
   package:/product/app/Netflix/Netflix.apk
2. [PASSED] Expect the output to contain "package:"
3. [FAILED] Does it have a TV launcher activity
   TV command failed (exit code 1):
4. [SKIPPED] Expect the output to contain "com.netflix.ninja/"
5. [SKIPPED] Start it
```

Reading it: installed, but there is no leanback launcher activity, so the grid should not list it and a launch with that activity cannot work.

## Tests

- `DebugActionCatalogTest` already checks that every action has a name, a description, steps with titles, and a unique name. It runs for new actions without changes.
- Add a catalog test when the order of steps matters, like the app helper pushing before it runs.
- A new step type needs three things: the case in `DebugStep`, the case in `DebugActionRunner.runStep` (the compiler forces the `when`), and tests in `DebugActionRunnerTest`. Test the passing case, the failing case and that later steps are skipped.
- Run `./gradlew testDebugUnitTest` and make sure it is green before handing the action over.
- Follow CLAUDE.md for the rest: hand-written fakes (`FakeTvShell` has `respondTo` and `failOn` per command), small methods, no comments that restate the code.
