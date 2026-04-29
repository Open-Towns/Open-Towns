package org.lwjgl.input;

import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

import org.lwjgl.LWJGLException;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.Display;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Mouse {
    private static long window = NULL;
    private static boolean insideWindow;
    private static int x;
    private static int y;
    private static long nativeCursor = NULL;
    private static final Queue<Event> events = new ArrayDeque<Event>();
    private static Event currentEvent;

    private Mouse() {
    }

    public static void install(long windowHandle) {
        window = windowHandle;
        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            x = (int) xpos;
            y = toLegacyY((int) ypos);
        });
        glfwSetCursorEnterCallback(window, (win, entered) -> insideWindow = entered);
        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (action == GLFW_PRESS || action == GLFW_RELEASE) {
                events.add(new Event(x, y, button, action == GLFW_PRESS, 0));
            }
        });
        glfwSetScrollCallback(window, (win, xoffset, yoffset) -> events.add(new Event(x, y, -1, false, (int) Math.signum(yoffset))));
    }

    public static void destroy() {
        if (nativeCursor != NULL) {
            glfwDestroyCursor(nativeCursor);
            nativeCursor = NULL;
        }
        events.clear();
        currentEvent = null;
        window = NULL;
    }

    public static boolean next() {
        currentEvent = events.poll();
        return currentEvent != null;
    }

    public static int getX() {
        updateCursorPosition();
        return x;
    }

    public static int getY() {
        updateCursorPosition();
        return y;
    }

    public static int getEventX() {
        return currentEvent == null ? getX() : currentEvent.x;
    }

    public static int getEventY() {
        return currentEvent == null ? getY() : currentEvent.y;
    }

    public static int getEventButton() {
        return currentEvent == null ? -1 : currentEvent.button;
    }

    public static boolean getEventButtonState() {
        return currentEvent != null && currentEvent.buttonState;
    }

    public static int getEventDWheel() {
        return currentEvent == null ? 0 : currentEvent.dWheel;
    }

    public static boolean isInsideWindow() {
        return insideWindow;
    }

    public static void setNativeCursor(Cursor cursor) throws LWJGLException {
        if (window == NULL || cursor == null || cursor.pixels == null) {
            return;
        }
        GLFWImage image = GLFWImage.malloc();
        try {
            image.width(cursor.width);
            image.height(cursor.height);
            image.pixels(cursorPixels(cursor.pixels));
            long newCursor = glfwCreateCursor(image, cursor.xHotspot, cursor.yHotspot);
            if (newCursor == NULL) {
                throw new LWJGLException("Unable to create native cursor");
            }
            glfwSetCursor(window, newCursor);
            if (nativeCursor != NULL) {
                glfwDestroyCursor(nativeCursor);
            }
            nativeCursor = newCursor;
        } finally {
            image.free();
        }
    }

    private static void updateCursorPosition() {
        if (window == NULL) {
            return;
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            java.nio.DoubleBuffer xpos = stack.mallocDouble(1);
            java.nio.DoubleBuffer ypos = stack.mallocDouble(1);
            glfwGetCursorPos(window, xpos, ypos);
            x = (int) xpos.get(0);
            y = toLegacyY((int) ypos.get(0));
        }
    }

    private static int toLegacyY(int glfwY) {
        return Display.getHeight() - glfwY - 1;
    }

    private static java.nio.ByteBuffer cursorPixels(IntBuffer argbPixels) {
        IntBuffer source = argbPixels.asReadOnlyBuffer();
        source.rewind();
        java.nio.ByteBuffer rgba = java.nio.ByteBuffer.allocateDirect(source.remaining() * 4);
        while (source.hasRemaining()) {
            int argb = source.get();
            rgba.put((byte) ((argb >> 16) & 0xff));
            rgba.put((byte) ((argb >> 8) & 0xff));
            rgba.put((byte) (argb & 0xff));
            rgba.put((byte) ((argb >> 24) & 0xff));
        }
        rgba.flip();
        return rgba;
    }

    private static final class Event {
        final int x;
        final int y;
        final int button;
        final boolean buttonState;
        final int dWheel;

        Event(int x, int y, int button, boolean buttonState, int dWheel) {
            this.x = x;
            this.y = y;
            this.button = button;
            this.buttonState = buttonState;
            this.dWheel = dWheel;
        }
    }
}
