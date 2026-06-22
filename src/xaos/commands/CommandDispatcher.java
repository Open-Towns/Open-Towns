package xaos.commands;

import java.util.HashMap;
import java.util.Map;

import xaos.data.Messages;
import xaos.main.Game;
import xaos.panels.CommandPanel;
import xaos.tasks.Task;
import xaos.utils.Log;
import xaos.commands.*;
import xaos.commands.tasks.CreateSelectableTaskCommandHandler;
import xaos.commands.tasks.CreateTaskWithParameterCommandHandler;
import xaos.commands.tasks.CustomActionCommandHandler;
import xaos.commands.ui.BackCommandHandler;

public final class CommandDispatcher {

    private final Map<String, CommandHandler> handlers = new HashMap<>();

    public CommandDispatcher() {
        registerHandlers();
    }

    public void execute(CommandContext context) {
        CommandHandler handler = handlers.get(context.getCommand());

        if (handler == null) {
            handleUnknownCommand(context);
            return;
        }

        handler.execute(context);
    }

    private void registerHandlers() {
        handlers.put(CommandPanel.COMMAND_BACK, new BackCommandHandler());
        handlers.put(CommandPanel.COMMAND_MINE, new CreateSelectableTaskCommandHandler(Task.TASK_MINE));
        handlers.put(CommandPanel.COMMAND_DIG, new CreateSelectableTaskCommandHandler(Task.TASK_DIG));
        handlers.put(CommandPanel.COMMAND_CANCEL_ORDER, new CreateSelectableTaskCommandHandler(Task.TASK_CANCEL_ORDER));
        // handlers.put(CommandPanel.COMMAND_BUILD, new
        // CreateTaskWithParameterCommandHandler(Task.TASK_BUILD));

        handlers.put(CommandPanel.COMMAND_MINE_LADDER, new CreateSelectableTaskCommandHandler(Task.TASK_MINE_LADDER));

        // handlers.put(CommandPanel.COMMAND_CUSTOM_ACTION, new CustomActionCommandHandler());
        handlers.put(CommandPanel.COMMAND_MM_SWITCH_MOUSE_SCROLL,
        new ToggleOptionCommandHandler(() ->
                Game.setMouseScrollON(!Game.isMouseScrollON())
        ));

handlers.put(CommandPanel.COMMAND_MM_SWITCH_MOUSE_SCROLL_EARS,
        new ToggleOptionCommandHandler(() ->
                Game.setMouseScrollEarsON(!Game.isMouseScrollEarsON())
        ));

handlers.put(CommandPanel.COMMAND_MM_SWITCH_MOUSE_2D_CUBES,
        new ToggleOptionCommandHandler(() ->
                Game.setMouse2DCubesON(!Game.isMouse2DCubesON())
        ));

handlers.put(CommandPanel.                COMMAND_MM_SWITCH_DISABLE_ITEMS,
        new ToggleOptionCommandHandler(() ->
                Game.setDisabledItemsON(!Game.isDisabledItemsON())
        ));
    }

    private void handleUnknownCommand(CommandContext context) {
        Log.log(
                Log.LEVEL_ERROR,
                Messages.getString("CommandPanel.6")
                        + context.getCommand()
                        + "] ["
                        + context.getParameter()
                        + "]",
                "CommandPanel");
    }
}