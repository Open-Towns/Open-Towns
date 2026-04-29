package xaos.platform.lwjgl3;

public final class Sys {
    private Sys() {
    }

    public static long getTimerResolution() {
        return 1_000_000_000L;
    }
}
