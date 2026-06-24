package xaos.commands.mainmenu;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.panels.MainMenuPanel;
import xaos.platform.lwjgl3.opengl.Display;

public final class ContinueGameCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        MainMenuPanel.loadingGame = true;
        Game.getPanelMainMenu().render();
        Display.update();

        Game.continueGame(
                context.getParameter(),
                context.getParameter2());
    }
}