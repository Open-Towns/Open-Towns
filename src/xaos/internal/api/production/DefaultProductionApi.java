package xaos.internal.api.production;

import xaos.api.definitions.DefinitionId;
import xaos.api.production.ProductionApi;
import xaos.api.production.ProductionResult;

import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.internal.api.definitions.DefinitionResolver;
import xaos.main.Game;
import xaos.tasks.Task;
import xaos.tasks.TaskManager;
import xaos.tasks.TaskTypes;

public final class DefaultProductionApi
        implements ProductionApi {

    @Override
    public ProductionResult queue(
            DefinitionId actionId,
            int quantity
    ) {
        if (quantity <= 0) {
            return new ProductionResult(
                    Math.max(quantity, 0),
                    0,
                    "Quantity must be greater than zero"
            );
        }

        String internalId =
                resolveActionId(actionId);

        if (internalId == null) {
            return new ProductionResult(
                    quantity,
                    0,
                    "Unknown or unsupported production action: "
                            + actionId
            );
        }

        TaskManager taskManager =
                Game.getWorld()
                        .getTaskManager();

        /*
         * Use exactly the same TASK_QUEUE path as the
         * normal Towns production-panel '+' button.
         */
        for (int i = 0; i < quantity; i++) {
            Task task =
                    new Task(
                            TaskTypes.TASK_QUEUE
                    );

            task.setParameter(
                    internalId
            );

            taskManager.addTask(task);
        }

        return new ProductionResult(
                quantity,
                quantity,
                null
        );
    }

    @Override
    public int queued(
            DefinitionId actionId
    ) {
        String internalId =
                resolveActionId(actionId);

        if (internalId == null) {
            return 0;
        }

        Integer count =
                Game.getWorld()
                        .getTaskManager()
                        .getItemsOnRegularQueue()
                        .get(internalId);

        return count == null
                ? 0
                : count.intValue();
    }

    @Override
    public int automatedTarget(
            DefinitionId actionId
    ) {
        String internalId =
                resolveActionId(actionId);

        if (internalId == null) {
            return 0;
        }

        return Game.getWorld()
                .getTaskManager()
                .getNumItemsOnAutomatedQueue(
                        internalId
                );
    }

    @Override
    public ProductionResult setAutomatedTarget(
            DefinitionId actionId,
            int quantity
    ) {
        if (quantity < 0) {
            return new ProductionResult(
                    0,
                    0,
                    "Automated quantity cannot be negative"
            );
        }

        String internalId =
                resolveActionId(actionId);

        if (internalId == null) {
            return new ProductionResult(
                    quantity,
                    0,
                    "Unknown or unsupported production action: "
                            + actionId
            );
        }

        TaskManager taskManager =
                Game.getWorld()
                        .getTaskManager();

        int current =
                taskManager
                        .getNumItemsOnAutomatedQueue(
                                internalId
                        );

        while (current < quantity) {
            taskManager.addItemOnAutomatedQueue(
                    internalId
            );

            current++;
        }

        while (current > quantity) {
            taskManager.removeItemOnAutomatedQueue(
                    internalId
            );

            current--;
        }

        return new ProductionResult(
                quantity,
                quantity,
                null
        );
    }

    private static String resolveActionId(
            DefinitionId actionId
    ) {
        String legacyId =
                DefinitionResolver.resolveAction(
                        actionId
                );

        if (legacyId == null) {
            return null;
        }

        ActionManagerItem action =
                ActionManager.getItem(
                        legacyId
                );

        if (action == null) {
            return null;
        }

        return action.getId();
    }
}