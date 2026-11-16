package xaos.api.world;

import java.util.List;

import xaos.api.definitions.DefinitionId;
import xaos.api.entity.EntityId;

/**
 * Read-only snapshot of a cell in the game world.
 *
 * Runtime entities are exposed by ID rather than by leaking the
 * internal Towns Entity, Item, Building or LivingEntity classes.
 */
public record CellView(
        WorldPosition position,
        DefinitionId terrainId,
        EntityId entityId,
        EntityId itemId,
        EntityId buildingId,
        List<EntityId> livingIds,
        boolean discovered,
        boolean cave,
        boolean diggable,
        boolean empty,
        boolean hasStockpile,
        int stockpileId,
        boolean hasZone,
        int zoneId
) {

    public CellView {
        livingIds = livingIds == null
                ? List.of()
                : List.copyOf(livingIds);
    }

    /**
     * Returns true if this cell directly contains a runtime entity.
     */
    public boolean hasEntity() {
        return entityId != null;
    }

    /**
     * Returns true if this cell directly contains an item.
     */
    public boolean hasItem() {
        return itemId != null;
    }

    /**
     * Returns true if this cell is occupied by any part of a building.
     */
    public boolean hasBuilding() {
        return buildingId != null;
    }

    public boolean hasLivings() {
        return !livingIds.isEmpty();
    }

    public int livingCount() {
        return livingIds.size();
    }
}