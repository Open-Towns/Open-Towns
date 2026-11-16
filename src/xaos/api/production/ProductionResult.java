package xaos.api.production;

public record ProductionResult(
        int requested,
        int accepted,
        String message
) {

    public ProductionResult {
        if (requested < 0) {
            throw new IllegalArgumentException(
                    "Requested quantity cannot be negative"
            );
        }

        if (accepted < 0 || accepted > requested) {
            throw new IllegalArgumentException(
                    "Accepted quantity is invalid"
            );
        }
    }

    public boolean complete() {
        return accepted == requested;
    }

    public boolean acceptedAnything() {
        return accepted > 0;
    }
}