package xaos.commands.options;

import java.awt.Desktop;
import java.io.File;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;

public final class OpenFolderCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(context.getParameter()));
            }
        } catch (Exception ignored) {
        }
    }
}