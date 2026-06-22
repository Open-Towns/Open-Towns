package xaos.commands;
import xaos.tiles.Tile;
import xaos.utils.Point3D;

public final class CommandContext {

    private final String command;
    private final String parameter;
    private final String parameter2;
    private final Point3D directPoint;
    private final Tile tile;
    private final int iconType;

    public CommandContext(
            String command,
            String parameter,
            String parameter2,
            Point3D directPoint,
            Tile tile,
            int iconType
    ) {
        this.command = command;
        this.parameter = parameter;
        this.parameter2 = parameter2;
        this.directPoint = directPoint;
        this.tile = tile;
        this.iconType = iconType;
    }

    public String getCommand() {
        return command;
    }

    public String getParameter() {
        return parameter;
    }

    public String getParameter2() {
        return parameter2;
    }

    public Point3D getDirectPoint() {
        return directPoint;
    }

    public Tile getTile() {
        return tile;
    }

    public int getIconType() {
        return iconType;
    }

    public boolean hasDirectPoint() {
        return directPoint != null;
    }
}