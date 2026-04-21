package xaos.zones;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import xaos.main.Game;
import xaos.tiles.entities.living.heroes.Hero;

public class ZoneHeroRoom extends Zone implements Externalizable {

    private static final long serialVersionUID = 5451613796754869362L;

    private int ownerID = -1; // ID de héroe, propietario de la zona

    public ZoneHeroRoom() {
        super();
    }

    public ZoneHeroRoom(String sIniHeader) {
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
     * Asigna una zona personal libre a un héroe. Si el héroe YA tiene ID de
     * zona NO hace nada y devolvemos false.
     *
     * @param hero
     * @return true si ha podido
     */
    public static boolean assignZone(Hero hero) {
        return assignZone(hero, -1);
    }

    /**
     * Asigna una zona personal CONCRETA y libre a un héroe Si el héroe YA tiene
     * ID de zona NO hace nada y devolvemos false.
     *
     * @param hero
     * @return true si ha podido
     */
    public static boolean assignZone(Hero hero, int iZoneID) {
        if (hero.getCitizenData().getZoneID() > 0) {
            return false;
        }

        Zone zone;
        ZoneManagerItem zmi;
        for (int j = 0; j < Game.getWorld().getZones().size(); j++) {
            zone = Game.getWorld().getZones().get(j);
            zmi = ZoneManager.getItem(zone.getIniHeader());
            if (zmi.getType() == ZoneManagerItem.TYPE_HERO_ROOM) {
                if ((iZoneID == -1 || zone.getID() == iZoneID) && ((ZoneHeroRoom) zone).getOwnerID() == -1) {
                    // La tenemos
                    hero.getCitizenData().setZoneID(zone.getID());
                    ((ZoneHeroRoom) zone).setOwnerID(hero.getID());
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Libera una personal hero zone, se usa si el héroe muere o se pira, por
     * ejemplo.
     *
     * @param hero
     */
    public static void unAssignZone(Hero hero) {
        int iID = hero.getCitizenData().getZoneID();
        if (iID != 0) {
            Zone zone;
            for (int i = 0; i < Game.getWorld().getZones().size(); i++) {
                zone = Game.getWorld().getZones().get(i);
                if (zone.getID() == iID && ZoneManager.getItem(zone.getIniHeader()).getType() == ZoneManagerItem.TYPE_HERO_ROOM) {
                    ((ZoneHeroRoom) zone).setOwnerID(-1);
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
