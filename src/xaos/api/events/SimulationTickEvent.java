package xaos.api.events;

/**
 * Fired once after a completed Open Towns world simulation turn.
 *
 * This is a simulation event, not a render-frame event.
 *
 * It does not fire while the game is paused. The game's
 * simulation speed controls how frequently this event occurs.
 */
public record SimulationTickEvent()
        implements GameEvent {
}