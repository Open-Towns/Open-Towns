package xaos.commands.tasks;

import xaos.commands.CommandContext;
import xaos.main.Game;
import xaos.tasks.Task;
import xaos.utils.Point3D;

public final class TaskCommandHelper {

    private TaskCommandHelper() {
    }

    public static void createSelectableTask(int taskType, CommandContext context) {
        Game.createTask(taskType);

        Task task = Game.getCurrentTask();
        task.setTile(context.getTile(), context.getIconType());

        if (context.hasDirectPoint()) {
            setDirectTaskPoint(task, context.getDirectPoint());
        }
    }

    public static void createSelectableTaskWithParameter(int taskType, CommandContext context) {
        Game.createTask(taskType, context.getParameter());

        Task task = Game.getCurrentTask();
        task.setTile(context.getTile(), context.getIconType());
    }

    public static void addImmediateTask(int taskType, CommandContext context) {
        Task task = new Task(taskType);
        task.setTile(context.getTile(), context.getIconType());
        task.setParameter(context.getParameter());
        task.setParameter2(context.getParameter2());
        task.setPointIni(context.getDirectPoint());

        Game.getWorld().getTaskManager().addTask(task);
    }

    private static void setDirectTaskPoint(Task task, Point3D point) {
        task.setPoint(point);
        task.setPoint(point);
    }
    
}