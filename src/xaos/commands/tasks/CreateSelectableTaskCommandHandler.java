package xaos.commands.tasks;

import xaos.commands.CommandHandler;
import xaos.commands.CommandContext;

public final class CreateSelectableTaskCommandHandler implements CommandHandler {

    private final int taskType;

    public CreateSelectableTaskCommandHandler(int taskType) {
        this.taskType = taskType;
    }

    @Override
    public void execute(CommandContext context) {
        TaskCommandHelper.createSelectableTask(taskType, context);
    }
}