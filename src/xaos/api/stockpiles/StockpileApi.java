package xaos.api.stockpiles;

import java.util.List;
import java.util.Optional;

import xaos.api.definitions.DefinitionId;
import xaos.api.world.WorldPosition;

public interface StockpileApi {

    StockpileView get(
            StockpileId id
    );

    default Optional<StockpileView> find(
            StockpileId id
    ) {
        return Optional.ofNullable(
                get(id)
        );
    }

    StockpileView at(
            WorldPosition position
    );

    List<StockpileView> all();

    List<StockpileView> accepting(
            DefinitionId itemDefinitionId
    );
}