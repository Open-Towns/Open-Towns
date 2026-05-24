# Gradle Task Reference

This document lists the Open-Towns Gradle tasks that matter for setup, development, verification, local overlays, and release packaging.

Use the Gradle wrapper from the repository root:

```powershell
.\gradlew.bat <task>
```

On Linux:

```bash
./gradlew <task>
```

## Standard Gradle Tasks

| Task | Purpose |
| --- | --- |
| `classes` | Compiles Java source and processes resources. |
| `run` | Runs `xaos.Towns` with `src/` as the working directory. |
| `test` | Runs JUnit Jupiter tests. |
| `jar` | Builds the project jar used by packaging tasks. |
| `clean` | Removes Gradle build outputs. |

## Setup Tasks

| Task | Group | Purpose |
| --- | --- | --- |
| `resolveBuildDependencies` | `setup` | Resolves compile and runtime classpaths to verify Maven dependencies can be downloaded. |
| `copyRuntimeAssets` | `setup` | Copies `graphics`, `audio`, and `fonts` from a provided or discovered Towns install into local ignored runtime folders. |
| `setupRuntimeAssets` | `setup` | Finds, copies, and verifies original Towns runtime assets for local development. |
| `applyTownsPlusPlus` | `setup` | Applies a local user-provided Towns++ checkout or extracted mod folder into the local runtime tree. |
| `setupDeveloperEnvironment` | `setup` | Runs `setupRuntimeAssets` and `resolveBuildDependencies`. |

## Verification Tasks

| Task | Group | Purpose |
| --- | --- | --- |
| `checkRuntimeAssets` | `verification` | Prints whether required local runtime files and asset folders exist. |
| `checkTownsPlusPlusSource` | `verification` | Validates a local Towns++ source folder before applying the overlay. |
| `test` | `verification` | Runs unit tests through JUnit Platform. |

## Help Tasks

| Task | Group | Purpose |
| --- | --- | --- |
| `printRuntimeInfo` | `help` | Prints the main class, Java target, run directory, LWJGL version, native classifier, and JNA version. |
| `findRuntimeAssets` | `help` | Searches common Steam locations and configured paths for a valid Towns asset source. |

## Distribution Tasks

| Task | Platform | Purpose |
| --- | --- | --- |
| `prepareWindowsAppImageInput` | Windows | Copies the jar, runtime dependencies, and INI files into `build/package/input`. |
| `createWindowsRuntimeImage` | Windows | Uses `jlink` to build a minimized Windows Java runtime image. |
| `packageWindowsAppImage` | Windows | Uses `jpackage` to build `build/package/Towns`. |
| `zipWindowsAppImage` | Windows | Archives the Windows app image to `build/release/TownsForever-<version>-windows-x64.zip`. |
| `prepareLinuxAppImageInput` | Linux | Copies the jar, runtime dependencies, and INI files into `build/package/input`. |
| `createLinuxRuntimeImage` | Linux | Uses `jlink` to build a minimized Linux Java runtime image. |
| `packageLinuxAppImage` | Linux | Uses `jpackage` to build `build/package/Towns`. |
| `zipLinuxAppImage` | Linux | Archives the Linux app image to `build/release/TownsForever-<version>-linux-x64.tar.gz`. |

Platform package tasks are guarded with `onlyIf`, so Windows packaging runs on Windows and Linux packaging runs on Linux.

## Common Workflows

### First Local Setup

```powershell
.\gradlew.bat setupDeveloperEnvironment
```

Use this after cloning when you have a local Towns install in a common Steam location.

### First Local Setup With Custom Asset Path

```powershell
.\gradlew.bat setupRuntimeAssets -Ptowns.assetSource="C:\Path\To\Towns\data"
.\gradlew.bat resolveBuildDependencies
```

`towns.assetSource` can point to either the Towns install folder or the install's `data` folder.

### Compile and Run

```powershell
.\gradlew.bat classes
.\gradlew.bat run
```

`run` uses `src/` as the working directory so legacy relative file lookups continue to work.

### Compile With Lint

```powershell
.\gradlew.bat -Ptowns.enableLint=true clean classes
```

This enables:

```text
-Xlint:deprecation
-Xlint:unchecked
-Xlint:rawtypes
```

### Test

```powershell
.\gradlew.bat test
```

Test output includes passed, skipped, and failed events with full exception details.

### Apply Towns++ Locally

```powershell
.\gradlew.bat checkTownsPlusPlusSource -PtownsPlusPlusSource="C:\Mods\Towns-plus-plus"
.\gradlew.bat applyTownsPlusPlus -PtownsPlusPlusSource="C:\Mods\Towns-plus-plus"
```

This copies local Towns++ data and config into `src/` for local playtesting. It should not be committed unless redistribution permission is granted.

### Build a Windows Release Archive

```powershell
.\gradlew.bat zipWindowsAppImage
```

Output:

```text
build/release/TownsForever-<version>-windows-x64.zip
```

### Build a Linux Release Archive

```bash
./gradlew zipLinuxAppImage
```

Output:

```text
build/release/TownsForever-<version>-linux-x64.tar.gz
```

## Gradle Properties and Environment Variables

| Name | Type | Purpose |
| --- | --- | --- |
| `towns.javaVersion` | Gradle property | Java toolchain and compiler release target. Defaults to `25`. |
| `towns.enableLint` | Gradle property | Enables extra Java compiler lint warnings when `true`. |
| `towns.assetSource` | Gradle property | Points asset setup at a Towns install folder or `data` folder. |
| `towns.installDir` | Gradle property | Alternate property for a Towns install folder. |
| `TOWNS_ASSET_SOURCE` | Environment variable | Environment equivalent for a Towns asset source. |
| `TOWNS_INSTALL_DIR` | Environment variable | Environment equivalent for a Towns install folder. |
| `townsPlusPlusSource` | Gradle property | Points Towns++ overlay tasks at a local source folder. |
| `towns.plusPlusSource` | Gradle property | Alternate property for a local Towns++ source folder. |
| `TOWNS_PLUS_PLUS_SOURCE` | Environment variable | Environment equivalent for a local Towns++ source folder. |
| `towns.packageVersion` | Gradle property | Overrides the version used in package metadata and release archive names. |

## Runtime Asset Detection

Runtime asset detection checks, in order:

- `towns.assetSource`
- `towns.installDir`
- `TOWNS_ASSET_SOURCE`
- `TOWNS_INSTALL_DIR`
- Common Steam install roots
- Steam libraries listed in `steamapps/libraryfolders.vdf`

A valid asset source must contain:

```text
graphics/
audio/
fonts/
```

If a provided path contains a `data/` folder with those three directories, Gradle normalizes the source to that `data/` folder automatically.

## Packaging Outputs

Packaging uses shared directories:

```text
build/package/input/        temporary jpackage input
build/runtime/towns-windows minimized Windows runtime image
build/runtime/towns-linux   minimized Linux runtime image
build/package/Towns/        portable app image
build/release/              release archives
```

The packaged app image includes:

- The project jar
- Runtime dependency jars
- `towns.ini`
- `graphics.ini`
- `audio.ini`
- `steam_appid.txt`
- A launch helper script (`towns.cmd` on Windows, `towns.sh` on Linux)
- Empty `data/` and `lib/native/` folders for local asset/native overlays

Original game assets are intentionally excluded from release archives.

## Troubleshooting

If dependencies cannot be resolved, run:

```powershell
.\gradlew.bat resolveBuildDependencies --refresh-dependencies
```

If runtime assets are missing, run:

```powershell
.\gradlew.bat findRuntimeAssets
.\gradlew.bat checkRuntimeAssets
```

If Gradle cannot find assets automatically, pass an explicit path:

```powershell
.\gradlew.bat setupRuntimeAssets -Ptowns.assetSource="C:\Path\To\Towns\data"
```
