package xaos.internal.api.inventory;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import xaos.api.entity.EntityId;
import xaos.api.inventory.InventoryReservation;

/**
 * Runtime registry for items temporarily reserved by
 * mod inventory operations.
 *
 * Reservations are deliberately not persisted.
 */
public final class ItemReservationRegistry {

    private static final Map<
            Integer,
            InventoryReservation> byItem =
                    new LinkedHashMap<>();

    private static final Map<
            Long,
            InventoryReservation> byReservation =
                    new LinkedHashMap<>();

    private static long nextReservationId = 1;

    private ItemReservationRegistry() {
    }

    public static synchronized InventoryReservation reserve(
            EntityId sourceContainerId,
            EntityId itemId
    ) {
        if (sourceContainerId == null
                || itemId == null) {
            return null;
        }

        if (byItem.containsKey(
                itemId.value()
        )) {
            return null;
        }

        long id =
                nextReservationId++;

        InventoryReservation reservation =
                new InventoryReservation(
                        id,
                        sourceContainerId,
                        itemId
                );

        byItem.put(
                itemId.value(),
                reservation
        );

        byReservation.put(
                id,
                reservation
        );

        return reservation;
    }

    public static synchronized boolean isReserved(
            int itemId
    ) {
        return byItem.containsKey(
                itemId
        );
    }

    public static synchronized boolean matches(
            InventoryReservation reservation
    ) {
        if (reservation == null) {
            return false;
        }

        InventoryReservation existing =
                byReservation.get(
                        reservation.reservationId()
                );

        return reservation.equals(
                existing
        );
    }

    public static synchronized boolean release(
            InventoryReservation reservation
    ) {
        if (!matches(reservation)) {
            return false;
        }

        byReservation.remove(
                reservation.reservationId()
        );

        byItem.remove(
                reservation.itemId()
                        .value()
        );

        return true;
    }

    public static synchronized Set<Integer> reservedItemIds() {
        return new LinkedHashSet<>(
                byItem.keySet()
        );
    }

    public static synchronized void clear() {
        byItem.clear();
        byReservation.clear();

       
    }
}