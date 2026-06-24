package xaos.commands.containers;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.tiles.entities.items.Container;


public final class ContainerFilterCommandHandler implements CommandHandler {

    public static final int ENABLE_ALL = 0;
    public static final int DISABLE_ALL = 1;
    public static final int ENABLE_ITEM = 2;
    public static final int DISABLE_ITEM = 3;

    private final int actionType;

    public ContainerFilterCommandHandler(int actionType) {
        this.actionType = actionType;
    }

    @Override
    public void execute(CommandContext context) {
        Container container = Game.getWorld().getContainer(
                Integer.parseInt(context.getParameter())
        );

        if (container == null) {
            return;
        }

        if (actionType == ENABLE_ALL) {
            container.enableAll(context.getParameter2());
        } else if (actionType == DISABLE_ALL) {
            container.disableAll(context.getParameter2());
        } else if (actionType == ENABLE_ITEM) {
            container.enableItem(context.getParameter2());
        } else if (actionType == DISABLE_ITEM) {
            container.disableItem(context.getParameter2());
        }
    }
}