package xaos.commands.ui;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.utils.Messages;
import xaos.panels.MessagesPanel;
import xaos.utils.ColorGL;
import xaos.utils.Log;
import xaos.utils.Utils;

public final class BuryCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        try {
            Utils.saveBury();

            MessagesPanel.addMessage(
                    MessagesPanel.TYPE_SYSTEM,
                    Messages.getString("CommandPanel.9"));
        } catch (Exception ex) {
            Log.log(
                    Log.LEVEL_ERROR,
                    Messages.getString("CommandPanel.8") + " [" + ex.toString() + "]",
                    "CommandPanel");

            MessagesPanel.addMessage(
                    MessagesPanel.TYPE_SYSTEM,
                    Messages.getString("CommandPanel.8") + " [" + ex.toString() + "]",
                    ColorGL.RED);
        }
    }
}