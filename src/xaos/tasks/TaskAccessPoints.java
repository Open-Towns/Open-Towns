package xaos.tasks;

import java.util.ArrayList;

import xaos.main.World;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;

final class TaskAccessPoints {

    private TaskAccessPoints() {
    }

    static ArrayList<Point3DShort> getAccessingPoints(int x, int y, int z, int task) {
        ArrayList<Point3DShort> accessiblePoints = new ArrayList<Point3DShort>();

        addAdjacentPointsOnLevel(accessiblePoints, x, y, z);

        if (z < World.MAP_DEPTH - 1) {
            accessiblePoints.add(Point3DShort.getPoolInstance(x, y, z + 1));
        }

        if (z > 0 && World.getCell(x, y, z - 1).isMined()) {
            addAdjacentPointsOnLevel(accessiblePoints, x, y, z - 1);
        }

        if (z < World.MAP_DEPTH - 1 && World.getCell(x, y, z + 1).isMined()) {
            addAdjacentPointsOnLevel(accessiblePoints, x, y, z + 1);
        }

        if (z > 0) {
            accessiblePoints.add(Point3DShort.getPoolInstance(x, y, z - 1));
        }

        return accessiblePoints;
    }

    static ArrayList<Point3DShort> getAccessingPointsMatchingAstarZone(
            int x,
            int y,
            int z,
            int astarZoneId,
            int task) {

        ArrayList<Point3DShort> accessiblePoints = getAccessingPoints(x, y, z, task);
        ArrayList<Point3DShort> matchingPoints = new ArrayList<Point3DShort>();

        for (int pointIndex = 0; pointIndex < accessiblePoints.size(); pointIndex++) {
            Point3DShort point = accessiblePoints.get(pointIndex);

            if (World.getCell(point).getAstarZoneID() == astarZoneId) {
                matchingPoints.add(point);
            }
        }

        return matchingPoints;
    }

    static ArrayList<Point3DShort> getAccessingPointsMatchingAstarZone(Point3DShort point, int astarZoneId, int task) {
        return getAccessingPointsMatchingAstarZone(point.x, point.y, point.z, astarZoneId, task);
    }

    static ArrayList<Point3DShort> getAccessingPointsMatchingAstarZone(Point3D point, int astarZoneId, int task) {
        return getAccessingPointsMatchingAstarZone(point.x, point.y, point.z, astarZoneId, task);
    }

    private static void addAdjacentPointsOnLevel(ArrayList<Point3DShort> accessiblePoints, int x, int y, int z) {
        if (x > 0) {
            addPointIfInsideMap(accessiblePoints, x - 1, y - 1, z);
            addPointIfInsideMap(accessiblePoints, x - 1, y, z);
            addPointIfInsideMap(accessiblePoints, x - 1, y + 1, z);
        }

        if (x < World.MAP_WIDTH - 1) {
            addPointIfInsideMap(accessiblePoints, x + 1, y - 1, z);
            addPointIfInsideMap(accessiblePoints, x + 1, y, z);
            addPointIfInsideMap(accessiblePoints, x + 1, y + 1, z);
        }

        addPointIfInsideMap(accessiblePoints, x, y - 1, z);
        addPointIfInsideMap(accessiblePoints, x, y + 1, z);
    }

    private static void addPointIfInsideMap(ArrayList<Point3DShort> accessiblePoints, int x, int y, int z) {
        if (!Utils.isInsideMap(x, y, z)) {
            return;
        }

        accessiblePoints.add(Point3DShort.getPoolInstance(x, y, z));
    }
}