package xaos.api.items;

/**
 * Context supplied when an item behaviour is executed.
 */
public record ItemBehaviourContext(
        ItemView item,
        long simulationTick
) {

    public ItemBehaviourContext {
        if (item == null) {
            throw new IllegalArgumentException(
                    "Item view cannot be null"
            );
        }

        if (simulationTick < 0) {
            throw new IllegalArgumentException(
                    "Simulation tick cannot be negative"
            );
        }
    }
}