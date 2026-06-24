package xaos.commands.tasks;

import xaos.actions.ActionManager;
import xaos.actions.ActionManagerItem;
import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.panels.menus.SmartMenu;
import xaos.tiles.Tile;

public final class QueueAndPlaceCommandHandler implements CommandHandler {
    private final int taskType;
    private final boolean useGeneratedItemFallback;

    public QueueAndPlaceCommandHandler(int taskType, boolean useGeneratedItemFallback) {
        this.taskType = taskType;
        this.useGeneratedItemFallback = useGeneratedItemFallback;
    }

    @Override
    public void execute(CommandContext context) {
        Game.createTask(taskType, context.getParameter());

        if (Game.getCurrentTask() == null) {
            return;
        }

        if (context.getTile() != null) {
            Game.getCurrentTask().setTile(
                    context.getTile(),
                    context.getIconType());
            return;
        }

        if (useGeneratedItemFallback) {
            setGeneratedItemTile(context);
        }
    }

    private void setGeneratedItemTile(CommandContext context) {
        ActionManagerItem actionManagerItem = ActionManager.getItem(context.getParameter());

        if (actionManagerItem == null || actionManagerItem.getGeneratedItem() == null) {
            return;
        }

        Game.getCurrentTask().setTile(
                new Tile(actionManagerItem.getGeneratedItem()),
                SmartMenu.ICON_TYPE_ITEM);
    }
}
