package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import xaos.utils.Messages;

public class HeroPrerequisite implements Externalizable {

    private static final long serialVersionUID = -7811687233202147070L;

    public final static String STR_ID_MIN_CITIZENS = "minCitizens"; //$NON-NLS-1$
    public final static String STR_ID_MAX_CITIZENS = "maxCitizens"; //$NON-NLS-1$
    public final static String STR_ID_LEVEL_DISCOVERED = "levelDiscovered"; //$NON-NLS-1$
    public final static String STR_ID_FREE_ROOM = "freeRoom"; //$NON-NLS-1$
    public final static String STR_ID_FREE_ROOM_UNDERGROUND = "freeRoomUnderground"; //$NON-NLS-1$
    public final static String STR_ID_FREE_ROOM_HIGH = "freeRoomHigh"; //$NON-NLS-1$
    public final static String STR_ID_ZONE = "zone"; //$NON-NLS-1$
    public final static String STR_ID_FREE_ROOM_ITEMS = "freeRoomItems"; //$NON-NLS-1$

    public final static int ID_MIN_CITIZENS = 1;
    public final static int ID_MAX_CITIZENS = 2;
    public final static int ID_LEVEL_DISCOVERED = 3;
    public final static int ID_FREE_ROOM = 4;
    public final static int ID_FREE_ROOM_UNDERGROUND = 5;
    public final static int ID_FREE_ROOM_HIGH = 6;
    public final static int ID_ZONE = 7;
    public final static int ID_FREE_ROOM_ITEMS = 8;

    private int id;
    private int valueInt;
    private boolean valueBoolean;
    private String valueString;

    public HeroPrerequisite() {
    }

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public void setId(String sID) throws Exception {
        if (sID == null || sID.length() == 0) {
            throw new Exception(Messages.getString("HeroPrerequisite.0")); //$NON-NLS-1$
        }

        if (sID.equalsIgnoreCase(STR_ID_MIN_CITIZENS)) {
            setId(ID_MIN_CITIZENS);
        } else if (sID.equalsIgnoreCase(STR_ID_MAX_CITIZENS)) {
            setId(ID_MAX_CITIZENS);
        } else if (sID.equalsIgnoreCase(STR_ID_LEVEL_DISCOVERED)) {
            setId(ID_LEVEL_DISCOVERED);
        } else if (sID.equalsIgnoreCase(STR_ID_FREE_ROOM)) {
            setId(ID_FREE_ROOM);
        } else if (sID.equalsIgnoreCase(STR_ID_FREE_ROOM_UNDERGROUND)) {
            setId(ID_FREE_ROOM_UNDERGROUND);
        } else if (sID.equalsIgnoreCase(STR_ID_FREE_ROOM_HIGH)) {
            setId(ID_FREE_ROOM_HIGH);
        } else if (sID.equalsIgnoreCase(STR_ID_ZONE)) {
            setId(ID_ZONE);
        } else if (sID.equalsIgnoreCase(STR_ID_FREE_ROOM_ITEMS)) {
            setId(ID_FREE_ROOM_ITEMS);
        } else {
            throw new Exception(Messages.getString("HeroPrerequisite.1") + sID + "]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public String getStringID(int iID) {
        switch (iID) {
            case ID_MIN_CITIZENS:
                return STR_ID_MIN_CITIZENS;
            case ID_MAX_CITIZENS:
                return STR_ID_MAX_CITIZENS;
            case ID_LEVEL_DISCOVERED:
                return STR_ID_LEVEL_DISCOVERED;
            case ID_FREE_ROOM:
                return STR_ID_FREE_ROOM;
            case ID_FREE_ROOM_UNDERGROUND:
                return STR_ID_FREE_ROOM_UNDERGROUND;
            case ID_FREE_ROOM_HIGH:
                return STR_ID_FREE_ROOM_HIGH;
            case ID_ZONE:
                return STR_ID_ZONE;
            case ID_FREE_ROOM_ITEMS:
                return STR_ID_FREE_ROOM_ITEMS;
            default:
                return Messages.getString("HeroPrerequisite.4"); //$NON-NLS-1$
        }
    }

    public int getValueInt() {
        return valueInt;
    }

    public void setValueInt(int valueInt) {
        this.valueInt = valueInt;
    }

    public boolean isValueBoolean() {
        return valueBoolean;
    }

    public void setValueBoolean(boolean valueBoolean) {
        this.valueBoolean = valueBoolean;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public void setValue(String sValue) throws Exception {
        if (sValue == null || sValue.length() == 0) {
            throw new Exception(Messages.getString("HeroPrerequisite.3")); //$NON-NLS-1$
        }

        switch (getId()) {
            case ID_MIN_CITIZENS:
            case ID_MAX_CITIZENS:
            case ID_LEVEL_DISCOVERED:
            case ID_FREE_ROOM_HIGH:
                // Numérico
                setValueInt(Integer.parseInt(sValue));
                break;
            case ID_FREE_ROOM:
            case ID_FREE_ROOM_UNDERGROUND:
                // Booleano
                setValueBoolean(Boolean.parseBoolean(sValue));
                break;
            case ID_ZONE:
            case ID_FREE_ROOM_ITEMS:
                // String
                setValueString(new String(sValue));
                break;
            default:
                throw new Exception(Messages.getString("HeroPrerequisite.2") + getStringID(getId()) + "] [" + sValue + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    /**
     * Se le pasa una lista de prerequisitos (puede ser null o vacía) y un ID de
     * prerequisito. Si ese prerequisito existe en la lista lo devuelve, en otro
     * caso devuelve null
     *
     * @param alPrerequisites
     * @param iPrerequisiteID
     * @return
     */
    public static HeroPrerequisite getHeroPrerequisite(ArrayList<HeroPrerequisite> alPrerequisites, int iPrerequisiteID) {
        if (alPrerequisites == null || alPrerequisites.size() == 0) {
            return null;
        }

        for (int i = 0; i < alPrerequisites.size(); i++) {
            if (alPrerequisites.get(i).getId() == iPrerequisiteID) {
                return alPrerequisites.get(i);
            }
        }

        return null;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = in.readInt();
        valueInt = in.readInt();
        valueBoolean = in.readBoolean();
        valueString = (String) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(id);
        out.writeInt(valueInt);
        out.writeBoolean(valueBoolean);
        out.writeObject(valueString);
    }
}
