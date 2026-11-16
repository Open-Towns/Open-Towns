package xaos.api.world;

/**
 * Immutable position within the game world.
 *
 * Coordinates follow the existing Towns coordinate system:
 *
 * x = horizontal map position
 * y = vertical map position
 * z = world depth / floor
 */
public record WorldPosition(int x, int y, int z) {

    /**
     * Returns a new position offset from this one.
     *
     * The returned position is not guaranteed to be inside the world.
     * Use WorldApi.isValidPosition() when bounds checking is required.
     */
    public WorldPosition offset(int xOffset, int yOffset, int zOffset) {
        return new WorldPosition(
                x + xOffset,
                y + yOffset,
                z + zOffset
        );
    }
}