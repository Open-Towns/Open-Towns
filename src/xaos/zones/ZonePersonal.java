package xaos.zones;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import xaos.main.Game;
import xaos.tiles.entities.living.Citizen;

public class ZonePersonal extends Zone implements Externalizable {

    private static final long serialVersionUID = -848265510157587431L;

    private int ownerID = -1; // ID de citizen, propietario de la zona

    public ZonePersonal() {
        super();
    }

    public ZonePersonal(String sIniHeader) {
        super(sIniHeader);
        setOwnerID(-1);
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
    }

    public int getOwnerID() {
        return ownerID;
    }

    /**
     * Asigna una zona personal libre a un aldeano Si el aldeano YA tiene ID de
     * zona NO hace nada y devolvemos false.
     *
     * @param citizen
     * @return true si ha podido
     */
    public static boolean assignZone(Citizen citizen) {
        return assignZone(citizen, -1);
    }

    /**
     * Asigna una zona personal CONCRETA y libre a un aldeano Si el aldeano YA
     * tiene ID de zona NO hace nada y devolvemos false.
     *
     * @param citizen
     * @return true si ha podido
     */
    public static boolean assignZone(Citizen citizen, int iZoneID) {
        if (citizen.getCitizenData().getZoneID() > 0) {
            return false;
        }

        Zone zone;
        ZoneManagerItem zmi;
        for (int j = 0; j < Game.getWorld().getZones().size(); j++) {
            zone = Game.getWorld().getZones().get(j);
            zmi = ZoneManager.getItem(zone.getIniHeader());
            if (zmi.getType() == ZoneManagerItem.TYPE_PERSONAL) {
                if ((iZoneID == -1 || zone.getID() == iZoneID) && ((ZonePersonal) zone).getOwnerID() == -1) {
                    // La tenemos
                    citizen.getCitizenData().setZoneID(zone.getID());
                    ((ZonePersonal) zone).setOwnerID(citizen.getID());
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Libera una personal zone, se usa si el ciudadano muere, por ejemplo.
     *
     * @param citizen
     */
    public static void unAssignZone(Citizen citizen) {
        int iID = citizen.getCitizenData().getZoneID();
        if (iID != 0) {
            Zone zone;
            for (int i = 0; i < Game.getWorld().getZones().size(); i++) {
                zone = Game.getWorld().getZones().get(i);
                if (zone.getID() == iID && ZoneManager.getItem(zone.getIniHeader()).getType() == ZoneManagerItem.TYPE_PERSONAL) {
                    ((ZonePersonal) zone).setOwnerID(-1);
                    break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        ownerID = in.readInt();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeInt(ownerID);
    }
}
