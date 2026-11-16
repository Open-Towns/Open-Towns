package xaos.api.inventory;

import xaos.api.entity.EntityId;

/**
 * Temporary reservation of a specific runtime item.
 *
 * Reservations are world-runtime state only and are not
 * persisted into save files.
 */
public record InventoryReservation(
        long reservationId,
        EntityId sourceContainerId,
        EntityId itemId
) {

    public InventoryReservation {
        if (reservationId <= 0) {
            throw new IllegalArgumentException(
                    "Reservation ID must be positive"
            );
        }

        if (sourceContainerId == null) {
            throw new IllegalArgumentException(
                    "Source container ID cannot be null"
            );
        }

        if (itemId == null) {
            throw new IllegalArgumentException(
                    "Item ID cannot be null"
            );
        }
    }
}