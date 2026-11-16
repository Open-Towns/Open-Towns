package xaos.internal.api.mods;

import java.io.IOException;
import java.io.InputStream;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import xaos.api.mods.ModSaveData;
import xaos.utils.Log;

/**
 * Holds persistent state for Java mods for the currently
 * loaded world/save.
 *
 * Mod data is stored separately from Towns' normal world
 * serialization so mods do not alter the core save format.
 */
public final class ModSaveDataRegistry {

    private static final String ZIP_FOLDER =
            "moddata/";

    private static final String ZIP_EXTENSION =
            ".properties";

    private static final Map<
            String,
            ModSaveDataStore> stores =
                    new LinkedHashMap<>();

    private ModSaveDataRegistry() {
    }

    /**
     * Returns the persistent storage belonging to a mod.
     *
     * The same backing store is retained for the lifetime
     * of the application so existing ModContext instances
     * remain valid when another save is loaded.
     */
    public static synchronized ModSaveData forMod(
            String modId
    ) {
        validateModId(modId);

        ModSaveDataStore store =
                stores.computeIfAbsent(
                        modId,
                        ignored ->
                                new ModSaveDataStore()
                );

        return new DefaultModSaveData(
                store
        );
    }

    /**
     * Clears all per-world values while retaining the
     * backing store objects.
     *
     * This is important because loaded mods already hold
     * references to these stores through their ModContext.
     */
    public static synchronized void clear() {

        for (ModSaveDataStore store
                : stores.values()) {

            store.clear();
        }
    }

    /**
     * Writes all non-empty mod save stores into the
     * current Towns save ZIP.
     *
     * Each mod receives its own entry:
     *
     * moddata/<mod-id>.properties
     */
    public static synchronized void writeTo(
            ZipOutputStream output
    ) throws IOException {

        if (output == null) {
            throw new IllegalArgumentException(
                    "ZIP output cannot be null"
            );
        }

        for (Map.Entry<
                String,
                ModSaveDataStore> entry
                : stores.entrySet()) {

            String modId =
                    entry.getKey();

            Map<String, String> values =
                    entry.getValue()
                            .snapshot();

            /*
             * Don't create empty files for mods that
             * currently have no world-specific state.
             */
            if (values.isEmpty()) {
                continue;
            }

            ZipEntry zipEntry =
                    new ZipEntry(
                            ZIP_FOLDER
                                    + modId
                                    + ZIP_EXTENSION
                    );

            output.putNextEntry(
                    zipEntry
            );

            Properties properties =
                    new Properties();

            for (Map.Entry<String, String>
                    valueEntry
                    : values.entrySet()) {

                properties.setProperty(
                        valueEntry.getKey(),
                        valueEntry.getValue()
                );
            }

            /*
             * Properties.store does not close the supplied
             * stream, so the ZipOutputStream remains usable.
             */
            properties.store(
                    output,
                    "Open Towns mod save data"
            );

            output.closeEntry();
        }
    }

    /**
     * Loads all mod save-data entries from a Towns save.
     *
     * Old saves simply contain no moddata entries, which
     * leaves all stores empty.
     *
     * Data belonging to currently disabled mods is also
     * retained so disabling and later re-enabling a mod
     * does not automatically destroy its save state.
     */
    public static synchronized void readFrom(
            ZipFile zipFile
    ) throws IOException {

        if (zipFile == null) {
            throw new IllegalArgumentException(
                    "ZIP file cannot be null"
            );
        }

        /*
         * We are entering a different world/save.
         *
         * Clear previous values but retain the actual
         * store instances already referenced by mods.
         */
        clear();

        Enumeration<? extends ZipEntry> entries =
                zipFile.entries();

        while (entries.hasMoreElements()) {

            ZipEntry entry =
                    entries.nextElement();

            if (entry.isDirectory()) {
                continue;
            }

            String modId =
                    getModId(
                            entry.getName()
                    );

            if (modId == null) {
                continue;
            }

            Properties properties =
                    new Properties();

            try (InputStream input =
                    zipFile.getInputStream(
                            entry
                    )) {

                properties.load(
                        input
                );

            } catch (IOException
                    | IllegalArgumentException exception) {

                /*
                 * Corrupt mod data should not make the
                 * entire Towns save unloadable.
                 */
                Log.log(
                        Log.LEVEL_ERROR,
                        "Unable to load save data for mod ["
                                + modId
                                + "]: "
                                + exception.getMessage(),
                        "ModSaveDataRegistry"
                );

                continue;
            }

            Map<String, String> values =
                    new LinkedHashMap<>();

            for (String key
                    : properties.stringPropertyNames()) {

                values.put(
                        key,
                        properties.getProperty(
                                key
                        )
                );
            }

            /*
             * Reuse the existing store if the mod is
             * already loaded.
             *
             * This preserves the reference held by
             * ModContext.saveData().
             */
            ModSaveDataStore store =
                    stores.computeIfAbsent(
                            modId,
                            ignored ->
                                    new ModSaveDataStore()
                    );

            store.replace(
                    values
            );
        }
    }

    /**
     * Returns a snapshot primarily for debugging/testing.
     */
    static synchronized Map<
            String,
            Map<String, String>> snapshot() {

        Map<
                String,
                Map<String, String>> result =
                        new LinkedHashMap<>();

        for (Map.Entry<
                String,
                ModSaveDataStore> entry
                : stores.entrySet()) {

            Map<String, String> values =
                    entry.getValue()
                            .snapshot();

            if (!values.isEmpty()) {
                result.put(
                        entry.getKey(),
                        values
                );
            }
        }

        return result;
    }

    private static String getModId(
            String entryName
    ) {
        if (entryName == null
                || !entryName.startsWith(
                        ZIP_FOLDER
                )
                || !entryName.endsWith(
                        ZIP_EXTENSION
                )) {

            return null;
        }

        String modId =
                entryName.substring(
                        ZIP_FOLDER.length(),
                        entryName.length()
                                - ZIP_EXTENSION.length()
                );

        /*
         * Only allow a file directly underneath moddata/.
         */
        if (!modId.matches(
                "[a-z0-9_.-]+"
        )) {
            return null;
        }

        return modId;
    }

    private static void validateModId(
            String modId
    ) {
        if (modId == null
                || !modId.matches(
                        "[a-z0-9_.-]+"
                )) {

            throw new IllegalArgumentException(
                    "Invalid mod ID: "
                            + modId
            );
        }
    }
}