package xaos.platform.lwjgl3;

public class LWJGLException extends Exception {
    public LWJGLException(String message) {
        super(message);
    }

    public LWJGLException(String message, Throwable cause) {
        super(message, cause);
    }
}
