package xaos.commands.options;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.Towns;
import xaos.utils.Utils;
import xaos.utils.UtilsAL;

public final class ToggleModCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        Game.toggleMod(context.getParameter());

        Towns.clearPropertiesGraphics();
        Game.loadAllIniTextures();

        UtilsAL.clearPropertiesAudio();
        UtilsAL.initAL(Game.getVolumeMusic(), Game.getVolumeFX());

        if (Game.getPanelMainMenu().isActive()) {
            UtilsAL.play(UtilsAL.SOURCE_MUSIC_MAINMENU);
        } else {
            UtilsAL.play(UtilsAL.SOURCE_MUSIC_INGAME);
        }

        Utils.saveOptions();

        Game.exitToMainMenu();
        Game.getPanelMainMenu().loadMenuTexture(true);
        Game.getPanelMainMenu().createMenu();
    }
}