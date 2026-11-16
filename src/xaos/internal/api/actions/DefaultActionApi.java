package xaos.internal.api.actions;

import xaos.actions.Action;
import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.actions.QueueData;


import xaos.api.GameApiBootstrap;
import xaos.api.actions.ActionApi;
import xaos.api.actions.ActionResult;
import xaos.api.definitions.DefinitionId;
import xaos.api.world.WorldPosition;

import xaos.internal.api.definitions.DefinitionResolver;

import xaos.main.Game;
import xaos.utils.Point3DShort;

public final class DefaultActionApi
        implements ActionApi {

    @Override
    public ActionResult queueAt(
            DefinitionId actionId,
            WorldPosition position
    ) {
        if (actionId == null) {
            return ActionResult.rejected(
                    "Action definition cannot be null"
            );
        }

        if (position == null) {
            return ActionResult.rejected(
                    "Position cannot be null"
            );
        }

       if (!GameApiBootstrap.get()
        .world()
        .isValidPosition(position)) {

            return ActionResult.rejected(
                    "Position is outside the world"
            );
        }

        String legacyId =
                DefinitionResolver.resolveAction(
                        actionId
                );

        if (legacyId == null) {
            return ActionResult.rejected(
                    "Unknown action: "
                            + actionId
            );
        }

        ActionManagerItem definition =
                ActionManager.getItem(
                        legacyId
                );

        if (definition == null) {
            return ActionResult.rejected(
                    "Unknown action: "
                            + actionId
            );
        }

        Action action =
                new Action(
                        definition.getId()
                );

        action.setTerrainPoint(
                Point3DShort.getPoolInstance(
                        position.x(),
                        position.y(),
                        position.z()
                )
        );

        action.setQueue(
                definition.getQueue()
        );

        action.setQueueData(
                new QueueData()
        );

        Game.getWorld()
                .getTaskManager()
                .addCustomAction(
                        action,
                        true
                );

        return ActionResult.success();
    }
}