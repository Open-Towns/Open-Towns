package org.lwjgl.opengl;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.LWJGLException;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Display {
    private static long window = NULL;
    private static DisplayMode displayMode;
    private static boolean fullscreen;
    private static boolean resizable = true;
    private static boolean vsync = true;
    private static boolean resized;
    private static String title = "Towns";
    private static long lastSyncTime = System.nanoTime();

    private Display() {
    }

    public static void create() throws LWJGLException {
        if (window != NULL) {
            return;
        }
        if (!glfwInit()) {
            throw new LWJGLException("Unable to initialize GLFW");
        }
        if (displayMode == null) {
            displayMode = getDesktopDisplayMode();
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
        if (fullscreen) {
            glfwWindowHint(GLFW_REFRESH_RATE, displayMode.getFrequency());
        }

        long monitor = fullscreen ? glfwGetPrimaryMonitor() : NULL;
        window = glfwCreateWindow(displayMode.getWidth(), displayMode.getHeight(), title, monitor, NULL);
        if (window == NULL) {
            glfwTerminate();
            throw new LWJGLException("Unable to create GLFW window");
        }

        if (!fullscreen) {
            centerWindow();
        }
        glfwSetWindowSizeCallback(window, (win, width, height) -> {
            if (width > 0 && height > 0) {
                displayMode = new DisplayMode(width, height, displayMode.getBitsPerPixel(), displayMode.getFrequency());
                resized = true;
            }
        });

        glfwMakeContextCurrent(window);
        glfwSwapInterval(vsync ? 1 : 0);
        GL.createCapabilities();
        Mouse.install(window);
        Keyboard.install(window);
        glfwShowWindow(window);
    }

    public static void destroy() {
        Mouse.destroy();
        Keyboard.destroy();
        if (window != NULL) {
            glfwDestroyWindow(window);
            window = NULL;
        }
        glfwTerminate();
    }

    public static void update() {
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    public static void sync(int fps) {
        if (fps <= 0) {
            return;
        }
        long targetNanos = 1_000_000_000L / fps;
        long elapsed = System.nanoTime() - lastSyncTime;
        long sleepNanos = targetNanos - elapsed;
        if (sleepNanos > 1_000_000L) {
            try {
                Thread.sleep(sleepNanos / 1_000_000L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        lastSyncTime = System.nanoTime();
    }

    public static boolean isCloseRequested() {
        return window != NULL && glfwWindowShouldClose(window);
    }

    public static DisplayMode getDesktopDisplayMode() {
        if (window == NULL) {
            glfwInit();
        }
        GLFWVidMode mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (mode == null) {
            return new DisplayMode(1024, 768);
        }
        return new DisplayMode(mode.width(), mode.height(), mode.redBits() + mode.greenBits() + mode.blueBits(), mode.refreshRate());
    }

    public static DisplayMode[] getAvailableDisplayModes() {
        if (window == NULL) {
            glfwInit();
        }
        GLFWVidMode.Buffer modes = glfwGetVideoModes(glfwGetPrimaryMonitor());
        if (modes == null) {
            return new DisplayMode[] { getDesktopDisplayMode() };
        }

        List<DisplayMode> result = new ArrayList<DisplayMode>();
        for (int i = 0; i < modes.limit(); i++) {
            GLFWVidMode mode = modes.get(i);
            result.add(new DisplayMode(mode.width(), mode.height(), mode.redBits() + mode.greenBits() + mode.blueBits(), mode.refreshRate()));
        }
        return result.toArray(new DisplayMode[0]);
    }

    public static void setDisplayMode(DisplayMode mode) throws LWJGLException {
        if (mode == null) {
            throw new LWJGLException("Display mode cannot be null");
        }
        displayMode = mode;
        if (window != NULL) {
            applyDisplayMode();
        }
    }

    public static void setDisplayModeAndFullscreen(DisplayMode mode) throws LWJGLException {
        setDisplayMode(mode);
        setFullscreen(true);
    }

    public static void setFullscreen(boolean enabled) throws LWJGLException {
        fullscreen = enabled;
        if (window != NULL) {
            applyDisplayMode();
        }
    }

    public static boolean isFullscreen() {
        return fullscreen;
    }

    public static void setResizable(boolean enabled) {
        resizable = enabled;
        if (window != NULL) {
            glfwSetWindowAttrib(window, GLFW_RESIZABLE, enabled ? GLFW_TRUE : GLFW_FALSE);
        }
    }

    public static void setVSyncEnabled(boolean enabled) {
        vsync = enabled;
        if (window != NULL) {
            glfwSwapInterval(enabled ? 1 : 0);
        }
    }

    public static void setTitle(String newTitle) {
        title = newTitle;
        if (window != NULL) {
            glfwSetWindowTitle(window, title);
        }
    }

    public static void setIcon(ByteBuffer[] icons) {
        // The legacy call site passes raw pixels without dimensions. Keep this
        // as a harmless no-op until the icon path is migrated directly to GLFW.
    }

    public static int getWidth() {
        return displayMode == null ? getDesktopDisplayMode().getWidth() : displayMode.getWidth();
    }

    public static int getHeight() {
        return displayMode == null ? getDesktopDisplayMode().getHeight() : displayMode.getHeight();
    }

    public static boolean wasResized() {
        boolean result = resized;
        resized = false;
        return result;
    }

    public static long getWindowHandle() {
        return window;
    }

    private static void applyDisplayMode() {
        long monitor = fullscreen ? glfwGetPrimaryMonitor() : NULL;
        int refresh = fullscreen ? displayMode.getFrequency() : GLFW_DONT_CARE;
        glfwSetWindowMonitor(window, monitor, 0, 0, displayMode.getWidth(), displayMode.getHeight(), refresh);
        if (!fullscreen) {
            centerWindow();
        }
        resized = true;
    }

    private static void centerWindow() {
        GLFWVidMode desktop = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (desktop == null || displayMode == null) {
            return;
        }
        glfwSetWindowPos(window, (desktop.width() - displayMode.getWidth()) / 2, (desktop.height() - displayMode.getHeight()) / 2);
    }

    public static int[] getFramebufferSize() {
        if (window == NULL) {
            return new int[] { getWidth(), getHeight() };
        }
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetFramebufferSize(window, width, height);
            return new int[] { width.get(0), height.get(0) };
        }
    }
}
