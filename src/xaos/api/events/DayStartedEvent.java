package xaos.api.events;

public record DayStartedEvent(
        int day,
        int month,
        int year
) implements GameEvent {
}