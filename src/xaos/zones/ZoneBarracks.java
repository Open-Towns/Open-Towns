package xaos.zones;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import xaos.data.SoldierGroupData;
import xaos.main.Game;

public class ZoneBarracks extends Zone implements Externalizable {

    private static final long serialVersionUID = -6093213217941710898L;

    private int groupID = -1; // ID de grupo, propietario de la zona

    public ZoneBarracks() {
        super();
    }

    public ZoneBarracks(String sIniHeader) {
        super(sIniHeader);
        setGroupID(-1);
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public int getGroupID() {
        return groupID;
    }

    /**
     * Asigna una zona cuartel libre a un grupo. Si el grupo YA tiene ID de zona
     * NO hace nada y devolvemos false.
     *
     * @param sgd
     * @return true si ha podido
     */
    public static boolean assignZone(SoldierGroupData sgd) {
        return assignZone(sgd, -1);
    }

    /**
     * Asigna una zona cuartel CONCRETA y libre a un grupo. Si el grupo YA tiene
     * ID de zona NO hace nada y devolvemos false.
     *
     * @param sgd
     * @param iZoneID
     * @return true si ha podido
     */
    public static boolean assignZone(SoldierGroupData sgd, int iZoneID) {
        if (sgd.getZoneID() > 0) {
            return false;
        }

        Zone zone;
        ZoneManagerItem zmi;
        for (int j = 0; j < Game.getWorld().getZones().size(); j++) {
            zone = Game.getWorld().getZones().get(j);
            zmi = ZoneManager.getItem(zone.getIniHeader());
            if (zmi.getType() == ZoneManagerItem.TYPE_BARRACKS) {
                if ((iZoneID == -1 || zone.getID() == iZoneID) && ((ZoneBarracks) zone).getGroupID() == -1) {
                    // La tenemos
                    sgd.setZoneID(zone.getID());
                    ((ZoneBarracks) zone).setGroupID(sgd.getId());
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Libera una cuartel zone, se usa si se han petado todas las celdas de la
     * zona, por ejemplo.
     *
     * @param sgd
     */
    public static void unAssignZone(SoldierGroupData sgd) {
        int iID = sgd.getZoneID();
        if (iID != 0) {
            Zone zone;
            for (int i = 0; i < Game.getWorld().getZones().size(); i++) {
                zone = Game.getWorld().getZones().get(i);
                if (zone.getID() == iID && ZoneManager.getItem(zone.getIniHeader()).getType() == ZoneManagerItem.TYPE_BARRACKS) {
                    ((ZoneBarracks) zone).setGroupID(-1);
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        groupID = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(groupID);
    }
}
