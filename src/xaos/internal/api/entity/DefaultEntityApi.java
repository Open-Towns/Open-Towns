package xaos.internal.api.entity;

import xaos.api.definitions.DefinitionId;
import xaos.api.entity.EntityApi;
import xaos.api.entity.EntityId;
import xaos.api.entity.EntityType;
import xaos.api.entity.EntityView;
import xaos.api.world.WorldPosition;
import xaos.main.World;
import xaos.tiles.entities.Entity;
import xaos.tiles.entities.buildings.Building;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.living.LivingEntity;
import xaos.internal.api.definitions.DefinitionKind;
import xaos.internal.api.definitions.DefinitionOwnershipRegistry;

/**
 * Default Entity API implementation backed by the existing
 * Towns runtime entity collections.
 */
public final class DefaultEntityApi implements EntityApi {

    @Override
    public EntityView get(EntityId id) {
        if (id == null) {
            return null;
        }

        Entity entity = findEntity(id.value());

        if (entity == null) {
            return null;
        }

        return mapEntity(entity);
    }

    /**
     * Locates an entity using the existing Towns runtime collections.
     */
    private static Entity findEntity(int id) {

        Item item = Item.getItemByID(id, true);

        if (item != null) {
            return item;
        }

        LivingEntity living = World.getLivingEntityByID(id);

        if (living != null) {
            return living;
        }

        for (Building building : World.getBuildings()) {
            if (building.getID() == id) {
                return building;
            }
        }

        return null;
    }

    /**
     * Converts an internal Towns entity into its public API snapshot.
     */
    private static EntityView mapEntity(Entity entity) {

        EntityType type = getEntityType(entity);

        DefinitionId definitionId = getDefinitionId(entity, type);

        WorldPosition position = new WorldPosition(
                entity.getX(),
                entity.getY(),
                entity.getZ());

        return new EntityView(
                new EntityId(entity.getID()),
                type,
                definitionId,
                position);
    }

    private static EntityType getEntityType(Entity entity) {
        if (entity instanceof Item) {
            return EntityType.ITEM;
        }

        if (entity instanceof Building) {
            return EntityType.BUILDING;
        }

        if (entity instanceof LivingEntity) {
            return EntityType.LIVING;
        }

        return EntityType.SPECIAL;
    }

    private static DefinitionId getDefinitionId(
            Entity entity,
            EntityType type) {
        if (type == EntityType.SPECIAL
                || type == EntityType.UNKNOWN) {
            return null;
        }

        String iniHeader = entity.getIniHeader();

        if (iniHeader == null || iniHeader.isBlank()) {
            return null;
        }

        DefinitionKind kind;

        switch (type) {

            case ITEM:
                kind = DefinitionKind.ITEM;
                break;

            case BUILDING:
                kind = DefinitionKind.BUILDING;
                break;

            case LIVING:
                kind = DefinitionKind.LIVING;
                break;

            default:
                return null;
        }

        return DefinitionOwnershipRegistry.idFor(
                kind,
                iniHeader);
    }
}