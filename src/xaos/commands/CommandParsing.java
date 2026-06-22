package xaos.commands;

import xaos.utils.Log;

public final class CommandParsing {

    private CommandParsing() {
    }

    public static Integer parseIntOrNull(String value, String commandName) {
        if (value == null) {
            logBadParameter(commandName, value);
            return null;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            logBadParameter(commandName, value);
            return null;
        }
    }

    private static void logBadParameter(String commandName, String value) {
        Log.log(
                Log.LEVEL_ERROR,
                "Invalid command parameter for [" + commandName + "]: [" + value + "]",
                "CommandPanel"
        );
    }
}