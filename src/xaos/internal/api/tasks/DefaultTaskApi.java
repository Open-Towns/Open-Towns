package xaos.internal.api.tasks;


import xaos.api.GameApiBootstrap;
import xaos.api.definitions.DefinitionId;
import xaos.api.tasks.TaskApi;
import xaos.api.tasks.TaskResult;
import xaos.api.world.WorldPosition;
import xaos.internal.api.definitions.DefinitionResolver;
import xaos.main.Game;
import xaos.tasks.Task;
import xaos.tasks.TaskTypes;
import xaos.utils.Point3D;
import xaos.main.World;
import xaos.tiles.Cell;
import xaos.tiles.entities.buildings.Building;
import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.api.tasks.TaskStartResult;
import xaos.internal.api.definitions.DefinitionResolver;
import xaos.panels.menus.SmartMenu;
import xaos.tiles.Tile;

public final class DefaultTaskApi implements TaskApi {

        @Override
        public TaskResult dig(WorldPosition position) {
                if (position == null) {
                        return TaskResult.rejected(
                                        "Position cannot be null");
                }

                if (!GameApiBootstrap.get()
                                .world()
                                .isValidPosition(position)) {

                        return TaskResult.rejected(
                                        "Position is outside the world");
                }

                Task task = new Task(TaskTypes.TASK_DIG);

                task.setPointIni(
                                new Point3D(
                                                position.x(),
                                                position.y(),
                                                position.z()));

                /*
                 * This is the same preparation used by the normal
                 * Task.finishCreation() pathway.
                 *
                 * It creates the task's hot points and applies the
                 * normal Towns validation rules.
                 */
                task.prepareForQueue();

                /*
                 * No hot points means Towns found nothing valid to dig.
                 *
                 * Avoid adding a dead/finished task to TaskManager.
                 */
                if (task.getHotPoints().isEmpty()) {
                        return TaskResult.rejected(
                                        "No valid dig target at this position");
                }

                /*
                 * Match normal God Mode behaviour.
                 */
                if (Game.isGodMode()
                                && task.canExecuteImmediately()
                                && task.executeImmediately()) {

                        return TaskResult.executed();
                }

                /*
                 * Normal game behaviour.
                 *
                 * Game.taskCreated() also performs the existing
                 * DIG -> MINE conversion before adding the task
                 * to TaskManager.
                 */
                Game.taskCreated(task);

                return TaskResult.queued();
        }

        @Override
        public TaskResult destroy(WorldPosition position) {
                if (position == null) {
                        return TaskResult.rejected(
                                        "Position cannot be null");
                }

                if (!GameApiBootstrap.get()
                                .world()
                                .isValidPosition(position)) {

                        return TaskResult.rejected(
                                        "Position is outside the world");
                }

                Point3D point = new Point3D(
                                position.x(),
                                position.y(),
                                position.z());

                Task task;

                /*
                 * Buildings need their own destruction path because Towns
                 * has to clear the entire building footprint and associated
                 * building state.
                 */
                if (Building.getBuilding(point) != null) {

                        task = new Task(
                                        TaskTypes.TASK_DESTROY_BUILDING);

                } else {

                        Cell cell = World.getCell(point);

                        if (cell == null || !cell.hasEntity()) {
                                return TaskResult.rejected(
                                                "There is nothing destroyable at this position");
                        }

                        task = new Task(
                                        TaskTypes.TASK_DESTROY_ENTITY);
                }

                task.setPointIni(point);

                /*
                 * Match the game's immediate execution behaviour
                 * when God Mode is active.
                 */
                if (Game.isGodMode()
                                && task.canExecuteImmediately()
                                && task.executeImmediately()) {

                        return TaskResult.executed();
                }

                /*
                 * Destroy tasks are direct TaskManager operations rather
                 * than citizen-work tasks.
                 */
                Game.getWorld()
                                .getTaskManager()
                                .addTask(task);

                return TaskResult.queued();
        }

        @Override
        public TaskResult build(
                        DefinitionId buildingId,
                        WorldPosition position) {

                if (buildingId == null) {
                        return TaskResult.rejected(
                                        "Building definition cannot be null");
                }

                if (position == null) {
                        return TaskResult.rejected(
                                        "Position cannot be null");
                }

                if (!GameApiBootstrap.get()
                                .world()
                                .isValidPosition(position)) {

                        return TaskResult.rejected(
                                        "Position is outside the world");
                }

                String legacyId = DefinitionResolver.resolveBuilding(
                                buildingId);

                if (legacyId == null) {
                        return TaskResult.rejected(
                                        "Unknown building definition: "
                                                        + buildingId);
                }

                Task task = new Task(
                                TaskTypes.TASK_BUILD);

                task.setParameter(
                                legacyId);

                task.setPointIni(
                                new Point3D(
                                                position.x(),
                                                position.y(),
                                                position.z()));

                task.prepareForQueue();

                if (task.getHotPoints().isEmpty()
                                || task.isFinished()) {

                        return TaskResult.rejected(
                                        "Building cannot be placed at this position");
                }

                if (Game.isGodMode()
                                && task.canExecuteImmediately()
                                && task.executeImmediately()) {

                        return TaskResult.executed();
                }

                Game.taskCreated(task);

                return TaskResult.queued();
        }

        @Override
        public TaskResult mine(WorldPosition position) {
                return mineArea(position, position);
        }

        @Override
        public TaskResult mineArea(
                        WorldPosition first,
                        WorldPosition second) {
                if (first == null || second == null) {
                        return TaskResult.rejected(
                                        "Mining positions cannot be null");
                }

                if (!GameApiBootstrap.get()
                                .world()
                                .isValidPosition(first)
                                || !GameApiBootstrap.get()
                                                .world()
                                                .isValidPosition(second)) {

                        return TaskResult.rejected(
                                        "Mining area is outside the world");
                }

                /*
                 * Towns mining areas operate on one Z level.
                 */
                if (first.z() != second.z()) {
                        return TaskResult.rejected(
                                        "Mining area must be on one Z level");
                }

                Task task = new Task(TaskTypes.TASK_MINE);

                task.setPointIni(
                                new Point3D(
                                                first.x(),
                                                first.y(),
                                                first.z()));

                task.setPointEnd(
                                new Point3D(
                                                second.x(),
                                                second.y(),
                                                second.z()));

                task.prepareForQueue();

                if (task.getHotPoints().isEmpty()) {
                        return TaskResult.rejected(
                                        "No valid mining targets in this area");
                }

                if (Game.isGodMode()
                                && task.canExecuteImmediately()
                                && task.executeImmediately()) {

                        return TaskResult.executed();
                }

                Game.taskCreated(task);

                return TaskResult.queued();
        }

        @Override
        public TaskStartResult beginQueueAndPlace(
                        DefinitionId actionId) {
                if (actionId == null) {
                        return TaskStartResult.rejected(
                                        "Action definition cannot be null");
                }

                String legacyActionId = DefinitionResolver.resolveAction(
                                actionId);

                if (legacyActionId == null) {
                        return TaskStartResult.rejected(
                                        "Unknown action definition: "
                                                        + actionId);
                }

                ActionManagerItem action = ActionManager.getItem(
                                legacyActionId);

                if (action == null) {
                        return TaskStartResult.rejected(
                                        "Action was not found: "
                                                        + actionId);
                }

                String generatedItem = action.getGeneratedItem();

                if (generatedItem == null
                                || generatedItem.isBlank()) {

                        return TaskStartResult.rejected(
                                        "Action does not create a placeable item: "
                                                        + actionId);
                }

                /*
                 * This is the same task type used by the native
                 * QUEUEANDPLACE command.
                 *
                 * Task.setParameter() will also inspect the action
                 * queue and populate parameter2 with the item that
                 * will eventually be created.
                 */
                Game.createTask(
                                TaskTypes.TASK_QUEUE_AND_PLACE,
                                legacyActionId);

                Task task = Game.getCurrentTask();

                if (task == null) {
                        return TaskStartResult.rejected(
                                        "Could not start item placement");
                }

                /*
                 * Match QueueAndPlaceCommandHandler's generated-item
                 * preview behaviour.
                 */
                task.setTile(
                                new Tile(
                                                generatedItem),
                                SmartMenu.ICON_TYPE_ITEM);

              return TaskStartResult.success();
        }
}