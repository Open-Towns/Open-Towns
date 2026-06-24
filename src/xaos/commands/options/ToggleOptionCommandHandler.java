package xaos.commands.options;
import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;
import xaos.utils.Utils;

public final class ToggleOptionCommandHandler implements CommandHandler {

    private final Runnable toggleAction;

    public ToggleOptionCommandHandler(Runnable toggleAction) {
        this.toggleAction = toggleAction;
    }

    @Override
    public void execute(CommandContext context) {
        toggleAction.run();
        Utils.saveOptions();
    }
}