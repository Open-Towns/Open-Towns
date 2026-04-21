package xaos.utils;

import java.awt.Point;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Point3D implements Externalizable {

    private static final long serialVersionUID = -3735625297070478527L;

    public int x;
    public int y;
    public int z;

    public Point3D() {
    }

    public Point3D(int x, int y, int z) {
        setPoint(x, y, z);
    }

    public Point3D(Point3D p3d) {
        this(p3d.x, p3d.y, p3d.z);
    }

    public void setPoint(Point3D p3d) {
        this.x = p3d.x;
        this.y = p3d.y;
        this.z = p3d.z;
    }

    public void setPoint(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setPoint(Point point) {
        setPoint(point.x, point.y);
    }

    public void setPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point3D merge(Point3D p3d) {
        return new Point3D(x + p3d.x, y + p3d.y, z + p3d.z);
    }

    public int hashCode() {
        return (((x << 8) + y) << 8) + z;
        //return (((x * 256) + y) * 256) + z;
    }

    public boolean equals(Object p) {
        Point3D p3d = (Point3D) p;
        return p3d.x == x && p3d.y == y && p3d.z == z;
    }

    public boolean equals(Point3DShort p) {
        return p.x == x && p.y == y && p.z == z;
    }

    public String toString() {
        return x + "," + y + "," + z; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public Point3DShort toPoint3DShort() {
        return Point3DShort.getPoolInstance((short) x, (short) y, (short) z);
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        x = in.readInt();
        y = in.readInt();
        z = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(z);
    }
}
