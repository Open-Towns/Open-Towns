package xaos.api.inventory;

public record InventoryMutationResult(
        int requested,
        int changed,
        String message
) {

    public InventoryMutationResult {
        if (requested < 0) {
            throw new IllegalArgumentException(
                    "Requested quantity cannot be negative"
            );
        }

        if (changed < 0 || changed > requested) {
            throw new IllegalArgumentException(
                    "Changed quantity is invalid"
            );
        }
    }

    public boolean complete() {
        return changed == requested;
    }

    public boolean partial() {
        return changed > 0 && changed < requested;
    }

    public boolean changedAnything() {
        return changed > 0;
    }
}