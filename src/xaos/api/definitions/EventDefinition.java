package xaos.api.definitions;

import java.util.List;

/** Read-only snapshot of an event definition. */
public final class EventDefinition extends AbstractDefinition {

    private final int order;
    private final int minPopulation;
    private final boolean spawnAtRandom;
    private final String turns;
    private final String eventCooldown;
    private final boolean siege;
    private final List<String> prerequisites;
    private final List<String> afterEvents;
    private final List<String> effects;
    private final List<String> items;

    /**
     * Legacy constructor.
     *
     * Unqualified IDs are treated as base Towns events.
     */
     EventDefinition(
            String legacyId,
            String name,
            int order,
            int minPopulation,
            boolean spawnAtRandom,
            String turns,
            String eventCooldown,
            boolean siege,
            List<String> prerequisites,
            List<String> afterEvents,
            List<String> effects,
            List<String> items) {

        this(
                DefinitionId.towns(
                        legacyId),
                name,
                order,
                minPopulation,
                spawnAtRandom,
                turns,
                eventCooldown,
                siege,
                prerequisites,
                afterEvents,
                effects,
                items);
    }

    /**
     * Namespaced constructor.
     */
     EventDefinition(
            DefinitionId id,
            String name,
            int order,
            int minPopulation,
            boolean spawnAtRandom,
            String turns,
            String eventCooldown,
            boolean siege,
            List<String> prerequisites,
            List<String> afterEvents,
            List<String> effects,
            List<String> items) {

        super(
                id,
                name);

        this.order = order;
        this.minPopulation = minPopulation;
        this.spawnAtRandom = spawnAtRandom;
        this.turns = turns;
        this.eventCooldown = eventCooldown;
        this.siege = siege;

        this.prerequisites = copy(prerequisites);

        this.afterEvents = copy(afterEvents);

        this.effects = copy(effects);

        this.items = copy(items);
    }

    public int order() {
        return order;
    }

    public int minPopulation() {
        return minPopulation;
    }

    public boolean spawnAtRandom() {
        return spawnAtRandom;
    }

    public String turns() {
        return turns;
    }

    public String eventCooldown() {
        return eventCooldown;
    }

    public boolean siege() {
        return siege;
    }

    public List<String> prerequisites() {
        return prerequisites;
    }

    public List<String> afterEvents() {
        return afterEvents;
    }

    public List<String> effects() {
        return effects;
    }

    public List<String> items() {
        return items;
    }

    private static List<String> copy(
            List<String> values) {
        return values == null
                ? List.of()
                : List.copyOf(values);
    }
}