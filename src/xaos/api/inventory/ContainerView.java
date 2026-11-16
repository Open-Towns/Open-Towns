package xaos.api.inventory;

import java.util.List;

import xaos.api.entity.EntityId;

public record ContainerView(
        EntityId containerId,
        List<EntityId> itemIds,
        boolean full
) {

    public ContainerView {
        if (containerId == null) {
            throw new IllegalArgumentException(
                    "Container ID cannot be null"
            );
        }

        itemIds = itemIds == null
                ? List.of()
                : List.copyOf(itemIds);
    }

    public boolean empty() {
        return itemIds.isEmpty();
    }

    public int itemCount() {
        return itemIds.size();
    }
}