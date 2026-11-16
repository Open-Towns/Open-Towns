package xaos.api.stockpiles;

import java.util.List;

import xaos.api.definitions.DefinitionId;
import xaos.api.entity.EntityId;
import xaos.api.world.WorldPosition;

public record StockpileView(
        StockpileId id,
        String typeId,
        List<WorldPosition> positions,
        List<EntityId> itemIds,
        List<DefinitionId> allowedItemIds,
        int filledPoints,
        boolean lockedToCopy
) {

    public StockpileView {
        positions = positions == null
                ? List.of()
                : List.copyOf(positions);

        itemIds = itemIds == null
                ? List.of()
                : List.copyOf(itemIds);

        allowedItemIds = allowedItemIds == null
                ? List.of()
                : List.copyOf(allowedItemIds);
    }

    public int capacity() {
        return positions.size();
    }

    public int freePoints() {
        return Math.max(
                0,
                capacity() - filledPoints
        );
    }

    public boolean empty() {
        return filledPoints == 0;
    }

    public boolean full() {
        return filledPoints >= capacity();
    }

    public boolean allows(DefinitionId itemId) {
        return allowedItemIds.contains(itemId);
    }
}