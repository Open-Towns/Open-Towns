# Command Dispatcher System

## Overview

The command dispatcher system replaces the old large `CommandPanel.executeCommand(...)` `if / else if` chain with a smaller, more maintainable command routing system.

Instead of keeping every command action inside `CommandPanel`, each command now has a dedicated handler class. `CommandPanel` receives the command, creates a `CommandContext`, and passes it to `CommandDispatcher`.

The goal of this refactor is to make command behaviour easier to read, easier to test, and easier to extend, especially as more menu items move into XML.

## Why this system exists

The old command system had several problems:

* All command behaviour lived in one very large method.
* Unrelated systems were mixed together, such as UI, tasks, stockpiles, containers, options, debug tools, and main menu logic.
* Adding a new command meant editing a fragile `if / else if` chain.
* It was difficult to see which commands existed.
* Repeated command patterns were duplicated many times.

The new system separates command routing from command behaviour.

## Core classes

### `CommandContext`

`CommandContext` stores the data passed into a command.

Typical fields include:

```java
private final String command;
private final String parameter;
private final String parameter2;
private final Point3D directPoint;
private final Tile tile;
private final int iconType;
```

This avoids passing the same long parameter list into every handler.

Instead of this:

```java
executeCommand(sCommand, sParameter, sParameter2, p3dDirect, tile, iconType);
```

Handlers receive this:

```java
handler.execute(context);
```

### `CommandHandler`

Every command handler implements the same interface:

```java
public interface CommandHandler {

    void execute(CommandContext context);
}
```

This gives all command handlers the same structure.

### `CommandDispatcher`

`CommandDispatcher` owns the command map:

```java
private final Map<String, CommandHandler> handlers = new HashMap<>();
```

Each command is registered with a handler:

```java
handlers.put(CommandPanel.COMMAND_BACK, new BackCommandHandler());
```

When a command is executed, the dispatcher looks up the handler and runs it.


## Handler examples

### Simple UI command

```java
public final class PauseCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        Game.togglePause(true);

        Game.updateTutorialFlow(
                TutorialTrigger.TYPE_INT_ICONHIT,
                TutorialTrigger.ICON_INT_PAUSE,
                null
        );
    }
}
```

Registration:

```java
handlers.put(CommandPanel.COMMAND_PAUSE, new PauseCommandHandler());
```

### Generic selectable task command

Used for commands like mine, dig, and cancel order.

```java
public final class CreateSelectableTaskCommandHandler implements CommandHandler {

    private final int taskType;

    public CreateSelectableTaskCommandHandler(int taskType) {
        this.taskType = taskType;
    }

    @Override
    public void execute(CommandContext context) {
        Game.createTask(taskType);

        if (Game.getCurrentTask() == null) {
            return;
        }

        Game.getCurrentTask().setTile(
                context.getTile(),
                context.getIconType()
        );

        if (context.getDirectPoint() != null) {
            Game.getCurrentTask().setPoint(context.getDirectPoint());
            Game.getCurrentTask().setPoint(context.getDirectPoint());
        }
    }
}
```

Registration:

```java
handlers.put(
        CommandPanel.COMMAND_MINE,
        new CreateSelectableTaskCommandHandler(Task.TASK_MINE)
);

handlers.put(
        CommandPanel.COMMAND_DIG,
        new CreateSelectableTaskCommandHandler(Task.TASK_DIG)
);
```

### Generic task-with-parameter command

Used for commands like build, stockpile, create zone, expand zone, create and place.

```java
public final class CreateTaskWithParameterCommandHandler implements CommandHandler {

    private final int taskType;

    public CreateTaskWithParameterCommandHandler(int taskType) {
        this.taskType = taskType;
    }

    @Override
    public void execute(CommandContext context) {
        Game.createTask(taskType, context.getParameter());

        if (Game.getCurrentTask() == null) {
            return;
        }

        Game.getCurrentTask().setTile(
                context.getTile(),
                context.getIconType()
        );
    }
}
```

Registration:

```java
handlers.put(
        CommandPanel.COMMAND_BUILD,
        new CreateTaskWithParameterCommandHandler(Task.TASK_BUILD)
);

handlers.put(
        CommandPanel.COMMAND_STOCKPILE,
        new CreateTaskWithParameterCommandHandler(Task.TASK_STOCKPILE)
);
```

### Simple option command

Used for options that run one action and then save options.

```java
public final class SimpleOptionCommandHandler implements CommandHandler {

    private final Runnable action;

    public SimpleOptionCommandHandler(Runnable action) {
        this.action = action;
    }

    @Override
    public void execute(CommandContext context) {
        action.run();
        Utils.saveOptions();
    }
}
```

Registration:

```java
handlers.put(
        CommandPanel.COMMAND_MM_UI_SCALE,
        new SimpleOptionCommandHandler(() -> UIScaler.cycleUIScale())
);

handlers.put(
        CommandPanel.COMMAND_MM_WORLD_ZOOM,
        new SimpleOptionCommandHandler(() -> MainPanel.cycleWorldZoom())
);
```

## How to add a new command

### Step 1 - Add or identify the command constant

Command constants currently live in `CommandPanel`.

Example:

```java
public static final String COMMAND_MY_NEW_COMMAND = "mynewcommand";
```

### Step 2 - Create a handler

Create a new class in the correct package.

Example:

```java
package xaos.commands.ui;

import xaos.commands.CommandContext;
import xaos.commands.CommandHandler;

public final class MyNewCommandHandler implements CommandHandler {

    @Override
    public void execute(CommandContext context) {
        // Command behaviour goes here
    }
}
```

### Step 3 - Register the handler

Add it to `CommandDispatcher.registerHandlers()`.

```java
handlers.put(
        CommandPanel.COMMAND_MY_NEW_COMMAND,
        new MyNewCommandHandler()
);
```

### Step 4 - Test the command

Use the menu or XML entry that triggers the command and confirm that:

* The handler is called.
* Any parameters are passed correctly.


## Important notes

### Avoid duplicate registrations

`handlers.put(...)` will overwrite an existing registration for the same command key.

This usually will not crash, but it can hide mistakes.

Avoid this:

```java
handlers.put(CommandPanel.COMMAND_STOCKPILE_ENABLE_ALL, new StockpileEnableAllCommandHandler());
handlers.put(CommandPanel.COMMAND_STOCKPILE_ENABLE_ALL, new StockpileEnableAllCommandHandler());
```

### Be careful with recursive commands

Some old commands call `executeCommand(...)` internally.

Examples:

```java
COMMAND_EXIT_TO_MAIN_MENU_SAVE
COMMAND_MM_NEWGAME
COMMAND_MM_NEWGAME_SET_SAVE_NAME_NO_BURY
```

This is usually fine, but it means the command may go through the dispatcher again.

When possible, prefer moving shared behaviour into a handler or helper rather than calling another command.



### Keep debug commands separate

Debug and test commands should stay in `xaos.commands.debug`.

They should also check:

```java
if (!TownsProperties.TEST_COMMANDS) {
    return;
}
```

This prevents accidental execution when test commands are disabled.

## Future improvements

Possible future improvements:

* Move command constants out of `CommandPanel` into a dedicated `CommandIds` class.
* Add command registration tests to detect duplicate command keys.
* Add safe parsing helpers for integer parameters.
* Replace integer action types with enums in generic handlers.
* Add XML validation to check that menu command names exist in the dispatcher.
