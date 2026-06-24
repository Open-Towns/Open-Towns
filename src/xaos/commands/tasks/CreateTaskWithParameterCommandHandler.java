package xaos.commands.tasks;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;


public final class CreateTaskWithParameterCommandHandler implements CommandHandler {

    private final int taskType;

    public CreateTaskWithParameterCommandHandler(int taskType) {
        this.taskType = taskType;
    }

    @Override
    public void execute(CommandContext context) {
        Game.createTask(taskType, context.getParameter());

        if (Game.getCurrentTask() == null) {
            return;
        }

        Game.getCurrentTask().setTile(
                context.getTile(),
                context.getIconType()
        );
    }
}