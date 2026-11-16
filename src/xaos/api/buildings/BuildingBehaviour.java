package xaos.api.buildings;

/**
 * Behaviour executed periodically for an operative
 * building instance.
 */
@FunctionalInterface
public interface BuildingBehaviour {

    void onTick(
            BuildingView building
    );
}