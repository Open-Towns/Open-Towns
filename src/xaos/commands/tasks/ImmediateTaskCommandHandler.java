package xaos.commands.tasks;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.tasks.Task;
import xaos.utils.Point3D;

public final class ImmediateTaskCommandHandler implements CommandHandler {

    private final int taskType;

    public ImmediateTaskCommandHandler(
            int taskType

    ) {
        this.taskType = taskType;

    }

    @Override
    public void execute(CommandContext context) {
        Task task = new Task(taskType);

        task.setTile(context.getTile(), context.getIconType());
        Point3D point = context.getDirectPoint();
        String param1 = context.getParameter();
        String param2 = context.getParameter2();
        if (point != null) {
            task.setPointIni(point);
        }

        if (param1 != null) {
            task.setParameter(param1);
        }

        if (param2 != null) {
            task.setParameter2(param2);
        }

        Game.getWorld().getTaskManager().addTask(task);
    }
}