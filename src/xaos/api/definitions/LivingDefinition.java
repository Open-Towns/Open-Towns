package xaos.api.definitions;

import java.util.List;

/** Read-only snapshot of a living-entity definition. */
public final class LivingDefinition extends AbstractDefinition {

    private final int type;
    private final int level;
    private final String building;
    private final int buildingTime;
    private final boolean female;
    private final List<String> actions;
    private final List<String> effects;

    /**
     * Legacy constructor.
     *
     * Unqualified IDs are treated as Towns definitions.
     */
     LivingDefinition(
            String legacyId,
            String name,
            int type,
            int level,
            String building,
            int buildingTime,
            boolean female,
            List<String> actions,
            List<String> effects) {

        this(
                DefinitionId.towns(
                        legacyId
                ),
                name,
                type,
                level,
                building,
                buildingTime,
                female,
                actions,
                effects
        );
    }

    /**
     * Namespaced constructor.
     */
    LivingDefinition(
            DefinitionId id,
            String name,
            int type,
            int level,
            String building,
            int buildingTime,
            boolean female,
            List<String> actions,
            List<String> effects) {

        super(
                id,
                name
        );

        this.type = type;
        this.level = level;
        this.building = building;
        this.buildingTime = buildingTime;
        this.female = female;
        this.actions = copy(actions);
        this.effects = copy(effects);
    }

    public int type() {
        return type;
    }

    public int level() {
        return level;
    }

    public String building() {
        return building;
    }

    public int buildingTime() {
        return buildingTime;
    }

    public boolean female() {
        return female;
    }

    public List<String> actions() {
        return actions;
    }

    public List<String> effects() {
        return effects;
    }

    private static List<String> copy(
            List<String> values
    ) {
        return values == null
                ? List.of()
                : List.copyOf(values);
    }
}