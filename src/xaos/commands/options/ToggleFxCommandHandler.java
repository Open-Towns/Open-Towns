package xaos.commands.options;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.main.Game;
import xaos.utils.Utils;
import xaos.utils.UtilsAL;

public final class ToggleFxCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        Game.setFXON(!Game.isFXON());
        Utils.saveOptions();

        if (!Game.isFXON()) {
            UtilsAL.stopFX();
            return;
        }

        UtilsAL.initAL(Game.getVolumeMusic(), Game.getVolumeFX());
        UtilsAL.play(UtilsAL.SOURCE_FX_CLICK);
    }
}