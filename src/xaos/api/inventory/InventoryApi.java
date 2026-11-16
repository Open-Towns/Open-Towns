package xaos.api.inventory;

import java.util.List;
import java.util.Optional;

import xaos.api.definitions.DefinitionId;
import xaos.api.entity.EntityId;

public interface InventoryApi {

        List<ContainerView> containers();

        ContainerView container(
                        EntityId containerId);

        default Optional<ContainerView> findContainer(
                        EntityId containerId) {
                return Optional.ofNullable(
                                container(containerId));
        }

        int count(
                        EntityId containerId,
                        DefinitionId itemDefinitionId);

        default boolean contains(
                        EntityId containerId,
                        DefinitionId itemDefinitionId) {
                return count(
                                containerId,
                                itemDefinitionId) > 0;
        }

        InventoryMutationResult consume(
                        EntityId containerId,
                        DefinitionId itemDefinitionId,
                        int quantity);

        InventoryMutationResult create(
                        EntityId containerId,
                        DefinitionId itemDefinitionId,
                        int quantity);

        /**
         * Moves one specific existing item between containers.
         *
         * The item's runtime identity is preserved.
         */
        InventoryMutationResult transferItem(
                        EntityId sourceContainerId,
                        EntityId targetContainerId,
                        EntityId itemId);

        /**
         * Moves up to quantity matching items between containers.
         *
         * Existing item instances are transferred rather than
         * destroyed/recreated.
         */
        InventoryMutationResult transfer(
                        EntityId sourceContainerId,
                        EntityId targetContainerId,
                        DefinitionId itemDefinitionId,
                        int quantity);

        InventoryReservation reserveItem(
                        EntityId sourceContainerId,
                        EntityId itemId);

        InventoryReservation reserve(
                        EntityId sourceContainerId,
                        DefinitionId itemDefinitionId);

        boolean releaseReservation(
                        InventoryReservation reservation);

        boolean isReserved(
                        EntityId itemId);

        InventoryMutationResult transferReserved(
                        InventoryReservation reservation,
                        EntityId targetContainerId);
}