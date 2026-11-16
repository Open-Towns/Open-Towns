package xaos.internal.api.mods;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import xaos.api.definitions.DefinitionId;
import xaos.main.Game;
import xaos.utils.Log;

public final class ModNamespaceResolver {

    private static final String METADATA_FILE =
            "mod.properties";

    private ModNamespaceResolver() {
    }

    public static String resolve(
            String folderName
    ) {
        if (folderName == null
                || folderName.isBlank()) {

            return DefinitionId.DEFAULT_NAMESPACE;
        }

        File modFolder =
                new File(
                        new File(
                                Game.getUserFolder(),
                                Game.MODS_FOLDER1
                        ),
                        folderName
                );

        File metadataFile =
                new File(
                        modFolder,
                        METADATA_FILE
                );

        /*
         * Legacy XML-only mods do not have a stable
         * declared ID yet, so keep their old Towns
         * namespace behaviour for compatibility.
         */
        if (!metadataFile.isFile()) {
            return DefinitionId.DEFAULT_NAMESPACE;
        }

        try (FileInputStream input =
                new FileInputStream(metadataFile)) {

            Properties properties =
                    new Properties();

            properties.load(input);

            String modId =
                    properties.getProperty("id");

            if (modId == null
                    || modId.isBlank()) {

                return DefinitionId.DEFAULT_NAMESPACE;
            }

            /*
             * Reuse DefinitionId validation.
             */
            return DefinitionId
                    .of(
                            modId,
                            "content"
                    )
                    .namespace();

        } catch (Exception exception) {

            Log.log(
                    Log.LEVEL_ERROR,
                    "Could not resolve mod namespace for ["
                            + folderName
                            + "]: "
                            + exception,
                    "ModNamespaceResolver"
            );

            return DefinitionId.DEFAULT_NAMESPACE;
        }
    }
}