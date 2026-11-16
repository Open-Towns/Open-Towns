package xaos.api.mods;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import xaos.api.GameApiBootstrap;
import xaos.main.Game;
import xaos.utils.Log;
import xaos.internal.api.mods.ModDependencyResolver;
import xaos.internal.api.mods.ModSaveDataRegistry;
import xaos.api.SdkVersion;

/**
 * Loads optional Java plugins from enabled Towns mod folders.
 *
 * A Java-enabled mod contains:
 *
 * mod.properties
 * <mod jar>
 *
 * Existing XML/content-only mods require no changes.
 */
public final class JavaModLoader {

        private static final String METADATA_FILE = "mod.properties";

        private static final List<LoadedJavaMod> loadedMods = new ArrayList<>();

        private static final Set<String> worldActiveMods = new HashSet<>();

        enum DiscoveryRegistrationResult {
                ADDED,
                DUPLICATE_DETECTED,
                ALREADY_DUPLICATE
        }

        private JavaModLoader() {
        }

        /**
         * Loads Java plugins belonging to currently enabled Towns mods.
         */
        public static void loadEnabledMods() {

                List<String> enabledMods = Game.getModsLoaded();

                if (enabledMods == null
                                || enabledMods.isEmpty()) {

                        return;
                }

                Map<String, DiscoveredJavaMod> discovered = discoverEnabledJavaMods(
                                enabledMods);

                Map<String, List<String>> dependencyGraph = new HashMap<>();

                for (Map.Entry<String, DiscoveredJavaMod> entry : discovered.entrySet()) {

                        dependencyGraph.put(
                                        entry.getKey(),
                                        entry.getValue()
                                                        .dependencies());
                }

                ModDependencyResolver.Result resolution = ModDependencyResolver.resolve(
                                dependencyGraph);

                /*
                 * Report structural dependency failures such as
                 * missing dependencies and dependency cycles.
                 */
                for (String message : resolution.failures()
                                .values()) {

                        Log.log(
                                        Log.LEVEL_ERROR,
                                        message,
                                        "JavaModLoader");
                }

                /*
                 * The resolver guarantees dependencies appear
                 * before their dependants.
                 */
                for (String modId : resolution.loadOrder()) {

                        if (isModIdLoaded(
                                        modId)) {

                                continue;
                        }

                        DiscoveredJavaMod mod = discovered.get(
                                        modId);

                        /*
                         * A dependency may have been structurally valid
                         * but then failed while its JAR was actually loading.
                         *
                         * Do not load dependants in that case.
                         */
                        boolean dependenciesLoaded = true;

                        for (String dependencyId : mod.dependencies()) {

                                if (!isModIdLoaded(
                                                dependencyId)) {

                                        Log.log(
                                                        Log.LEVEL_ERROR,
                                                        "Cannot load Java mod ["
                                                                        + modId
                                                                        + "]: dependency ["
                                                                        + dependencyId
                                                                        + "] failed to load.",
                                                        "JavaModLoader");

                                        dependenciesLoaded = false;

                                        break;
                                }
                        }

                        if (!dependenciesLoaded) {
                                continue;
                        }

                        loadMod(
                                        mod.folderName());
                }

                Log.log(
                                Log.LEVEL_DEBUG,
                                "Loaded "
                                                + loadedMods.size()
                                                + " Java mod(s)",
                                "JavaModLoader");
        }

        private static Map<String, DiscoveredJavaMod> discoverEnabledJavaMods(
                        List<String> enabledMods) {

                Map<String, DiscoveredJavaMod> discovered = new HashMap<>();

                Set<String> duplicateIds = new HashSet<>();

                for (String folderName : enabledMods) {

                        File modFolder = new File(
                                        new File(
                                                        Game.getUserFolder(),
                                                        Game.MODS_FOLDER1),
                                        folderName);

                        File metadataFile = new File(
                                        modFolder,
                                        METADATA_FILE);

                        /*
                         * No mod.properties means this is an existing
                         * content/XML-only mod.
                         */
                        if (!metadataFile.isFile()) {
                                continue;
                        }

                        try {

                                Properties properties = loadProperties(
                                                metadataFile);

                                String modId = requiredProperty(
                                                properties,
                                                "id");

                                validateModId(
                                                modId);

                                List<String> dependencies = parseDependencies(
                                                properties);
                                Log.log(
                                                Log.LEVEL_DEBUG,
                                                "Discovered Java mod ["
                                                                + modId
                                                                + "] dependencies="
                                                                + dependencies,
                                                "JavaModLoader");
                                validateDependencies(
                                                modId,
                                                dependencies);

                                DiscoveryRegistrationResult registrationResult = registerDiscovered(
                                                discovered,
                                                duplicateIds,
                                                modId,
                                                new DiscoveredJavaMod(
                                                                folderName,
                                                                modFolder,
                                                                metadataFile,
                                                                modId,
                                                                dependencies));

                                if (registrationResult == DiscoveryRegistrationResult.DUPLICATE_DETECTED) {

                                        Log.log(
                                                        Log.LEVEL_ERROR,
                                                        "Duplicate Java mod ID ["
                                                                        + modId
                                                                        + "]. Mods using duplicate IDs "
                                                                        + "will not be loaded.",
                                                        "JavaModLoader");
                                }

                                discovered.put(
                                                modId,
                                                new DiscoveredJavaMod(
                                                                folderName,
                                                                modFolder,
                                                                metadataFile,
                                                                modId,
                                                                dependencies));

                        } catch (Exception exception) {

                                Log.log(
                                                Log.LEVEL_ERROR,
                                                "Could not read Java mod metadata from folder ["
                                                                + folderName
                                                                + "]: "
                                                                + exception,
                                                "JavaModLoader");
                        }
                }

                return discovered;
        }

        private static boolean isModIdLoaded(
                        String modId) {
                for (LoadedJavaMod loadedMod : loadedMods) {

                        if (loadedMod.context()
                                        .metadata()
                                        .id()
                                        .equals(modId)) {

                                return true;
                        }
                }

                return false;
        }

        private static boolean isFolderLoaded(String folderName) {
                for (LoadedJavaMod loadedMod : loadedMods) {
                        if (loadedMod.folderName().equals(folderName)) {
                                return true;
                        }
                }

                return false;
        }

        private record DiscoveredJavaMod(
                        String folderName,
                        File modFolder,
                        File metadataFile,
                        String modId,
                        List<String> dependencies) {
        }

        private static void loadMod(String folderName) {

                File modFolder = new File(
                                new File(
                                                Game.getUserFolder(),
                                                Game.MODS_FOLDER1),
                                folderName);

                File metadataFile = new File(modFolder, METADATA_FILE);

                /*
                 * No mod.properties simply means this is an existing
                 * XML/content-only mod.
                 */
                if (!metadataFile.isFile()) {
                        return;
                }

                try {
                        loadJavaMod(
                                        folderName,
                                        modFolder,
                                        metadataFile);
                } catch (Exception | LinkageError exception) {
                        Log.log(
                                        Log.LEVEL_ERROR,
                                        "Failed to load Java mod from folder ["
                                                        + folderName
                                                        + "]: "
                                                        + exception,
                                        "JavaModLoader");
                }
        }

        private static void loadJavaMod(
                        String folderName,
                        File modFolder,
                        File metadataFile) throws Exception {

                Properties properties = loadProperties(metadataFile);

                String modId = requiredProperty(properties, "id");

                String jarName = requiredProperty(properties, "jar");

                String mainClassName = requiredProperty(properties, "main");

                String modName = optionalProperty(
                                properties,
                                "name",
                                modId);

                String version = optionalProperty(
                                properties,
                                "version",
                                "unknown");

                String author = optionalProperty(
                                properties,
                                "author",
                                "unknown");

                int apiVersion = requiredIntProperty(
                                properties,
                                "apiVersion");

                validateApiVersion(
                                apiVersion);
                List<String> dependencies = parseDependencies(
                                properties);

                validateDependencies(
                                modId,
                                dependencies);
                ModMetadata metadata = new ModMetadata(
                                modId,
                                modName,
                                version,
                                author, apiVersion, dependencies);
                validateModId(modId);

                File jarFile = new File(modFolder, jarName);

                if (!jarFile.isFile()) {
                        throw new IOException(
                                        "Mod JAR does not exist: "
                                                        + jarFile.getAbsolutePath());
                }

                URL jarUrl = jarFile.toURI().toURL();

                URLClassLoader classLoader = new URLClassLoader(
                                new URL[] { jarUrl },
                                TownsMod.class.getClassLoader());

                try {
                        TownsMod mod = createModInstance(
                                        classLoader,
                                        mainClassName);

                        ModContext context = new ModContext(
                                        metadata,
                                        GameApiBootstrap.get(),
                                        new DefaultModLogger(
                                                        modId),
                                        ModSaveDataRegistry.forMod(
                                                        modId));

                        /*
                         * Let the mod initialise before considering it
                         * successfully loaded.
                         */
                        mod.onLoad(context);

                        loadedMods.add(
                                        new LoadedJavaMod(
                                                        folderName,
                                                        modId,
                                                        mod,
                                                        context,
                                                        classLoader));

                        context.logger().info(
                                        "Loaded "
                                                        + metadata.name()
                                                        + " v"
                                                        + metadata.version()
                                                        + " by "
                                                        + metadata.author());

                } catch (Exception | LinkageError exception) {
                        classLoader.close();
                        throw exception;
                }
        }

        private static Properties loadProperties(
                        File file) throws IOException {

                Properties properties = new Properties();

                try (FileInputStream input = new FileInputStream(file)) {

                        properties.load(input);
                }

                return properties;
        }

        static String requiredProperty(
                        Properties properties,
                        String key) {

                String value = properties.getProperty(key);

                if (value == null
                                || value.isBlank()) {

                        throw new IllegalArgumentException(
                                        "Missing required mod property: "
                                                        + key);
                }

                return value.trim();
        }

        static int requiredIntProperty(
                        Properties properties,
                        String key) {
                String value = requiredProperty(
                                properties,
                                key);

                try {
                        return Integer.parseInt(
                                        value);

                } catch (NumberFormatException exception) {

                        throw new IllegalArgumentException(
                                        "Invalid integer mod property ["
                                                        + key
                                                        + "]: "
                                                        + value);
                }
        }

        static void validateModId(String modId) {

                if (!modId.matches("[a-z0-9_.-]+")) {
                        throw new IllegalArgumentException(
                                        "Invalid mod ID ["
                                                        + modId
                                                        + "]. Mod IDs may contain "
                                                        + "lowercase letters, numbers, "
                                                        + "underscores, hyphens and dots.");
                }
        }

        static void validateApiVersion(
                        int apiVersion) {
                if (apiVersion > SdkVersion.API_VERSION) {

                        throw new IllegalArgumentException(
                                        "Mod requires Open Towns API version "
                                                        + apiVersion
                                                        + ", but this game provides API version "
                                                        + SdkVersion.API_VERSION);
                }

                if (apiVersion < SdkVersion.MIN_SUPPORTED_API_VERSION) {

                        throw new IllegalArgumentException(
                                        "Mod requires Open Towns API version "
                                                        + apiVersion
                                                        + ", but the oldest supported API version is "
                                                        + SdkVersion.MIN_SUPPORTED_API_VERSION);
                }
        }

        static List<String> parseDependencies(
                        Properties properties) {
                String value = optionalProperty(
                                properties,
                                "dependencies",
                                "");

                if (value.isBlank()) {
                        return List.of();
                }

                List<String> dependencies = new ArrayList<>();

                for (String part : value.split(",")) {

                        String dependencyId = part.trim();

                        if (dependencyId.isEmpty()) {
                                continue;
                        }

                        validateModId(
                                        dependencyId);

                        if (!dependencies.contains(
                                        dependencyId)) {

                                dependencies.add(
                                                dependencyId);
                        }
                }

                return List.copyOf(
                                dependencies);
        }

        static void validateDependencies(
                        String modId,
                        List<String> dependencies) {
                if (dependencies.contains(
                                modId)) {

                        throw new IllegalArgumentException(
                                        "Mod ["
                                                        + modId
                                                        + "] cannot depend on itself");
                }
        }

        private static String optionalProperty(
                        Properties properties,
                        String key,
                        String defaultValue) {

                String value = properties.getProperty(key);

                if (value == null
                                || value.isBlank()) {
                        return defaultValue;
                }

                return value.trim();
        }

        private static TownsMod createModInstance(
                        ClassLoader classLoader,
                        String mainClassName) throws Exception {

                Class<?> mainClass = Class.forName(
                                mainClassName,
                                true,
                                classLoader);

                if (!TownsMod.class
                                .isAssignableFrom(mainClass)) {

                        throw new IllegalArgumentException(
                                        "Main class ["
                                                        + mainClassName
                                                        + "] does not implement "
                                                        + TownsMod.class.getName());
                }

                Object instance = mainClass
                                .getDeclaredConstructor()
                                .newInstance();

                return (TownsMod) instance;
        }

        /**
         * Notifies all currently enabled Java mods that a playable
         * world has finished loading.
         */
        public static void notifyWorldLoaded() {

                /*
                 * A new world cannot become active while another world is
                 * still considered active.
                 *
                 * This is also a defensive fallback for any game path that
                 * changes worlds without going through exitToMainMenu().
                 */
                notifyWorldUnloaded();

                /*
                 * Pick up any Java mod that may have been enabled since
                 * the initial startup load.
                 */
                loadEnabledMods();

                for (LoadedJavaMod loadedMod : loadedMods) {

                        /*
                         * A Java mod cannot safely be unloaded from the JVM once
                         * its classes have been loaded, but if the user disables
                         * it we can stop sending lifecycle callbacks to it.
                         */
                        if (!Game.isModLoaded(loadedMod.folderName())) {
                                continue;
                        }

                        /*
                         * Mark the mod active before invoking the callback.
                         *
                         * If onWorldLoaded() partially registers resources and
                         * then fails, the next unload can still clean them up.
                         */
                        worldActiveMods.add(
                                        loadedMod.folderName());

                        try {
                                loadedMod.instance().onWorldLoaded(
                                                loadedMod.context());

                        } catch (Exception | LinkageError exception) {

                                loadedMod.context()
                                                .logger()
                                                .error(
                                                                "Error during onWorldLoaded()",
                                                                exception);
                        }
                }
        }

        /**
         * Notifies mods that the current playable world is about to be
         * unloaded, then releases all world-scoped resources.
         */
        public static void notifyWorldUnloaded() {

                for (LoadedJavaMod loadedMod : loadedMods) {

                        /*
                         * Only mods that received onWorldLoaded() for the current
                         * world should receive onWorldUnloaded().
                         */
                        if (!worldActiveMods.remove(
                                        loadedMod.folderName())) {
                                continue;
                        }

                        try {
                                loadedMod.instance().onWorldUnloaded(
                                                loadedMod.context());

                        } catch (Exception | LinkageError exception) {

                                loadedMod.context()
                                                .logger()
                                                .error(
                                                                "Error during onWorldUnloaded()",
                                                                exception);

                        } finally {

                                /*
                                 * World-scoped resources are always released,
                                 * even if the mod throws during its unload hook.
                                 */
                                loadedMod.context()
                                                .clearWorldResources();
                        }
                }
        }

        /**
         * Shuts down the Java mod loader.
         *
         * Any active world is unloaded first. Mods then receive onUnload()
         * in reverse load order before remaining resources and classloaders
         * are released.
         */
        public static void shutdown() {

                notifyWorldUnloaded();

                for (int i = loadedMods.size() - 1; i >= 0; i--) {

                        LoadedJavaMod loadedMod = loadedMods.get(i);

                        try {
                                loadedMod.instance().onUnload(
                                                loadedMod.context());

                        } catch (Exception | LinkageError exception) {

                                loadedMod.context()
                                                .logger()
                                                .error(
                                                                "Error during onUnload()",
                                                                exception);

                        } finally {

                                loadedMod.context()
                                                .clearAllResources();

                                try {
                                        loadedMod.classLoader().close();

                                } catch (IOException exception) {

                                        loadedMod.context()
                                                        .logger()
                                                        .error(
                                                                        "Error closing mod class loader",
                                                                        exception);
                                }
                        }
                }

                loadedMods.clear();
                worldActiveMods.clear();
        }

        static <T> DiscoveryRegistrationResult registerDiscovered(
                        Map<String, T> discovered,
                        Set<String> duplicateIds,
                        String modId,
                        T value) {
                if (duplicateIds.contains(modId)) {
                        return DiscoveryRegistrationResult.ALREADY_DUPLICATE;
                }

                if (discovered.containsKey(modId)) {

                        /*
                         * Neither mod is allowed to win when two enabled
                         * Java mods declare the same ID.
                         */
                        discovered.remove(modId);
                        duplicateIds.add(modId);

                        return DiscoveryRegistrationResult.DUPLICATE_DETECTED;
                }

                discovered.put(
                                modId,
                                value);

                return DiscoveryRegistrationResult.ADDED;
        }
}
