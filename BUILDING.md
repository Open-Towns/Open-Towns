# Building TownsEX

This repository keeps the original released Towns layout: Java source, XML data, and INI files all live under `src/`.

## Requirements

- JDK 25 is the current default project baseline.
- Gradle 9.x.

Java 25 is the current LTS line as of 2026. Java 26 is a non-LTS feature release, so it is not the best long-lived baseline for this project. The Gradle wrapper is on a Java-25-capable version, and the build target is controlled by `towns.javaVersion` in `gradle.properties`.

The build pulls the legacy game libraries from public Maven repositories:

- LWJGL 2.9.3
- JNA 4.1.0
- TWL PNGDecoder 1.0
- Slick Util 1.0.0

The legacy runtime dependencies are older than the new project baseline. Their class files range from Java 1.4 through Java 6 bytecode, and they run on modern JVMs through backwards compatibility. LWJGL 2 remains the main modernization risk because it depends on old native libraries and APIs.

## Compile

```powershell
gradle classes
```

The Java source files have been normalized to UTF-8, and the Gradle build compiles them with UTF-8 explicitly.

To test a different Java target, override the project Java version:

```powershell
gradle "-Ptowns.javaVersion=21" clean classes
```

Gradle can auto-provision a matching JDK toolchain when one is not already installed locally.

## Run

```powershell
gradle run
```

The `run` task uses `src/` as the working directory because the original code loads files such as `towns.ini`, `graphics.ini`, and `data/actions.xml` using relative paths.

The build also extracts LWJGL 2 native libraries into `build/natives/lwjgl` and passes that directory to the JVM.

The original runtime graphics, audio, and font folders are not committed to this source repository. If those folders have been copied locally from an installed Towns release, `gradle run` can launch the game.

## Check Runtime Assets

```powershell
gradle checkRuntimeAssets
```

This prints which expected runtime files/folders are present.

## Runtime Assets

The source tree includes code and XML/INI data, but the original release assets should remain local-only and ignored by git:

- `src/data/graphics/`
- `src/data/audio/`
- `src/data/fonts/`

Copy those folders from an installed Towns release when you want to run the game locally.

## Mod Loading Notes

The game creates a user folder at:

```text
<user home>/.towns/
```

Mods live under:

```text
<user home>/.towns/mods/<mod name>/
```

Many managers load the base XML first, then load files from active mods. A mod can usually provide matching files under paths such as:

```text
<user home>/.towns/mods/<mod name>/data/items.xml
<user home>/.towns/mods/<mod name>/data/actions.xml
<user home>/.towns/mods/<mod name>/data/menu.xml
```

The active mod list is stored in the `MODS` property in `towns.ini`, and the main menu also has a Mods screen for toggling folders it finds there.
