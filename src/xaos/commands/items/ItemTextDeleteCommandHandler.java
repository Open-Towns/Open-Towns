package xaos.commands.items;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.World;

public final class ItemTextDeleteCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        World.getItemsText().remove(
                Integer.valueOf(context.getParameter())
        );
    }
}