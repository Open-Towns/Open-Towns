# Towns Forever Baby!!!!

Modernized community source release for Towns.

## Releases
- [Linux Build Release! v0.0.2](https://github.com/JustinHammitt/TownsForever/releases/tag/v0.0.2)
-- Disclaimer, I own many linux machines, not a single one is used for gaming lol. Linux releases are provided as-is no-guarantees
- [First Release! Windows x64 v0.0.1](https://github.com/JustinHammitt/TownsForever/releases/tag/v0.0.1)

### Install
1. Download and open Towns-x.x.x-platform-x64.zip
2. extract the contents of "Towns" from the archive over your steam Towns install.

Probably here
```C:\Program Files (x86)\Steam\steamapps\common\towns```


## Gradle Quick Start

### Windows

```
git clone https://github.com/JustinHammitt/TownsForever.git
cd TownsForever
.\gradlew.bat setupRuntimeAssets
.\gradlew.bat run
```

If asset setup cannot find your installed game automatically, pass the install or data folder manually:

```
.\gradlew.bat setupRuntimeAssets -Ptowns.assetSource="C:\Path\To\Towns\data"
```

### Linux

Thanks wu! It runs on Linux too, though sound may still need work.

```
git clone https://github.com/JustinHammitt/TownsForever.git
cd TownsForever
chmod +x gradlew
./gradlew setupRuntimeAssets -Ptowns.assetSource="/home/$USER/.local/share/Steam/steamapps/common/towns/"
./gradlew run
```

## Useful Gradle Tasks

```
.\gradlew.bat printRuntimeInfo
.\gradlew.bat checkRuntimeAssets
.\gradlew.bat resolveBuildDependencies
.\gradlew.bat run
.\gradlew.bat packageWindowsAppImage
.\gradlew.bat zipWindowsAppImage
```

`run` uses the local `src` folder as the working directory so the original `.ini` files and copied runtime assets are found.

`packageWindowsAppImage` builds a Steam-shaped Windows app folder at `build/package/Towns`.
`zipWindowsAppImage` archives that folder to `build/release/TownsForever-<version>-windows-x64.zip`.

On Linux, the equivalent package tasks are:

```
./gradlew packageLinuxAppImage
./gradlew zipLinuxAppImage
```

`zipLinuxAppImage` archives the Linux app image to `build/release/TownsForever-<version>-linux-x64.tar.gz`.

## Targeted Releases

Pushing a version tag builds targeted platform archives and publishes them to a GitHub Release. The first supported targets are Windows x64 and Linux x64:

```
git tag v0.1.0
git push origin v0.1.0
```

The release workflow also accepts plain numeric tags such as `0.1.0`.

GitHub may still show its automatic source-code zip and tarball, but the intended player/developer downloads are the targeted build assets:

- `TownsForever-<version>-windows-x64.zip`
- `TownsForever-<version>-linux-x64.tar.gz`

Release archives intentionally exclude original graphics, audio, fonts, and Steam DLLs. For local Steam compatibility testing, overlay the `data` and `lib` folders from a legally owned Towns install into the package root.

## Current Runtime

 - [x] JDK Version 25
 - [x] LWJGL 3.4.1
 - [x] JNA 5.18.1
 - [x] Gradle-managed LWJGL natives
 - [x] Steam native access enabled for Java 25
 - [x] GH Workflow targeted Windows and Linux release builds
 - [ ] Linux audio follow-up

[Issues? Troubleshooting](#troubleshooting)

# Towns

Repository for the [Towns game](https://store.steampowered.com/app/221020/Towns/)

This game was originally developed over a decade ago. For a while, it built a small but active community. Today, that community is much smaller, but the project still lives on in different ways.

Over the years, several people have reached out asking for access to the source code to understand how it works, modify it, or create their own versions. Until now, I had always chosen to keep it private.

I think it's time to change that.

This repository is the result of that decision.



# Repository Contents

- Full source code of the game
- Basic game data (.ini files, .xml files)

# Code License

The source code is released under the [GNU GPL v3 license](./LICENSE).

In simple terms:

- You can use, study, and modify the code
- You can redistribute it
- If you distribute a modified version, you must also release it under GPL and make the source code available

See the [LICENSE](./LICENSE) file for the full text.


# Original Assets License Notice

The original game assets, including graphics, audio, and other media, are not covered by the same license as the source code.

These assets remain the property of their respective authors and may be subject to separate usage and distribution restrictions. Therefore:

- They must NOT be included in public forks of this repository
- They must NOT be redistributed under the GPL or any other license without permission from their original authors

This repository only contains code and/or assets that are safe and legal to redistribute under its license.


# Contributions & Forks

You are welcome to fork the project, experiment, or create your own versions.

I don't guarantee reviewing pull requests or maintaining the project, but I'd genuinely enjoy seeing what comes out of it.

# Community

If you build something interesting, feel free to share it on the Discord server.

It's also the place to discuss ideas, mods, and the future of Towns with other contributors.

[Discord invite link](https://discord.gg/wAW28PkrwF)

Thanks to everyone who has been part of this project.

# Troubleshooting

Refresh and verify dependencies:

```
.\gradlew.bat resolveBuildDependencies --refresh-dependencies
.\gradlew.bat run
```

If Gradle cannot download dependencies, check DNS, VPN, firewall, or proxy access to Maven Central:

https://repo.maven.apache.org/maven2/

The current dependency set uses LWJGL 3.4.1, JNA 5.18.1, and `pngdecoder` for legacy image loading compatibility. Older LWJGL 2 native-copy troubleshooting no longer applies.

Check runtime asset setup:

```
.\gradlew.bat checkRuntimeAssets
.\gradlew.bat findRuntimeAssets
```

If the game starts but assets are missing, run `setupRuntimeAssets` with `-Ptowns.assetSource=<installed Towns data folder>`.

Example dependency failure:

```
* What went wrong:
Could not resolve all files for configuration ':runtimeClasspath'.
   > Could not resolve org.lwjgl:lwjgl:3.4.1.
     Required by:
         root project 'TownsForever'
      > Could not get resource 'https://repo.maven.apache.org/maven2/org/lwjgl/lwjgl/3.4.1/lwjgl-3.4.1.pom'.
         > No such host is known (repo.maven.apache.org)
```
