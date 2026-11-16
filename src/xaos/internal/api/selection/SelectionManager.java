package xaos.internal.api.selection;

import java.util.function.Consumer;

import xaos.api.selection.AreaSelection;
import xaos.api.world.WorldPosition;
import xaos.utils.Point3D;

public final class SelectionManager {

    private static Consumer<AreaSelection> areaCallback;

    private static WorldPosition firstPoint;

    private SelectionManager() {
    }

    public static void beginAreaSelection(
            Consumer<AreaSelection> callback
    ) {
        if (callback == null) {
            throw new IllegalArgumentException(
                    "Selection callback cannot be null"
            );
        }

        cancel();

        areaCallback = callback;
    }

    public static boolean isSelecting() {
        return areaCallback != null;
    }

    public static boolean hasFirstPoint() {
        return firstPoint != null;
    }

    public static WorldPosition getFirstPoint() {
        return firstPoint;
    }

    public static void selectPoint(Point3D point) {
        if (!isSelecting() || point == null) {
            return;
        }

        WorldPosition selected =
                new WorldPosition(
                        point.x,
                        point.y,
                        point.z
                );

        /*
         * First click.
         */
        if (firstPoint == null) {
            firstPoint = selected;
            return;
        }

        /*
         * Keep an area selection on the same Z level
         * as the first click.
         */
        WorldPosition second =
                new WorldPosition(
                        selected.x(),
                        selected.y(),
                        firstPoint.z()
                );

        AreaSelection selection =
                new AreaSelection(
                        firstPoint,
                        second
                );

        Consumer<AreaSelection> callback =
                areaCallback;

        cancel();

        callback.accept(selection);
    }

    public static void cancel() {
        areaCallback = null;
        firstPoint = null;
    }
}