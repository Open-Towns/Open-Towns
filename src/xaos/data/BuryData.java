package xaos.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import xaos.main.Game;
import xaos.main.World;
import xaos.tiles.Cell;
import xaos.tiles.entities.items.Item;
import xaos.tiles.entities.items.ItemManager;
import xaos.tiles.entities.items.ItemManagerItem;
import xaos.utils.Messages;
import xaos.utils.Point3DShort;
import xaos.utils.UtilsIniHeaders;

public class BuryData implements Externalizable {

    private static final long serialVersionUID = 1902280084956466209L;

    public static final int BURY_VERSION_V12 = 1;
    public static final int BURY_VERSION_V13 = 2;
    public static final int BURY_VERSION = BURY_VERSION_V13;

    public static final String EMPTY = "_EMPTY";

    private int version;
    private int heightMin; // Altura mínima
    private int height; // Altura (SÓLO SE ACTUALIZA CUANDO SE GENERA EL BURY EN UN NUEVO MAPA, NO VIENE DE SERIE, YA QUE DEPENDE DE LO QUE HAYA EN EL MAPA)
    private HashMap<Point3DShort, Integer> hash; // Hash por cada punto del bury con su numeric iniHeader
    private HashMap<Point3DShort, ArrayList<String>> hashTexts; // Hash por cada punto del bury con los textos de item (si tiene)

    public BuryData() {
        hash = null;
        hashTexts = null;
        heightMin = -1;
    }

    public void setHeightMin(int iHeight) {
        this.heightMin = iHeight;
    }

    public int getHeightMin() {
        return heightMin;
    }

    public void setHeight(int iHeight) {
        this.height = iHeight;
    }

    public int getHeight() {
        return height;
    }

    public void setHash(HashMap<Point3DShort, Integer> oHash) {
        this.hash = oHash;
    }

    public HashMap<Point3DShort, Integer> getHash() {
        return hash;
    }

    /**
     * @param hashTexts the hashTexts to set
     */
    public void setHashTexts(HashMap<Point3DShort, ArrayList<String>> hashTexts) {
        this.hashTexts = hashTexts;
    }

    /**
     * @return the hashTexts
     */
    public HashMap<Point3DShort, ArrayList<String>> getHashTexts() {
        return hashTexts;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * @return the version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Genera los datos a partir del mundo actual
     */
    public static BuryData generate(String sName) {
        BuryData bd = new BuryData();

        HashMap<Point3DShort, Integer> oHash = new HashMap<Point3DShort, Integer>();
        HashMap<Point3DShort, ArrayList<String>> oHashTexts = new HashMap<Point3DShort, ArrayList<String>>();

        Cell[][][] cells = World.getCells();

        int iEmptyInt = UtilsIniHeaders.getIntIniHeader(EMPTY);

        Item item;
        ItemManagerItem imi;
        int iMinHeight = -1;
        // Items
        for (int x = 0; x < World.MAP_WIDTH; x++) {
            for (int y = 0; y < World.MAP_HEIGHT; y++) {
                for (int z = 0; z < World.MAP_DEPTH; z++) {
                    item = cells[x][y][z].getItem();
                    if (item != null) {
                        imi = ItemManager.getItem(item.getIniHeader());
                        if (imi.isBury()) {
                            oHash.put(Point3DShort.getPoolInstance(x, y, z), UtilsIniHeaders.getIntIniHeader(item.getIniHeader()));

                            if (imi.isText()) {
                                // Si tiene texto lo buscamos y lo metemos
                                ArrayList<String> alTexts = World.getItemsText().get(Integer.valueOf(item.getID()));
                                if (alTexts != null && alTexts.size() > 0) {
                                    oHashTexts.put(Point3DShort.getPoolInstance(x, y, z), alTexts);
                                }
                            }

                            if (iMinHeight == -1) {
                                iMinHeight = z;
                            } else if (iMinHeight > z) {
                                iMinHeight = z;
                            }

                            // Si es un wall, vaciamos lo de debajo hasta encontrar otro wall o una celda non-mined
                            if (imi.isWall()) {
                                ItemManagerItem imiUnderWall;
                                Item itemUnderWall;
                                Cell cellUnderWall;
                                for (int zWall = (z + 1); zWall < (World.MAP_DEPTH - 1); zWall++) {
                                    cellUnderWall = cells[x][y][zWall];
                                    if (!cellUnderWall.isMined()) {
                                        break;
                                    }

                                    itemUnderWall = cellUnderWall.getItem();
                                    if (itemUnderWall == null) {
                                        oHash.put(Point3DShort.getPoolInstance(x, y, zWall), iEmptyInt);
                                    } else {
                                        imiUnderWall = ItemManager.getItem(itemUnderWall.getIniHeader());
                                        if (imiUnderWall == null) {
                                            break;
                                        }

                                        if (imiUnderWall.isWall()) {
                                            break;
                                        }

                                        // Si llega aquí es que hay que crear un agujero
                                        oHash.put(Point3DShort.getPoolInstance(x, y, zWall), iEmptyInt);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        bd.setHeightMin(iMinHeight);
        bd.setHash(oHash);
        bd.setHashTexts(oHashTexts);

        return bd;
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        if (Game.SAVEGAME_LOADING_VERSION >= Game.SAVEGAME_V12) {
            version = in.readInt();
            heightMin = in.readInt();
            height = in.readInt();

            // Hash
            int iNumPoints = in.readInt();
            hash = new HashMap<Point3DShort, Integer>();
            Point3DShort p3ds;
            String sItem;
            for (int i = 0; i < iNumPoints; i++) {
                p3ds = (Point3DShort) in.readObject();
                sItem = (String) in.readObject();
                if (sItem.equals(EMPTY) || ItemManager.getItem(sItem) != null) {
                    // Existe
                    hash.put(p3ds, UtilsIniHeaders.getIntIniHeader(sItem));
                } else {
                    // Item que no existe (quizá porque es de un mod)
                    hash.clear();
                    hash = null;
                    throw new IOException(Messages.getString("BuryData.0") + " [" + sItem + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
            }

            // Hash texts
            if (version >= BURY_VERSION_V13) {
                hashTexts = (HashMap<Point3DShort, ArrayList<String>>) in.readObject();
            } else {
                hashTexts = new HashMap<Point3DShort, ArrayList<String>>();
            }
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(BURY_VERSION);
        out.writeInt(heightMin);
        out.writeInt(height);

        if (hash != null) {
            out.writeInt(hash.size());
            Iterator<Point3DShort> it = hash.keySet().iterator();
            Point3DShort p3ds;
            Integer iIniHeader;
            while (it.hasNext()) {
                p3ds = (Point3DShort) it.next();
                iIniHeader = hash.get(p3ds);
                if (iIniHeader != null) {
                    out.writeObject(p3ds);
                    out.writeObject(UtilsIniHeaders.getStringIniHeader(iIniHeader.intValue()));
                } else {
                    throw new IOException(Messages.getString("BuryData.3")); //$NON-NLS-1$
                }
            }
        } else {
            out.writeInt(0);
        }

        out.writeObject(hashTexts);
    }
}
