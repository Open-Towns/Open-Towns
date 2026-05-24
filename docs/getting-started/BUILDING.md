# Building Open-Towns

This repository keeps the original Towns project layout: Java source, XML data, INI files, and runtime-relative resources all live under `src/`.

The Gradle build modernizes how the project compiles, runs, prepares local assets, and packages portable app images while keeping that legacy runtime layout intact.

For the command-by-command task reference, see [GRADLE_TASKS.md](GRADLE_TASKS.md).

## Requirements

- Use the checked-in Gradle wrapper.
- JDK 25 is the default Java toolchain target.
- Original Towns runtime assets are required to run the game locally.

The Java target is controlled by the `towns.javaVersion` Gradle property. The default comes from `gradle.properties` and currently resolves to Java 25.

Example override:

```powershell
.\gradlew.bat -Ptowns.javaVersion=21 clean classes
```

Gradle can auto-provision a matching Java toolchain when one is not already installed locally.

## Core Dependencies

The build resolves Java dependencies from Maven Central.

Current runtime stack:

- LWJGL 3.4.1
- LWJGL GLFW, OpenGL, OpenAL, and STB modules
- Gradle-selected LWJGL native jars for the current operating system
- JNA 5.18.1
- JUnit Jupiter 5.10.1 for tests

The project uses the LWJGL BOM so LWJGL module versions stay aligned.

## Source Layout

Gradle is configured so the main source set reads from `src/`.

```text
src/
  xaos/              Java source
  data/              XML, language files, and runtime data
  towns.ini          Main runtime configuration
  graphics.ini       Graphics runtime configuration
  audio.ini          Audio runtime configuration
```

The main Java source set excludes `src/test/**`. The main resources source set also reads from `src/`, but excludes Java files and tests.

Tests live under:

```text
src/test/java
src/test/resources
```

This layout exists because the original game loads many files through relative paths.

## Compile

Compile the project:

```powershell
.\gradlew.bat classes
```

Compile with lint warnings enabled:

```powershell
.\gradlew.bat -Ptowns.enableLint=true clean classes
```

Compilation uses UTF-8 source encoding and Gradle's `--release` option for the configured Java target.

## Run

Run the game from Gradle:

```powershell
.\gradlew.bat run
```

The `run` task uses `src/` as its working directory. This is intentional because the legacy game expects files like `towns.ini`, `graphics.ini`, `audio.ini`, and `data/actions.xml` to be available relative to the process working directory.

The `run` task also passes:

```text
--enable-native-access=ALL-UNNAMED
```

That keeps native library access available for LWJGL and JNA on modern Java versions.

## Runtime Assets

The source tree does not redistribute the original Towns graphics, audio, or font assets.

Local-only runtime assets should be copied into:

```text
src/data/graphics/
src/data/audio/
src/data/fonts/
```

These folders are ignored by git.

To let Gradle find and copy assets from an installed Towns release:

```powershell
.\gradlew.bat setupRuntimeAssets
```

If the install is somewhere custom, pass the install folder or the `data` folder:

```powershell
.\gradlew.bat setupRuntimeAssets -Ptowns.assetSource="C:\Path\To\Towns\data"
```

The asset finder also checks these environment variables:

```text
TOWNS_ASSET_SOURCE
TOWNS_INSTALL_DIR
```

## First-Time Developer Setup

For a new local checkout, the usual setup command is:

```powershell
.\gradlew.bat setupDeveloperEnvironment
```

This runs:

```text
setupRuntimeAssets
resolveBuildDependencies
```

Use it when you want Gradle to copy local runtime assets and preflight Maven dependency resolution in one step.

## Towns++ Local Overlay

Towns++ compatibility is provided through a local overlay task. The repository does not commit or redistribute Towns++ content.

Point Gradle at a local checkout or extracted copy:

```powershell
.\gradlew.bat checkTownsPlusPlusSource -PtownsPlusPlusSource="C:\Mods\Towns-plus-plus"
.\gradlew.bat applyTownsPlusPlus -PtownsPlusPlusSource="C:\Mods\Towns-plus-plus"
```

The source folder should contain:

```text
data/
graphics.ini
towns.ini
```

`applyTownsPlusPlus` copies those files into the local `src/` runtime tree. That intentionally changes local runtime files for playtesting. Do not commit copied Towns++ data or assets unless redistribution permission is granted.

The task also accepts:

```text
-Ptowns.plusPlusSource=<path>
TOWNS_PLUS_PLUS_SOURCE=<path>
```

## Packaging

The build has platform-specific packaging tasks for Windows and Linux.

Windows:

```powershell
.\gradlew.bat packageWindowsAppImage
.\gradlew.bat zipWindowsAppImage
```

Linux:

```bash
./gradlew packageLinuxAppImage
./gradlew zipLinuxAppImage
```

Packaging uses:

- `jar` to build the project jar
- `jlink` to create a minimized Java runtime image
- `jpackage` to create a portable app image

Windows package output:

```text
build/package/Towns/
build/release/TownsForever-<version>-windows-x64.zip
```

Linux package output:

```text
build/package/Towns/
build/release/TownsForever-<version>-linux-x64.tar.gz
```

The package version defaults to the project version without `-SNAPSHOT`. Override it with:

```powershell
.\gradlew.bat -Ptowns.packageVersion=0.1.0 zipWindowsAppImage
```

Release archives intentionally do not include original graphics, audio, fonts, or Steam DLLs. For Steam compatibility testing, overlay legally owned Towns runtime assets into the packaged app folder.

## Useful Diagnostics

Print the current build/runtime configuration:

```powershell
.\gradlew.bat printRuntimeInfo
```

Check whether local runtime assets are present:

```powershell
.\gradlew.bat checkRuntimeAssets
```

Search common Steam locations for a Towns install:

```powershell
.\gradlew.bat findRuntimeAssets
```

Preflight dependency downloads:

```powershell
.\gradlew.bat resolveBuildDependencies
```

If dependency resolution fails with `No such host is known (repo.maven.apache.org)`, Gradle cannot reach Maven Central. Check internet, DNS, VPN, firewall, or proxy settings, then rerun the command.

## Mod Loading Notes

See [MODDING.md](../modding/MODDING.md) for the current source-level map of mod loading and future tooling ideas.

The game creates a user folder at:

```text
<user home>/.towns/
```

Mods live under:

```text
<user home>/.towns/mods/<mod name>/
```

Many managers load base XML first, then load files from active mods. A mod can usually provide matching files under paths such as:

```text
<user home>/.towns/mods/<mod name>/data/items.xml
<user home>/.towns/mods/<mod name>/data/actions.xml
<user home>/.towns/mods/<mod name>/data/menu.xml
```

The active mod list is stored in the `MODS` property in `towns.ini`, and the main menu has a Mods screen for toggling folders it finds there.
