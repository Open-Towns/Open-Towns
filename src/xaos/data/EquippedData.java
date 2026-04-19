package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.military.MilitaryItem;

public class EquippedData implements Externalizable {

    private static final long serialVersionUID = 151096510604619758L;

    private MilitaryItem head;
    private MilitaryItem body;
    private MilitaryItem legs;
    private MilitaryItem feet;
    private MilitaryItem weapon;

    public EquippedData() {
    }

    public void refreshTransients() {
        if (head != null) {
            head.refreshTransients();
        }
        if (body != null) {
            body.refreshTransients();
        }
        if (legs != null) {
            legs.refreshTransients();
        }
        if (feet != null) {
            feet.refreshTransients();
        }
        if (weapon != null) {
            weapon.refreshTransients();
        }
    }

    public MilitaryItem getHead() {
        return head;
    }

    public void setHead(MilitaryItem head) {
        this.head = head;
    }

    public MilitaryItem getBody() {
        return body;
    }

    public void setBody(MilitaryItem body) {
        this.body = body;
    }

    public MilitaryItem getLegs() {
        return legs;
    }

    public void setLegs(MilitaryItem legs) {
        this.legs = legs;
    }

    public MilitaryItem getFeet() {
        return feet;
    }

    public void setFeet(MilitaryItem feet) {
        this.feet = feet;
    }

    public MilitaryItem getWeapon() {
        return weapon;
    }

    public void setWeapon(MilitaryItem weapon) {
        this.weapon = weapon;
    }

    public boolean isWearing(int location) {
        switch (location) {
            case MilitaryItem.LOCATION_HEAD:
                return getHead() != null;
            case MilitaryItem.LOCATION_BODY:
                return getBody() != null;
            case MilitaryItem.LOCATION_LEGS:
                return getLegs() != null;
            case MilitaryItem.LOCATION_FEET:
                return getFeet() != null;
            case MilitaryItem.LOCATION_WEAPON:
                return getWeapon() != null;
        }
        return false;
    }

    public MilitaryItem getLocation(int location) {
        switch (location) {
            case MilitaryItem.LOCATION_HEAD:
                return getHead();
            case MilitaryItem.LOCATION_BODY:
                return getBody();
            case MilitaryItem.LOCATION_LEGS:
                return getLegs();
            case MilitaryItem.LOCATION_FEET:
                return getFeet();
            case MilitaryItem.LOCATION_WEAPON:
                return getWeapon();
        }
        return null;
    }

    /**
     * Equipa un item en la casilla que le toca
     *
     * @param mi
     * @return true si puede equiparlo
     */
    public boolean equip(MilitaryItem mi) {
        int location = ItemManager.getItem(mi.getIniHeader()).getLocation();

        switch (location) {
            case MilitaryItem.LOCATION_HEAD:
                if (getHead() == null) {
                    setHead(mi);
                    return true;
                }
                break;
            case MilitaryItem.LOCATION_BODY:
                if (getBody() == null) {
                    setBody(mi);
                    return true;
                }
                break;
            case MilitaryItem.LOCATION_LEGS:
                if (getLegs() == null) {
                    setLegs(mi);
                    return true;
                }
                break;
            case MilitaryItem.LOCATION_FEET:
                if (getFeet() == null) {
                    setFeet(mi);
                    return true;
                }
                break;
            case MilitaryItem.LOCATION_WEAPON:
                if (getWeapon() == null) {
                    setWeapon(mi);
                    return true;
                }
                break;
        }

        return false;
    }

    /**
     * DesEquipa un item en la casilla que le toca
     *
     * @param location
     * @return El item desequipado o null si no ha podido
     */
    public MilitaryItem unequip(int location) {
        switch (location) {
            case MilitaryItem.LOCATION_HEAD:
                if (getHead() != null) {
                    MilitaryItem mi = getHead();
                    setHead(null);
                    return mi;
                }
                break;
            case MilitaryItem.LOCATION_BODY:
                if (getBody() != null) {
                    MilitaryItem mi = getBody();
                    setBody(null);
                    return mi;
                }
                break;
            case MilitaryItem.LOCATION_LEGS:
                if (getLegs() != null) {
                    MilitaryItem mi = getLegs();
                    setLegs(null);
                    return mi;
                }
                break;
            case MilitaryItem.LOCATION_FEET:
                if (getFeet() != null) {
                    MilitaryItem mi = getFeet();
                    setFeet(null);
                    return mi;
                }
                break;
            case MilitaryItem.LOCATION_WEAPON:
                if (getWeapon() != null) {
                    MilitaryItem mi = getWeapon();
                    setWeapon(null);
                    return mi;
                }
                break;
        }

        return null;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        head = (MilitaryItem) in.readObject();
        body = (MilitaryItem) in.readObject();
        legs = (MilitaryItem) in.readObject();
        feet = (MilitaryItem) in.readObject();
        weapon = (MilitaryItem) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(head);
        out.writeObject(body);
        out.writeObject(legs);
        out.writeObject(feet);
        out.writeObject(weapon);
    }
}
