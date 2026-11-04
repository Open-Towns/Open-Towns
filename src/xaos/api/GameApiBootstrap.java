package xaos.api;

import xaos.internal.api.actions.DefaultActionApi;
import xaos.internal.api.buildings.DefaultBuildingApi;
import xaos.internal.api.buildings.DefaultBuildingBehaviourApi;
import xaos.internal.api.definitions.DefaultDefinitionsApi;
import xaos.internal.api.entity.DefaultEntityApi;
import xaos.internal.api.events.DefaultEventApi;
import xaos.internal.api.fluids.DefaultFluidApi;
import xaos.internal.api.inventory.DefaultInventoryApi;
import xaos.internal.api.items.DefaultItemBehaviourApi;
import xaos.internal.api.items.DefaultItemQueryApi;
import xaos.internal.api.production.DefaultProductionApi;
import xaos.internal.api.selection.DefaultSelectionApi;
import xaos.internal.api.stockpiles.DefaultStockpileApi;
import xaos.internal.api.tasks.DefaultTaskApi;
import xaos.internal.api.ui.DefaultUiApi;
import xaos.internal.api.world.DefaultWorldApi;

/**
 * Runtime bootstrap for the public GameApi.
 *
 * This class is part of the Open Towns runtime and is deliberately
 * excluded from the public SDK JAR.
 */
public final class GameApiBootstrap {

    private static final GameApi INSTANCE =
            new GameApi(
                    new DefaultDefinitionsApi(),
                    new DefaultWorldApi(),
                    new DefaultEntityApi(),
                    new DefaultUiApi(),
                    new DefaultTaskApi(),
                    new DefaultSelectionApi(),
                    new DefaultEventApi(),
                    new DefaultInventoryApi(),
                    new DefaultStockpileApi(),
                    new DefaultItemQueryApi(),
                    new DefaultItemBehaviourApi(),
                    new DefaultBuildingApi(),
                    new DefaultBuildingBehaviourApi(),
                    new DefaultFluidApi(),
                    new DefaultProductionApi(),
                    new DefaultActionApi()
            );

    private GameApiBootstrap() {
    }

    public static GameApi get() {
        GameApi.install(INSTANCE);
        return INSTANCE;
    }
}