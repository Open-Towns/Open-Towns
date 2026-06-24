package xaos.commands.options;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.utils.Utils;

public final class SimpleOptionCommandHandler implements CommandHandler {

    private final Runnable action;

    public SimpleOptionCommandHandler(Runnable action) {
        this.action = action;
    }

    @Override
    public void execute(CommandContext context) {
        action.run();
        Utils.saveOptions();
    }
}