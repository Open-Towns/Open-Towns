package xaos.api.mods;

import java.util.List;

/**
 * Basic descriptive information about a loaded mod.
 */
public record ModMetadata(
       String id,
        String name,
        String version,
        String author,
        int apiVersion,
         List<String> dependencies
) {

    public ModMetadata {

         dependencies = dependencies == null
                ? List.of()
                : List.copyOf(dependencies);
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Mod ID cannot be empty"
            );
        }

        if (name == null || name.isBlank()) {
            name = id;
        }

        if (version == null || version.isBlank()) {
            version = "unknown";
        }

        if (author == null || author.isBlank()) {
            author = "unknown";
        }
    }
}