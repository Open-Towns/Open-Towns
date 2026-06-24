package xaos.commands.tasks;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.tasks.Task;
import xaos.utils.Point3D;

public final class CustomActionCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {

        Game.createTask(Task.TASK_CUSTOM_ACTION);
        Game.getCurrentTask().setTile(context.getTile(), context.getIconType());
        Game.getCurrentTask().setParameter(context.getParameter());
        Game.getCurrentTask().setParameter2(context.getParameter2());
        Point3D p3dDirect = context.getDirectPoint();
        if (p3dDirect != null) {
            Game.getCurrentTask().setPoint(p3dDirect);
            Game.getCurrentTask().setPoint(p3dDirect);
        }
    }
}
