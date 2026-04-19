package xaos.campaign;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.StringTokenizer;

import xaos.main.World;
import xaos.utils.Messages;
import xaos.utils.Point3D;
import xaos.utils.Utils;


public class TutorialTrigger implements Externalizable {
	private static final long serialVersionUID = -1647103476689470707L;

	public static final int WASD_UP = 1;
	public static final int WASD_DOWN = 2;
	public static final int WASD_LEFT = 4;
	public static final int WASD_RIGHT = 8;

	public static final int LAYER_UP = 1;
	public static final int LAYER_DOWN = 2;

	public static final String ICON_FLAT = "flat"; //$NON-NLS-1$
	public static final String ICON_GRID = "grid"; //$NON-NLS-1$
	public static final String ICON_FLATCURSOR = "flatcursor"; //$NON-NLS-1$
	public static final String ICON_3DMOUSE = "3dmouse"; //$NON-NLS-1$
	public static final String ICON_SETTINGS = "settings"; //$NON-NLS-1$
	public static final String ICON_LEVELUP = "levelup"; //$NON-NLS-1$
	public static final String ICON_LEVELDOWN = "leveldown"; //$NON-NLS-1$
	public static final String ICON_SPEEDUP = "speedup"; //$NON-NLS-1$
	public static final String ICON_SPEEDDOWN = "speeddown"; //$NON-NLS-1$
	public static final String ICON_PAUSE = "pause"; //$NON-NLS-1$
	public static final String ICON_REGULAR_PLUS = "regularplus"; //$NON-NLS-1$
	public static final String ICON_REGULAR_MINUS = "regularminus"; //$NON-NLS-1$
	public static final String ICON_AUTOMATED_PLUS = "automatedplus"; //$NON-NLS-1$
	public static final String ICON_AUTOMATED_MINUS = "automatedminus"; //$NON-NLS-1$
	public static final String ICON_CITIZENS = "citizens"; //$NON-NLS-1$
	public static final String ICON_SOLDIERS = "soldiers"; //$NON-NLS-1$
	public static final String ICON_HEROES = "heroes"; //$NON-NLS-1$
	public static final String ICON_TRADE = "trade"; //$NON-NLS-1$
	public static final String ICON_LIVINGS_LIVINGS = "livings_livings"; //$NON-NLS-1$
	public static final String ICON_LIVINGS_BODY = "livings_body"; //$NON-NLS-1$
	public static final String ICON_LIVINGS_AUTOEQUIP = "livings_autoequip"; //$NON-NLS-1$
	public static final String ICON_LIVINGS_CONVERTSOLDIER = "livings_convertsoldier"; //$NON-NLS-1$
	public static final String ICON_LIVINGS_JOBS = "livings_jobs"; //$NON-NLS-1$
	public static final String ICON_LIVINGS_GROUPS= "livings_groups"; //$NON-NLS-1$
	public static final String ICON_LIVINGS_CONVERTCIVILIAN = "livings_convertcivilian"; //$NON-NLS-1$
	public static final String ICON_LIVINGS_RESTRICTION = "livings_restriction"; //$NON-NLS-1$
	public static final String ICON_LIVINGS_GUARD = "livings_guard"; //$NON-NLS-1$
	public static final String ICON_LIVINGS_PATROL = "livings_patrol"; //$NON-NLS-1$
	public static final String ICON_LIVINGS_BOSS = "livings_boss"; //$NON-NLS-1$

	public static final int ICON_INT_FLAT = 1;
	public static final int ICON_INT_GRID = 2;
	public static final int ICON_INT_FLATCURSOR = 3;
	public static final int ICON_INT_3DMOUSE = 4;
	public static final int ICON_INT_SETTINGS = 5;
	public static final int ICON_INT_LEVELUP = 6;
	public static final int ICON_INT_LEVELDOWN = 7;
	public static final int ICON_INT_SPEEDUP = 8;
	public static final int ICON_INT_SPEEDDOWN = 9;
	public static final int ICON_INT_PAUSE = 10;
	public static final int ICON_INT_REGULAR_PLUS = 11;
	public static final int ICON_INT_REGULAR_MINUS = 12;
	public static final int ICON_INT_AUTOMATED_PLUS = 13;
	public static final int ICON_INT_AUTOMATED_MINUS = 14;
	public static final int ICON_INT_CITIZENS = 15;
	public static final int ICON_INT_SOLDIERS = 16;
	public static final int ICON_INT_HEROES = 17;
	public static final int ICON_INT_TRADE = 18;
	public static final int ICON_INT_LIVINGS_LIVINGS = 19;
	public static final int ICON_INT_LIVINGS_BODY = 20;
	public static final int ICON_INT_LIVINGS_AUTOEQUIP = 21;
	public static final int ICON_INT_LIVINGS_CONVERTSOLDIER = 22;
	public static final int ICON_INT_LIVINGS_JOBS = 23;
	public static final int ICON_INT_LIVINGS_GROUPS = 24;
	public static final int ICON_INT_LIVINGS_CONVERTCIVILIAN = 25;
	public static final int ICON_INT_LIVINGS_RESTRICTION = 26;
	public static final int ICON_INT_LIVINGS_GUARD = 27;
	public static final int ICON_INT_LIVINGS_PATROL = 28;
	public static final int ICON_INT_LIVINGS_BOSS = 29;

	public static final String TYPE_COLLECT = "COLLECT"; //$NON-NLS-1$
	public static final String TYPE_BUILD = "BUILD"; //$NON-NLS-1$
	public static final String TYPE_ZONE = "ZONE"; //$NON-NLS-1$
	public static final String TYPE_KILL = "KILL"; //$NON-NLS-1$
	public static final String TYPE_PILE = "PILE"; //$NON-NLS-1$
	public static final String TYPE_SOLDIER = "SOLDIER"; //$NON-NLS-1$
	public static final String TYPE_WASD = "WASD"; //$NON-NLS-1$
	public static final String TYPE_LAYERUPDOWN = "LAYERUPDOWN"; //$NON-NLS-1$
	public static final String TYPE_ICONHIT = "ICONHIT"; //$NON-NLS-1$
	public static final String TYPE_PLACE = "PLACE"; //$NON-NLS-1$
	public static final String TYPE_POPULATION = "POPULATION"; //$NON-NLS-1$
	public static final String TYPE_TILL = "TILL"; //$NON-NLS-1$
	public static final String TYPE_CIV2GROUP = "CIV2GROUP"; //$NON-NLS-1$

	public static final int TYPE_INT_NONE = 0;
	public static final int TYPE_INT_COLLECT = 1;
	public static final int TYPE_INT_BUILD = 2;
	public static final int TYPE_INT_ZONE = 3;
	public static final int TYPE_INT_KILL = 4;
	public static final int TYPE_INT_PILE = 5;
	public static final int TYPE_INT_SOLDIER = 6;
	public static final int TYPE_INT_WASD = 7;
	public static final int TYPE_INT_LAYERUPDOWN = 8;
	public static final int TYPE_INT_ICONHIT = 9;
	public static final int TYPE_INT_PLACE = 10;
	public static final int TYPE_INT_POPULATION = 11;
	public static final int TYPE_INT_TILL = 12;
	public static final int TYPE_INT_CIV2GROUP = 13;

	private int type;
	private String param1;
	private int param2;
	private Point3D paramXYZ;


	public TutorialTrigger () {
	}

	public int getType () {
		return type;
	}


	public void setType (String type) throws Exception {
		if (type == null) {
			throw new Exception (Messages.getString ("TutorialTrigger.0") + " [" + type + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		if (type.equalsIgnoreCase (TYPE_COLLECT)) {
			this.type = TYPE_INT_COLLECT;
		} else if (type.equalsIgnoreCase (TYPE_BUILD)) {
			this.type = TYPE_INT_BUILD;
		} else if (type.equalsIgnoreCase (TYPE_ZONE)) {
			this.type = TYPE_INT_ZONE;
		} else if (type.equalsIgnoreCase (TYPE_KILL)) {
			this.type = TYPE_INT_KILL;
		} else if (type.equalsIgnoreCase (TYPE_PILE)) {
			this.type = TYPE_INT_PILE;
		} else if (type.equalsIgnoreCase (TYPE_SOLDIER)) {
			this.type = TYPE_INT_SOLDIER;
		} else if (type.equalsIgnoreCase (TYPE_WASD)) {
			this.type = TYPE_INT_WASD;
		} else if (type.equalsIgnoreCase (TYPE_LAYERUPDOWN)) {
			this.type = TYPE_INT_LAYERUPDOWN;
		} else if (type.equalsIgnoreCase (TYPE_ICONHIT)) {
			this.type = TYPE_INT_ICONHIT;
		} else if (type.equalsIgnoreCase (TYPE_PLACE)) {
			this.type = TYPE_INT_PLACE;
		} else if (type.equalsIgnoreCase (TYPE_POPULATION)) {
			this.type = TYPE_INT_POPULATION;
		} else if (type.equalsIgnoreCase (TYPE_TILL)) {
			this.type = TYPE_INT_TILL;
		} else if (type.equalsIgnoreCase (TYPE_CIV2GROUP)) {
			this.type = TYPE_INT_CIV2GROUP;
		} else {
			throw new Exception (Messages.getString ("TutorialTrigger.0") + " [" + type + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}


	public String getParam1 () {
		return param1;
	}


	public void setParam1 (String param1) {
		this.param1 = param1;
	}


	public int getParam2 () {
		return param2;
	}


	public void setParam2 (int param2) {
		this.param2 = param2;
	}


	public void setParam2 (String sParam2) {
		setParam2 (Utils.launchDice (sParam2));
	}


	public void setParamXYZ (String sParamXYZ) throws Exception {
		if (sParamXYZ == null || sParamXYZ.trim ().length () == 0) {
			this.paramXYZ = null;
		} else {
			StringTokenizer tokenizer = new StringTokenizer (sParamXYZ, ","); //$NON-NLS-1$
			int iX = -1;
			int iY = -1;
			int iZ = 6789;
			if (tokenizer.hasMoreTokens ()) {
				iX = Integer.parseInt (tokenizer.nextToken ());

				if (tokenizer.hasMoreTokens ()) {
					iY = Integer.parseInt (tokenizer.nextToken ());

					if (tokenizer.hasMoreTokens ()) {
						iZ = Integer.parseInt (tokenizer.nextToken ());
					}
				}
			}

			if (iX >= 0 && iX < World.MAP_WIDTH && iY >= 0 && iY < World.MAP_HEIGHT && iZ != 6789) {
				this.paramXYZ = new Point3D (iX, iY, iZ);
			} else {
				throw new Exception (Messages.getString("TutorialTrigger.1") + " [" + sParamXYZ + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
	}

	public Point3D getParamXYZ () {
		return paramXYZ;
	}

	public void readExternal (ObjectInput in) throws IOException, ClassNotFoundException {
		type = in.readInt ();
		param1 = (String) in.readObject ();
		param2 = in.readInt ();
		paramXYZ = (Point3D) in.readObject ();
	}


	public void writeExternal (ObjectOutput out) throws IOException {
		out.writeInt (type);
		out.writeObject (param1);
		out.writeInt (param2);
		out.writeObject (paramXYZ);
	}
}
