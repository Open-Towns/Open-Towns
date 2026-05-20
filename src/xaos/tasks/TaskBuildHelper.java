package xaos.tasks;

import java.util.ArrayList;

import xaos.main.World;
import xaos.stockpiles.Stockpile;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.buildings.BuildingManager;
import xaos.tiles.entities.buildings.BuildingManagerItem;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.tiles.entities.living.LivingEntityManager;
import xaos.tiles.entities.living.LivingEntityManagerItem;
import xaos.tiles.terrain.TerrainManager;
import xaos.tiles.terrain.TerrainManagerItem;
import xaos.utils.Log;
import xaos.utils.Messages;
import xaos.utils.Point3DShort;
import xaos.utils.Utils;
import xaos.zones.Zone;

final class TaskBuildHelper {

    private final Task task;

    TaskBuildHelper(Task task) {
        this.task = task;
    }

    void setBuildHotPoints(TaskArea area) {
        BuildingManagerItem buildingDefinition = BuildingManager.getItem(task.getParameter());
        Building building = Building.createBuilding(buildingDefinition);

        if (building == null) {
            Log.log(
                    Log.LEVEL_ERROR,
                    Messages.getString("Task.17") + task.getParameter() + "]", //$NON-NLS-1$ //$NON-NLS-2$
                    task.getClass().getName());
            return;
        }

        if (!areCellsAvailableForBuilding(buildingDefinition, area)) {
            return;
        }

        Point3DShort entrancePoint = getBuildingEntrancePoint(buildingDefinition, area);

        if (entrancePoint == null) {
            Log.log(
                    Log.LEVEL_ERROR,
                    Messages.getString("Task.31") + building.getIniHeader() + "]", //$NON-NLS-1$ //$NON-NLS-2$
                    task.getClass().toString());
            return;
        }

        task.addHotPoint(new HotPoint(entrancePoint, entrancePoint));
        task.setMaxCitizens(1);

        building.setCoordinates(area.xStart, area.yStart, area.z);

        building.setPrerequisites(buildingDefinition.getPrerequisites());
        building.setPrerequisitesLiving(buildingDefinition.getPrerequisitesFriendly());

        setupAutomaticBuilding(building, buildingDefinition, area);

        World.getCells()[area.xStart][area.yStart][area.z].setEntity(building);
        World.getBuildings().add(building);

        markBuildingCells(buildingDefinition, area);
    }

    private boolean areCellsAvailableForBuilding(BuildingManagerItem buildingDefinition, TaskArea area) {
        for (short x = area.xStart; x < area.xStart + buildingDefinition.getWidth(); x++) {
            for (short y = area.yStart; y < area.yStart + buildingDefinition.getHeight(); y++) {
                char groundDataChar = buildingDefinition.getGroundData().charAt(
                        (y - area.yStart) * buildingDefinition.getWidth() + (x - area.xStart));

                if (groundDataChar == Building.GROUND_NON_BUILDING) {
                    continue;
                }

                if (!Building.isCellAvailableForBuilding(buildingDefinition, x, y, area.z)) {
                    return false;
                }
            }
        }

        return true;
    }

    private Point3DShort getBuildingEntrancePoint(BuildingManagerItem buildingDefinition, TaskArea area) {
        for (short x = area.xStart; x < area.xStart + buildingDefinition.getWidth(); x++) {
            for (short y = area.yStart; y < area.yStart + buildingDefinition.getHeight(); y++) {
                char groundDataChar = buildingDefinition.getGroundData().charAt(
                        (y - area.yStart) * buildingDefinition.getWidth() + (x - area.xStart));

                if (groundDataChar == Building.GROUND_ENTRANCE) {
                    return Point3DShort.getPoolInstance(x, y, area.z);
                }
            }
        }

        return null;
    }

    private void setupAutomaticBuilding(Building building, BuildingManagerItem buildingDefinition, TaskArea area) {
        if (!buildingDefinition.isAutomatic()) {
            return;
        }

        String firstItemName = getFirstAutomaticBuildingItem(buildingDefinition, area);

        if (firstItemName != null) {
            building.setLastItem(firstItemName);
            building.setNonStop(true);
        }
    }

    private String getFirstAutomaticBuildingItem(BuildingManagerItem buildingDefinition, TaskArea area) {
        boolean isSpawner = buildingDefinition.getType() != null
                && buildingDefinition.getType().equalsIgnoreCase(Building.TYPE_SPAWN);

        if (isSpawner) {
            ArrayList<LivingEntityManagerItem> livingItems = LivingEntityManager
                    .getItemsByBuilding(buildingDefinition.getIniHeader());

            if (livingItems.size() > 0) {
                return livingItems.get(0).getIniHeader();
            }

            return null;
        }

        if (buildingDefinition.isMineTerrain()) {
            return getRandomMineDropForBuilding(buildingDefinition, area);
        }

        ArrayList<ItemManagerItem> items = ItemManager.getItemsByBuilding(buildingDefinition.getIniHeader());

        if (items.size() > 0) {
            return items.get(0).getIniHeader();
        }

        return null;
    }

    private String getRandomMineDropForBuilding(BuildingManagerItem buildingDefinition, TaskArea area) {
        ArrayList<String> drops = new ArrayList<String>();

        if (area.z < World.MAP_DEPTH - 1) {
            for (int x = area.xStart; x < area.xStart + buildingDefinition.getWidth(); x++) {
                for (int y = area.yStart; y < area.yStart + buildingDefinition.getHeight(); y++) {
                    TerrainManagerItem terrainDefinition = TerrainManager.getItemByID(
                            World.getCell(x, y, area.z + 1).getTerrain().getTerrainID());

                    if (terrainDefinition.getDrop() != null) {
                        drops.add(terrainDefinition.getDrop());
                    }
                }
            }
        }

        if (drops.size() == 0) {
            Log.log(Log.LEVEL_ERROR, Messages.getString("Task.33"), task.getClass().toString()); //$NON-NLS-1$
            return null;
        }

        return drops.get(Utils.getRandomBetween(0, drops.size() - 1));
    }

    private void markBuildingCells(BuildingManagerItem buildingDefinition, TaskArea area) {
        for (short x = area.xStart; x < area.xStart + buildingDefinition.getWidth(); x++) {
            for (short y = area.yStart; y < area.yStart + buildingDefinition.getHeight(); y++) {
                char groundDataChar = buildingDefinition.getGroundData().charAt(
                        (y - area.yStart) * buildingDefinition.getWidth() + (x - area.xStart));

                if (groundDataChar == Building.GROUND_NON_BUILDING) {
                    continue;
                }

                World.getCells()[x][y][area.z].setBuildingCoordinates(
                        Point3DShort.getPoolInstance(area.xStart, area.yStart, area.z));

                Stockpile.deleteStockpilePoint(x, y, area.z);
                Zone.deleteZonePoint(x, y, area.z);
            }
        }
    }
}