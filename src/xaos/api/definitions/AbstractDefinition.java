package xaos.api.definitions;

abstract class AbstractDefinition implements Definition {

    private final DefinitionId id;
    private final String legacyId;
    private final String name;

    AbstractDefinition(
            DefinitionId id,
            String name
    ) {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Definition ID cannot be null"
            );
        }

        this.id = id;
        this.legacyId = id.path();
        this.name = name;
    }

    AbstractDefinition(
            String legacyId,
            String name
    ) {
        this(
                DefinitionId.towns(legacyId),
                name
        );
    }

    @Override
    public final DefinitionId id() {
        return id;
    }

    @Override
    public final String legacyId() {
        return legacyId;
    }

    @Override
    public final String name() {
        return name;
    }
}