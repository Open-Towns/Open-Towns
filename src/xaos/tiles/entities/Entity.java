package xaos.tiles.entities;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import xaos.TownsProperties;

import xaos.main.Game;
import xaos.main.World;
import xaos.panels.menus.SmartMenu;
import xaos.tiles.Cell;
import xaos.tiles.Tile;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.living.LivingEntity;
import xaos.utils.Point3D;
import xaos.utils.Point3DShort;

//public abstract class Entity extends Tile implements Comparable<Entity>, Serializable {
public abstract class Entity extends Tile implements Externalizable {

    private static final long serialVersionUID = -2225320099181906815L;

    // Coordinates
    private Point3DShort coordinates = Point3DShort.getPoolInstance((short) -1, (short) -1, (short) -1);

    public Entity() {
        super();
    }

    public Entity(String iniHeader) {
        super(iniHeader);

        // ID
        setID(World.getNextEntityID());
    }

    public void setCoordinates(short iX, short iY, short iZ) {
        coordinates.setPoint(iX, iY, iZ);
    }

    public void setCoordinates(int iX, int iY, int iZ) {
        coordinates.setPoint(iX, iY, iZ);
    }

    public void setCoordinates(Point3DShort point) {
        coordinates.setPoint(point);
    }

    public Point3DShort getCoordinates() {
        return coordinates;
    }

    public short getX() {
        return coordinates.x;
    }

    public short getY() {
        return coordinates.y;
    }

    public short getZ() {
        return coordinates.z;
    }

    public void init(int x, int y, int z) {
        setCoordinates(x, y, z);
    }

    public void delete() {
        World.getCell(getCoordinates()).setEntity(null);
    }

    /**
     * Borra una living de la celda eb la que está
     */
    public void deleteLiving() {
        ArrayList<LivingEntity> livings = World.getCell(getCoordinates()).getLivings();
        if (livings != null) {
            LivingEntity le;
            for (int i = 0; i < livings.size(); i++) {
                le = livings.get(i);
                if (le.getID() == getID()) {
                    livings.remove(i);
                    break;
                }
            }
        }
    }

    public abstract String getTileName();

    /**
     * Fills a contextual menú refering an entity of a cell
     *
     * @param cell
     * @param sm
     */
    public static void fillMenu(Cell cell, SmartMenu sm) {
        if (cell.hasEntity() && !cell.hasBuilding()) {
            Point3D p3d = cell.getCoordinates().toPoint3D();

            if (TownsProperties.DEBUG_MODE) {
                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "Coord " + cell.getEntity().getCoordinates(), null, null, null, null, p3d)); //$NON-NLS-1$
                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "Num " + Item.getNumItemsTotal(cell.getEntity().getIniHeader(), World.MAP_DEPTH - 1), null, null, null, null, p3d)); //$NON-NLS-1$
                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "Num over restriction (" + Game.getWorld ().getRestrictHaulEquippingLevel () + "): " + + Item.getNumItemsTotal(cell.getEntity().getIniHeader(), Game.getWorld ().getRestrictHaulEquippingLevel ()), null, null, null, null, p3d)); //$NON-NLS-1$ //$NON-NLS-2$
                sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "", null, null, null, null, p3d)); //$NON-NLS-1$
            }
            sm.addItem(new SmartMenu(SmartMenu.TYPE_TEXT, "", null, null, null)); //$NON-NLS-1$
        }
    }

    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        coordinates = (Point3DShort) in.readObject();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(coordinates);
    }
}
