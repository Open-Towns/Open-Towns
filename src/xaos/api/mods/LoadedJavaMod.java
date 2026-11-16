package xaos.api.mods;

import java.net.URLClassLoader;

import xaos.api.mods.ModContext;
import xaos.api.mods.TownsMod;

/**
 * Internal record of a successfully loaded Java mod.
 */
record LoadedJavaMod(
        String folderName,
        String id,
        TownsMod instance,
        ModContext context,
        URLClassLoader classLoader
) {
}