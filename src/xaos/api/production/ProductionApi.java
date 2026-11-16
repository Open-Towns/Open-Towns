package xaos.api.production;

import xaos.api.definitions.DefinitionId;

public interface ProductionApi {

    ProductionResult queue(
            DefinitionId actionId,
            int quantity
    );

    int queued(
            DefinitionId actionId
    );

    int automatedTarget(
            DefinitionId actionId
    );

    ProductionResult setAutomatedTarget(
            DefinitionId actionId,
            int quantity
    );
}