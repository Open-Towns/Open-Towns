package xaos.platform.lwjgl3.input;

import java.nio.IntBuffer;

public final class Cursor {
    final int width;
    final int height;
    final int xHotspot;
    final int yHotspot;
    final IntBuffer pixels;

    public Cursor(int width, int height, int xHotspot, int yHotspot, int numImages, IntBuffer images, IntBuffer delays) {
        this.width = width;
        this.height = height;
        this.xHotspot = xHotspot;
        this.yHotspot = yHotspot;
        this.pixels = images == null ? null : images.asReadOnlyBuffer();
    }
}
