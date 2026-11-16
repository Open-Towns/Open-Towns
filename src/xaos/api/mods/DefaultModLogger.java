package xaos.api.mods;


import java.io.PrintWriter;
import java.io.StringWriter;

import xaos.api.mods.ModLogger;
import xaos.utils.Log;

/**
 * Bridges the public ModLogger API to Towns' existing logging system.
 */
final class DefaultModLogger implements ModLogger {

    private final String modId;

    DefaultModLogger(String modId) {
        this.modId = modId;
    }

    @Override
    public void info(String message) {
        Log.log(
                Log.LEVEL_DEBUG,
                message,
                "Mod:" + modId
        );
    }

    @Override
    public void error(String message) {
        Log.log(
                Log.LEVEL_ERROR,
                message,
                "Mod:" + modId
        );
    }

    @Override
    public void error(String message, Throwable throwable) {
        StringWriter output = new StringWriter();

        if (throwable != null) {
            throwable.printStackTrace(
                    new PrintWriter(output)
            );
        }

        Log.log(
                Log.LEVEL_ERROR,
                message
                        + (throwable == null
                                ? ""
                                : System.lineSeparator()
                                        + output),
                "Mod:" + modId
        );
    }
}