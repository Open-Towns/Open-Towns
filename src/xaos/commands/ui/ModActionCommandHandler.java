package xaos.commands.ui;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.internal.api.ui.UiActionRegistry;

public final class ModActionCommandHandler
        implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        String actionId = context.getParameter();

        if (actionId == null) {
            return;
        }

        UiActionRegistry.execute(actionId);
    }
}