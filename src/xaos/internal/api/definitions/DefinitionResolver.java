package xaos.internal.api.definitions;

import xaos.api.definitions.DefinitionId;

public final class DefinitionResolver {

    private DefinitionResolver() {
    }

    public static String resolve(
            DefinitionKind kind,
            DefinitionId id
    ) {
        if (kind == null || id == null) {
            return null;
        }

        DefinitionId actual =
                DefinitionOwnershipRegistry.idFor(
                        kind,
                        id.path()
                );

        if (!actual.equals(id)) {
            return null;
        }

        return id.path();
    }

    public static String resolveItem(
            DefinitionId id
    ) {
        return resolve(
                DefinitionKind.ITEM,
                id
        );
    }

    public static String resolveAction(
            DefinitionId id
    ) {
        return resolve(
                DefinitionKind.ACTION,
                id
        );
    }

    public static String resolveBuilding(
            DefinitionId id
    ) {
        return resolve(
                DefinitionKind.BUILDING,
                id
        );
    }
}