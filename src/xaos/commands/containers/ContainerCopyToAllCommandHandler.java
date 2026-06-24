package xaos.commands.containers;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;

public final class ContainerCopyToAllCommandHandler implements CommandHandler {

    private final ContainerCommandService containerCommandService;

    public ContainerCopyToAllCommandHandler() {
        this.containerCommandService = new ContainerCommandService();
    }

    @Override
    public void execute(CommandContext context) {
        containerCommandService.copyToAllMatchingContainers(
                Integer.parseInt(context.getParameter())
        );
    }
}