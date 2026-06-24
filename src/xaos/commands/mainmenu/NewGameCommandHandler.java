package xaos.commands.mainmenu;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.panels.CommandPanel;
import xaos.panels.MainMenuPanel;
import xaos.platform.lwjgl3.opengl.Display;
import xaos.utils.Utils;

public final class NewGameCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        ArrayList<String> paths = Utils.getPathToFile(
                "save.zip",
                context.getParameter(),
                context.getParameter2());

        if (paths.size() > 0) {
            CommandPanel.executeCommand(
                    CommandPanel.COMMAND_MM_CONTINUEGAME,
                    "save.zip",
                    context.getParameter() + "," + context.getParameter2() + "," + paths.get(0),
                    null,
                    null,
                    0);
            return;
        }

        MainMenuPanel.loadingGame = true;
        Game.getPanelMainMenu().render();
        Display.update();
        Display.sync(Game.FPS_MAINMENU);

        GL11.glClear(
                GL11.GL_COLOR_BUFFER_BIT
                        | GL11.GL_DEPTH_BUFFER_BIT
                        | GL11.GL_ACCUM_BUFFER_BIT
                        | GL11.GL_STENCIL_BUFFER_BIT);

        Game.startGame(
                context.getParameter(),
                context.getParameter2());
    }
}