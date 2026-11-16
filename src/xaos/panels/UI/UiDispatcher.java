package xaos.panels.UI;


import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import xaos.utils.Log;

/**
 * Queues UI work to run safely on the main game loop.
 *
 * This prevents UI created during an existing menu/input command
 * from being immediately removed by that command's cleanup logic.
 */
public final class UiDispatcher {

    private static final Queue<Runnable> pendingTasks =
            new ConcurrentLinkedQueue<>();

    private UiDispatcher() {
    }

    /**
     * Schedules UI work for the main game loop.
     */
    public static void invokeLater(Runnable task) {
        if (task != null) {
            pendingTasks.add(task);
        }
    }

    /**
     * Executes UI work waiting for the main thread.
     */
    public static void processPending() {

        Runnable task;

        while ((task = pendingTasks.poll()) != null) {
            try {
                task.run();
            } catch (RuntimeException exception) {
                Log.log(
                        Log.LEVEL_ERROR,
                        "Error processing queued UI task: "
                                + exception,
                        "UiDispatcher"
                );
            }
        }
    }
}