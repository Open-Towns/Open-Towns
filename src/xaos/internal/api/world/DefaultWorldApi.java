package xaos.internal.api.world;

import xaos.main.World;
import xaos.api.definitions.DefinitionId;
import xaos.tiles.Cell;
import xaos.tiles.terrain.TerrainManager;
import xaos.tiles.terrain.TerrainManagerItem;
import java.util.ArrayList;
import java.util.List;

import xaos.api.entity.EntityId;
import xaos.api.world.CellView;
import xaos.api.world.WorldApi;
import xaos.api.world.WorldPosition;
import xaos.tiles.entities.Entity;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.living.LivingEntity;


/**
 * Default implementation of the public World API using the
 * existing Towns world implementation.
 */
public final class DefaultWorldApi implements WorldApi {

    @Override
    public int width() {
        return World.MAP_WIDTH;
    }

    @Override
    public int height() {
        return World.MAP_HEIGHT;
    }

    @Override
    public int depth() {
        return World.MAP_DEPTH;
    }

    @Override
    public boolean isValidPosition(WorldPosition position) {
        if (position == null) {
            return false;
        }

        return position.x() >= 0
                && position.x() < width()
                && position.y() >= 0
                && position.y() < height()
                && position.z() >= 0
                && position.z() < depth();
    }

    @Override
    public CellView cell(WorldPosition position) {
        if (!isValidPosition(position)) {
            return null;
        }

        Cell cell = World.getCell(
                position.x(),
                position.y(),
                position.z());

        if (cell == null) {
            return null;
        }

        return mapCell(position, cell);
    }

    private static CellView mapCell(
            WorldPosition position,
            Cell cell) {
        TerrainManagerItem terrain = TerrainManager.getItemByID(
                cell.getTerrain().getTerrainID());

        DefinitionId terrainId = terrain == null
                ? null
                : DefinitionId.towns(terrain.getIniHeader());

        Entity entity = cell.getEntity();

        EntityId entityId = entity == null
                ? null
                : new EntityId(entity.getID());

        Item item = cell.getItem();

        EntityId itemId = item == null
                ? null
                : new EntityId(item.getID());

        Building building = Building.getBuilding(
                position.x(),
                position.y(),
                position.z());

        EntityId buildingId = building == null
                ? null
                : new EntityId(building.getID());

        List<EntityId> livingIds = new ArrayList<>();

        if (cell.getLivings() != null) {
            for (LivingEntity living : cell.getLivings()) {
                livingIds.add(
                        new EntityId(living.getID()));
            }
        }

        return new CellView(
                position,
                terrainId,
                entityId,
                itemId,
                buildingId,
                livingIds,
                cell.isDiscovered(),
                cell.isCave(),
                cell.isDiggable(),
                cell.isEmpty(),
                cell.hasStockPile(),
                cell.getStockPileID(),
                cell.hasZone(),
                cell.getZoneID());
    }
}