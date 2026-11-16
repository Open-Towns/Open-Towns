package xaos.api.events;

public interface EventApi {

    <T extends GameEvent> EventSubscription subscribe(
            Class<T> eventType,
            EventListener<T> listener
    );
}