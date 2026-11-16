package xaos.api.events;

@FunctionalInterface
public interface EventListener<T extends GameEvent> {

    void onEvent(T event);
}