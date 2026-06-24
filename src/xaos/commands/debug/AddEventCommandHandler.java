package xaos.commands.debug;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.events.EventManager;
import xaos.events.EventManagerItem;
import xaos.main.Game;
import xaos.TownsProperties;

public final class AddEventCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        if (!TownsProperties.TEST_COMMANDS) {
            return;
        }

        EventManagerItem eventDefinition = EventManager.getItem(
                context.getParameter());

        if (eventDefinition == null) {
            return;
        }

        Game.getWorld().addEvent(eventDefinition);
    }
}