package xaos.commands.ui;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.utils.UtilsAL;

public final class ExitToMainMenuNoSaveCommandHandler implements CommandHandler {
    @Override
    public void execute(CommandContext context) {
        UtilsAL.stopMusic();
        UtilsAL.stopFX();
        UtilsAL.play(UtilsAL.SOURCE_MUSIC_MAINMENU);
        Game.exitToMainMenu();
    }
}
