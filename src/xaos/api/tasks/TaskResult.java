package xaos.api.tasks;

public record TaskResult(
        TaskOutcome outcome,
        String message
) {

    public TaskResult {
        if (outcome == null) {
            throw new IllegalArgumentException(
                    "Task outcome cannot be null"
            );
        }
    }

    public boolean accepted() {
        return outcome != TaskOutcome.REJECTED;
    }

    public boolean isQueued() {
        return outcome == TaskOutcome.QUEUED;
    }

    public boolean isExecuted() {
        return outcome == TaskOutcome.EXECUTED;
    }

    public boolean isRejected() {
        return outcome == TaskOutcome.REJECTED;
    }

    public static TaskResult queued() {
        return new TaskResult(
                TaskOutcome.QUEUED,
                null
        );
    }

    public static TaskResult executed() {
        return new TaskResult(
                TaskOutcome.EXECUTED,
                null
        );
    }

    public static TaskResult rejected(String message) {
        return new TaskResult(
                TaskOutcome.REJECTED,
                message
        );
    }
}