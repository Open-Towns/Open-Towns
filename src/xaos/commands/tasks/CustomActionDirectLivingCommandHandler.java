package xaos.commands.tasks;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.tasks.Task;
import xaos.main.World;
import xaos.tiles.entities.living.*;

public final class CustomActionDirectLivingCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        LivingEntity livingEntity = World.getLivingEntityByID(
                Integer.parseInt(context.getParameter2())
        );

        if (livingEntity == null) {
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

        Game.getCurrentTask().setPoint(livingEntity.getCoordinates().toPoint3D());
        Game.getCurrentTask().setPoint(livingEntity.getCoordinates().toPoint3D());
    }
}