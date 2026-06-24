package xaos.commands.items;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.utils.Messages;
import xaos.panels.TypingPanel;
import xaos.panels.UI.UIPanelState;

public final class ItemTextAddCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        if (UIPanelState.typingPanel != null) {
            return;
        }

        UIPanelState.typingPanel = new TypingPanel(
                UIPanelState.renderWidth,
                UIPanelState.renderHeight,
                Messages.getString("CommandPanel.14"),
                "",
                TypingPanel.TYPE_ADD_TEXT_TO_ITEM,
                Integer.valueOf(context.getParameter())
        );
    }
}