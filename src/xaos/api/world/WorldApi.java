package xaos.api.world;

import java.util.ArrayList;
import java.util.List;

/**
 * Public API for querying the game world.
 *
 * This interface deliberately hides the internal World and Cell
 * implementations from mods.
 */
public interface WorldApi {

    /**
     * Returns the width of the current world in cells.
     */
    int width();

    /**
     * Returns the height of the current world in cells.
     */
    int height();

    /**
     * Returns the number of vertical levels in the current world.
     */
    int depth();

    /**
     * Returns true if the supplied position exists within the
     * bounds of the current world.
     */
    boolean isValidPosition(WorldPosition position);

    /**
     * Returns a read-only snapshot of the cell at the supplied position.
     *
     * Returns null if the position is outside the current world.
     */
    CellView cell(WorldPosition position);

    /**
     * Convenience overload for retrieving a cell directly by coordinates.
     */
    default CellView cell(int x, int y, int z) {
        return cell(new WorldPosition(x, y, z));
    }

    /**
     * Returns the cell at an offset from the supplied position.
     *
     * Returns null if the resulting position is outside the world.
     */
    default CellView relativeCell(
            WorldPosition position,
            int xOffset,
            int yOffset,
            int zOffset) {
        if (position == null) {
            return null;
        }

        return cell(position.offset(
                xOffset,
                yOffset,
                zOffset));
    }

    /**
     * Returns the horizontally adjacent cells at the same world level.
     *
     * Only valid cells are returned, so positions on the edge of the
     * map will naturally have fewer neighbours.
     */
    default List<CellView> horizontalNeighbours(WorldPosition position) {
        if (position == null) {
            return List.of();
        }

        List<CellView> neighbours = new ArrayList<>(4);

        CellView cell;

        cell = relativeCell(position, -1, 0, 0);
        if (cell != null) {
            neighbours.add(cell);
        }

        cell = relativeCell(position, 1, 0, 0);
        if (cell != null) {
            neighbours.add(cell);
        }

        cell = relativeCell(position, 0, -1, 0);
        if (cell != null) {
            neighbours.add(cell);
        }

        cell = relativeCell(position, 0, 1, 0);
        if (cell != null) {
            neighbours.add(cell);
        }

        return List.copyOf(neighbours);
    }

    /**
     * Returns all valid cells inside the rectangular area between two positions,
     * inclusive.
     *
     * The positions can be supplied in either order.
     *
     * Any part of the requested area outside the world bounds is ignored.
     */
    default List<CellView> cellsInArea(
            WorldPosition first,
            WorldPosition second) {
        if (first == null || second == null) {
            return List.of();
        }

        int minX = Math.max(0, Math.min(first.x(), second.x()));
        int maxX = Math.min(width() - 1, Math.max(first.x(), second.x()));

        int minY = Math.max(0, Math.min(first.y(), second.y()));
        int maxY = Math.min(height() - 1, Math.max(first.y(), second.y()));

        int minZ = Math.max(0, Math.min(first.z(), second.z()));
        int maxZ = Math.min(depth() - 1, Math.max(first.z(), second.z()));

        if (minX > maxX || minY > maxY || minZ > maxZ) {
            return List.of();
        }

        List<CellView> cells = new ArrayList<>();

        for (int z = minZ; z <= maxZ; z++) {
            for (int y = minY; y <= maxY; y++) {
                for (int x = minX; x <= maxX; x++) {
                    CellView cell = cell(x, y, z);

                    if (cell != null) {
                        cells.add(cell);
                    }
                }
            }
        }

        return List.copyOf(cells);
    }

    /**
     * Returns all valid cells within a horizontal radius of the supplied position.
     *
     * Only cells on the same Z level are included.
     *
     * Radius uses circular distance rather than returning the entire surrounding
     * square.
     */
    default List<CellView> cellsInRadius(
            WorldPosition centre,
            int radius) {
        if (centre == null || radius < 0) {
            return List.of();
        }

        if (!isValidPosition(centre)) {
            return List.of();
        }

        List<CellView> cells = new ArrayList<>();

        int radiusSquared = radius * radius;

        for (int yOffset = -radius; yOffset <= radius; yOffset++) {
            for (int xOffset = -radius; xOffset <= radius; xOffset++) {

                int distanceSquared = (xOffset * xOffset)
                        + (yOffset * yOffset);

                if (distanceSquared > radiusSquared) {
                    continue;
                }

                CellView cell = relativeCell(
                        centre,
                        xOffset,
                        yOffset,
                        0);

                if (cell != null) {
                    cells.add(cell);
                }
            }
        }

        return List.copyOf(cells);
    }
}