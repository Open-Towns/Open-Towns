package xaos.commands.options;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.utils.Messages;
import xaos.utils.Utils;

public final class ChangeLanguageCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        if (context.getDirectPoint() == null) {
            Messages.changeLanguage(
                    context.getParameter(),
                    context.getParameter2(),
                    null
            );
        } else if (Game.getModsLoaded() != null
                && Game.getModsLoaded().size() > context.getDirectPoint().x) {
            Messages.changeLanguage(
                    context.getParameter(),
                    context.getParameter2(),
                    Game.getModsLoaded().get(context.getDirectPoint().x)
            );
        } else {
            Messages.changeLanguage(
                    context.getParameter(),
                    context.getParameter2(),
                    null
            );
        }

        Utils.saveOptions();
        Game.getPanelMainMenu().createMenu();
    }
}