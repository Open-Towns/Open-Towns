package xaos.api.selection;

import xaos.api.world.WorldPosition;

public record AreaSelection(
        WorldPosition first,
        WorldPosition second
) {

    public AreaSelection {
        if (first == null || second == null) {
            throw new IllegalArgumentException(
                    "Selection positions cannot be null"
            );
        }

        if (first.z() != second.z()) {
            throw new IllegalArgumentException(
                    "Area selection must be on one Z level"
            );
        }
    }

    public int minX() {
        return Math.min(first.x(), second.x());
    }

    public int maxX() {
        return Math.max(first.x(), second.x());
    }

    public int minY() {
        return Math.min(first.y(), second.y());
    }

    public int maxY() {
        return Math.max(first.y(), second.y());
    }

    public int z() {
        return first.z();
    }

    public int width() {
        return maxX() - minX() + 1;
    }

    public int height() {
        return maxY() - minY() + 1;
    }

    public WorldPosition origin() {
        return new WorldPosition(
                minX(),
                minY(),
                z()
        );
    }
}