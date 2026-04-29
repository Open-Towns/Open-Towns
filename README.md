# Towns Forever Baby!!

Quick start Instructions:

Windows

```
git clone https://github.com/JustinHammitt/TownsEX.git
cd TownsEX
.\gradlew.bat setupRuntimeAssets
# if this fails you'll have to extract the assets from your game manually
# TODO: Asset extraction steps
.\gradlew.bat run
```

Linux, Thanks wu!
Tuesday, April 21, 2026 5:48 PM wu: its running on linux as well - but no sound at the moment

```

git clone https://github.com/JustinHammitt/TownsEX.git
cd TownsEX
chmod +x gradlew
./gradlew setupRuntimeAssets -Ptowns.assetSource="/home/$USER/.local/share/Steam/steamapps/common/towns/"
./gradlew run

```

[Issues? Troubleshooting](#Troubleshooting)

Short term goals:
 - [x] JDK Version 25
 - [x] lwjglVersion 3.4.1
 - [x] jnaVersion 5.18.1
 - [ ] TBD

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

rebuild your dependencies.
```
.\gradlew.bat resolveBuildDependencies --refresh-dependencies
.\gradlew.bat run
```

This mean's gradle was unable to conneect to the repo and fetch a dependancy(lwjgl:2.9.3).
Check your VPN, firewall, whaterver. 
If you can reach your dependency in your browser you should be able to build.
https://repo.maven.apache.org/maven2/org/lwjgl/lwjgl/lwjgl-platform/2.9.3/
```
* What went wrong:
Could not determine the dependencies of task ':copyLwjglNatives'.
> Could not resolve all files for configuration ':lwjglNatives'.
   > Could not resolve org.lwjgl.lwjgl:lwjgl-platform:2.9.3.
     Required by:
         root project 'TownsEX'
      > Could not resolve org.lwjgl.lwjgl:lwjgl-platform:2.9.3.
         > Could not get resource 'https://repo.maven.apache.org/maven2/org/lwjgl/lwjgl/lwjgl-platform/2.9.3/lwjgl-platform-2.9.3.pom'.
            > Could not GET 'https://repo.maven.apache.org/maven2/org/lwjgl/lwjgl/lwjgl-platform/2.9.3/lwjgl-platform-2.9.3.pom'.
               > No such host is known (repo.maven.apache.org)
```


