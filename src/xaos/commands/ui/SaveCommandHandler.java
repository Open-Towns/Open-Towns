package xaos.commands.ui;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.utils.Messages;
import xaos.panels.MessagesPanel;
import xaos.utils.ColorGL;
import xaos.utils.Log;
import xaos.utils.Utils;

public final class SaveCommandHandler implements CommandHandler {

    private final boolean saveMissionData;

    public SaveCommandHandler(boolean saveMissionData) {
        this.saveMissionData = saveMissionData;
    }

    @Override
    public void execute(CommandContext context) {
        try {
            Utils.save(saveMissionData);
        } catch (Exception ex) {
            Log.log(
                    Log.LEVEL_ERROR,
                    Messages.getString("CommandPanel.38") + ex.toString() + "]",
                    "CommandPanel"
            );

            MessagesPanel.addMessage(
                    MessagesPanel.TYPE_SYSTEM,
                    Messages.getString("CommandPanel.38") + ex.toString() + "]",
                    ColorGL.RED
            );
        }
    }
}