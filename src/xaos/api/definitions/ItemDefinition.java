package xaos.api.definitions;

import java.util.List;

/** Read-only snapshot of an item definition. */
public final class ItemDefinition
        extends AbstractDefinition {

    private final String type;
    private final int level;
    private final String building;
    private final int buildingTime;
    private final int value;
    private final List<String> tags;
    private final List<String> actions;
    private final boolean wall;
    private final boolean door;
    private final boolean container;
    private final boolean stackable;
    private final boolean militaryItem;

    /*
     * Legacy constructor.
     *
     * Existing Towns definitions continue to default
     * to the "towns" namespace.
     */
     ItemDefinition(
            String legacyId,
            String name,
            String type,
            int level,
            String building,
            int buildingTime,
            int value,
            List<String> tags,
            List<String> actions,
            boolean wall,
            boolean door,
            boolean container,
            boolean stackable,
            boolean militaryItem
    ) {
        this(
                DefinitionId.towns(legacyId),
                name,
                type,
                level,
                building,
                buildingTime,
                value,
                tags,
                actions,
                wall,
                door,
                container,
                stackable,
                militaryItem
        );
    }

    /*
     * Namespaced constructor.
     */
     ItemDefinition(
            DefinitionId id,
            String name,
            String type,
            int level,
            String building,
            int buildingTime,
            int value,
            List<String> tags,
            List<String> actions,
            boolean wall,
            boolean door,
            boolean container,
            boolean stackable,
            boolean militaryItem
    ) {
        super(id, name);

        this.type = type;
        this.level = level;
        this.building = building;
        this.buildingTime = buildingTime;
        this.value = value;
        this.tags = copy(tags);
        this.actions = copy(actions);
        this.wall = wall;
        this.door = door;
        this.container = container;
        this.stackable = stackable;
        this.militaryItem = militaryItem;
    }

    public String type() {
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

    public int value() {
        return value;
    }

    public List<String> tags() {
        return tags;
    }

    public List<String> actions() {
        return actions;
    }

    public boolean wall() {
        return wall;
    }

    public boolean door() {
        return door;
    }

    public boolean container() {
        return container;
    }

    public boolean stackable() {
        return stackable;
    }

    public boolean militaryItem() {
        return militaryItem;
    }

    private static List<String> copy(
            List<String> values
    ) {
        return values == null
                ? List.of()
                : List.copyOf(values);
    }
}