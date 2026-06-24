package xaos.commands.containers;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.panels.UI.UIPanelState;

public final class ContainerManageCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        int containerId = Integer.parseInt(context.getParameter());

        UIPanelState.setPilePanelActive(containerId, true);
    }
}