package xaos.api.definitions;

/**
 * Common read-only metadata shared by content definitions exposed to mods and
 * tooling.
 */
public interface Definition {

    DefinitionId id();

    /**
     * Original Towns/XML identifier used by the current runtime managers.
     */
    String legacyId();

    String name();
}
