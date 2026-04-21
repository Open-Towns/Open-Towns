package xaos.utils;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;


public class Point3DShort implements Externalizable {

	private static final long serialVersionUID = -2424329152748416157L;

	// Pool
	public static final int MAX_POOL = 1024 * 8;
	public static ArrayList<Point3DShort> alPool = new ArrayList<Point3DShort> ();

	public short x;
	public short y;
	public short z;


	public Point3DShort () {
	}


	public Point3DShort (short x, short y, short z) {
		setPoint (x, y, z);
	}


	// public Point3DShort (int x, int y, int z) {
	// setPoint ((short) x, (short) y, (short) z);
	// }
	//
	//
	// public Point3DShort (Point3DShort p3d) {
	// this (p3d.x, p3d.y, p3d.z);
	// }
	public void setPoint (Point3DShort p3d) {
		this.x = p3d.x;
		this.y = p3d.y;
		this.z = p3d.z;
	}


	public void setPoint (short x, short y, short z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}


	public void setPoint (int x, int y, int z) {
		this.x = (short) x;
		this.y = (short) y;
		this.z = (short) z;
	}


	public void setPoint (short x, short y) {
		this.x = x;
		this.y = y;
	}


	public Point3DShort merge (Point3DShort p3d) {
		return new Point3DShort ((short) (x + p3d.x), (short) (y + p3d.y), (short) (z + p3d.z));
	}


	public int hashCode () {
		return (((((int) x) << 8) + (int) y) << 8) + (int) z;
		// return (((((int) x) * 256) + (int) y) * 256) + (int) z;
	}


	public boolean equals (Object p) {
		Point3DShort p3ds = (Point3DShort) p;
		return p3ds.x == x && p3ds.y == y && p3ds.z == z;
	}


	public boolean equals (Point3D p) {
		return p.x == x && p.y == y && p.z == z;
	}


	public String toString () {
		return x + "," + y + "," + z; //$NON-NLS-1$ //$NON-NLS-2$
	}


	public Point3D toPoint3D () {
		return new Point3D (x, y, z);
	}


	public static Point3DShort getPoolInstance (int x, int y, int z) {
		return getPoolInstance ((short) x, (short) y, (short) z);
	}


	public static Point3DShort getPoolInstance (Point3DShort p3ds) {
		return getPoolInstance ((short) p3ds.x, (short) p3ds.y, (short) p3ds.z);
	}


	public static Point3DShort getPoolInstance (short x, short y, short z) {
		synchronized (alPool) {
			if (alPool.size () > 0) {
				Point3DShort nodo = alPool.remove (alPool.size () - 1);
				nodo.setPoint (x, y, z);
				return nodo;
			} else {
				return new Point3DShort (x, y, z);
			}
		}
	}


	public static void returnToPool (ArrayList<Point3DShort> list) {
		if (list != null) {
			synchronized (alPool) {
				while (list.size () > 0 && alPool.size () < MAX_POOL) {
					alPool.add (list.remove (list.size () - 1));
				}
			}
		}
	}


	public static void returnToPool (Point3DShort nodo) {
		synchronized (alPool) {
			if (alPool.size () < MAX_POOL) {
				alPool.add (nodo);
			}
		}
	}


	@SuppressWarnings("unchecked")
	public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
		x = in.readShort ();
		y = in.readShort ();
		z = in.readShort ();
	}


	public void writeExternal (ObjectOutput out) throws IOException {
		out.writeShort (x);
		out.writeShort (y);
		out.writeShort (z);
	}
}
