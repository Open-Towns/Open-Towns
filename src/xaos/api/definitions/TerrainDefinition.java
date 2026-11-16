package xaos.api.definitions;

import java.util.List;

/**
 * Read-only snapshot of a terrain definition.
 */
public final class TerrainDefinition extends AbstractDefinition {

    private final int terrainId;
    private final int mineTurns;
    private final String drop;
    private final int dropPercent;
    private final String ladderItem;
    private final boolean canBeFilled;
    private final String group;
    private final boolean blocky;
    private final List<String> actions;

    /**
     * Legacy constructor.
     *
     * Unqualified IDs are treated as base Towns
     * terrain definitions.
     */
     TerrainDefinition(
            String legacyId,
            String name,
            int terrainId,
            int mineTurns,
            String drop,
            int dropPercent,
            String ladderItem,
            boolean canBeFilled,
            String group,
            boolean blocky,
            List<String> actions) {

        this(
                DefinitionId.towns(
                        legacyId
                ),
                name,
                terrainId,
                mineTurns,
                drop,
                dropPercent,
                ladderItem,
                canBeFilled,
                group,
                blocky,
                actions
        );
    }

    /**
     * Namespaced constructor.
     */
     TerrainDefinition(
            DefinitionId id,
            String name,
            int terrainId,
            int mineTurns,
            String drop,
            int dropPercent,
            String ladderItem,
            boolean canBeFilled,
            String group,
            boolean blocky,
            List<String> actions) {

        super(
                id,
                name
        );

        this.terrainId = terrainId;
        this.mineTurns = mineTurns;
        this.drop = drop;
        this.dropPercent = dropPercent;
        this.ladderItem = ladderItem;
        this.canBeFilled = canBeFilled;
        this.group = group;
        this.blocky = blocky;

        this.actions =
                actions == null
                        ? List.of()
                        : List.copyOf(actions);
    }

    public int terrainId() {
        return terrainId;
    }

    public int mineTurns() {
        return mineTurns;
    }

    public String drop() {
        return drop;
    }

    public int dropPercent() {
        return dropPercent;
    }

    public String ladderItem() {
        return ladderItem;
    }

    public boolean canBeFilled() {
        return canBeFilled;
    }

    public String group() {
        return group;
    }

    public boolean blocky() {
        return blocky;
    }

    public List<String> actions() {
        return actions;
    }
}