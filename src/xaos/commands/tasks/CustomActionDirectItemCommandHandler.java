package xaos.commands.tasks;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.tiles.entities.items.Item;
import xaos.tasks.Task;

public final class CustomActionDirectItemCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        Item item = Item.getItemByID(
                Integer.parseInt(context.getParameter2())
        );

        if (item == null) {
            return;
        }

        Game.createTask(Task.TASK_CUSTOM_ACTION);

        if (Game.getCurrentTask() == null) {
            return;
        }

        Game.getCurrentTask().setTile(
                context.getTile(),
                context.getIconType()
        );

        Game.getCurrentTask().setParameter(context.getParameter());
        Game.getCurrentTask().setParameter2(context.getParameter2());

        Game.getCurrentTask().setPoint(item.getCoordinates().toPoint3D());
        Game.getCurrentTask().setPoint(item.getCoordinates().toPoint3D());
    }
}