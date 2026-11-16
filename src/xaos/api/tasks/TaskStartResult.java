package xaos.api.tasks;

/**
 * Result of starting an interactive task such as
 * item placement.
 */
public record TaskStartResult(
        boolean started,
        String message
) {

    public static TaskStartResult success() {
        return new TaskStartResult(
                true,
                null
        );
    }

    public static TaskStartResult rejected(
            String message
    ) {
        return new TaskStartResult(
                false,
                message
        );
    }
}